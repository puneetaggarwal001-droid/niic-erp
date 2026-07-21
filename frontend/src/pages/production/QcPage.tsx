import { useEffect, useState } from 'react';
import {
  createQcEntry,
  fillQcEntryDetails,
  listJobs,
  listQcEntries,
  listQcRework,
  listWorkstations,
  recordQcReworkResult,
} from '../../api/production';
import type { Job, QcEntry, QcRework, Side, Workstation } from '../../api/productionTypes';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

function errorMessage(err: unknown, fallback: string): string {
  const maybe = err as { response?: { data?: { message?: string } } };
  return maybe?.response?.data?.message ?? fallback;
}

export default function QcPage() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [jobId, setJobId] = useState<number | ''>('');
  const [entries, setEntries] = useState<QcEntry[]>([]);
  const [rework, setRework] = useState<QcRework[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [date, setDate] = useState(today());
  const [colourId, setColourId] = useState<number | ''>('');
  const [sizeId, setSizeId] = useState<number | ''>('');
  const [side, setSide] = useState<Side | ''>('');
  const [opRef, setOpRef] = useState('');
  const [workstationId, setWorkstationId] = useState<number | ''>('');
  const [totalChecked, setTotalChecked] = useState('');
  const [passQty, setPassQty] = useState('');
  const [alterQty, setAlterQty] = useState('');
  const [rejectQty, setRejectQty] = useState('');
  const [skipDetails, setSkipDetails] = useState(false);

  const [fillValues, setFillValues] = useState<Record<number, { passQty: string; alterQty: string; rejectQty: string }>>({});
  const [reworkValues, setReworkValues] = useState<Record<number, { done: string; reject: string }>>({});

  const selectedJob = jobs.find((j) => j.id === jobId) ?? null;
  const selectedColour = selectedJob?.colours.find((c) => c.id === colourId) ?? null;

  useEffect(() => {
    listJobs(true).then(setJobs).catch(() => setError('Could not load jobs.'));
    listWorkstations().then(setWorkstations).catch(() => setError('Could not load workstations.'));
  }, []);

  function refresh(id: number) {
    listQcEntries(id).then(setEntries).catch(() => setError('Could not load QC entries.'));
    listQcRework(id).then(setRework).catch(() => setError('Could not load rework records.'));
  }

  useEffect(() => {
    if (jobId === '') {
      setEntries([]);
      setRework([]);
      return;
    }
    refresh(jobId);
  }, [jobId]);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (jobId === '') return;
    try {
      await createQcEntry({
        date,
        jobId,
        colourId: colourId === '' ? undefined : colourId,
        sizeId: sizeId === '' ? undefined : sizeId,
        side: side === '' ? undefined : side,
        opRef: opRef || undefined,
        workstationId: workstationId === '' ? undefined : workstationId,
        totalChecked: Number(totalChecked),
        passQty: skipDetails ? 0 : Number(passQty || 0),
        alterQty: skipDetails ? 0 : Number(alterQty || 0),
        rejectQty: skipDetails ? 0 : Number(rejectQty || 0),
        skipDetails,
      });
      setMessage('QC entry saved.');
      setTotalChecked('');
      setPassQty('');
      setAlterQty('');
      setRejectQty('');
      setOpRef('');
      refresh(jobId);
    } catch (err) {
      setError(errorMessage(err, 'Could not save QC entry.'));
    }
  }

  async function handleFillDetails(entry: QcEntry) {
    setError(null);
    setMessage(null);
    const values = fillValues[entry.id];
    if (!values || jobId === '') return;
    try {
      await fillQcEntryDetails(entry.id, {
        passQty: Number(values.passQty || 0),
        alterQty: Number(values.alterQty || 0),
        rejectQty: Number(values.rejectQty || 0),
      });
      setMessage('QC details filled in.');
      refresh(jobId);
    } catch (err) {
      setError(errorMessage(err, 'Could not fill in QC details.'));
    }
  }

  async function handleRecordResult(r: QcRework) {
    setError(null);
    setMessage(null);
    const values = reworkValues[r.id];
    if (!values || jobId === '') return;
    try {
      await recordQcReworkResult(r.id, Number(values.done || 0), Number(values.reject || 0));
      setMessage('Rework result recorded.');
      refresh(jobId);
    } catch (err) {
      setError(errorMessage(err, 'Could not record rework result.'));
    }
  }

  return (
    <div>
      <h1>Quality Control</h1>
      {error && <p style={{ color: '#dc2626' }}>{error}</p>}
      {message && <p style={{ color: '#15803d' }}>{message}</p>}

      <div style={{ marginBottom: 16 }}>
        <select
          value={jobId}
          onChange={(e) => {
            setJobId(e.target.value ? Number(e.target.value) : '');
            setColourId('');
            setSizeId('');
          }}
        >
          <option value="">Select a job</option>
          {jobs.map((j) => (
            <option key={j.id} value={j.id}>
              {j.jobDisplayId} — {j.modelNo}
            </option>
          ))}
        </select>
      </div>

      {jobId !== '' && (
        <>
          <section style={{ marginBottom: 32 }}>
            <h2>New QC entry</h2>
            <form onSubmit={handleCreate}>
              <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
                <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
                {selectedJob && (
                  <select
                    value={colourId}
                    onChange={(e) => {
                      setColourId(e.target.value ? Number(e.target.value) : '');
                      setSizeId('');
                    }}
                  >
                    <option value="">Colour (optional)</option>
                    {selectedJob.colours.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                )}
                {selectedColour && (
                  <select value={sizeId} onChange={(e) => setSizeId(e.target.value ? Number(e.target.value) : '')}>
                    <option value="">Size (optional)</option>
                    {selectedColour.sizes.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.size}
                      </option>
                    ))}
                  </select>
                )}
                <select value={side} onChange={(e) => setSide(e.target.value as Side | '')}>
                  <option value="">Side (optional)</option>
                  <option value="LEFT">Left</option>
                  <option value="RIGHT">Right</option>
                  <option value="PAIR">Pair</option>
                </select>
                <select value={workstationId} onChange={(e) => setWorkstationId(e.target.value ? Number(e.target.value) : '')}>
                  <option value="">Workstation (optional)</option>
                  {workstations.map((ws) => (
                    <option key={ws.id} value={ws.id}>
                      {ws.name}
                    </option>
                  ))}
                </select>
                <input placeholder="Op ref (optional)" value={opRef} onChange={(e) => setOpRef(e.target.value)} />
              </div>
              <div style={{ display: 'flex', gap: 8, marginBottom: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                <input type="number" min={1} placeholder="Total checked" value={totalChecked} onChange={(e) => setTotalChecked(e.target.value)} required style={{ width: 130 }} />
                <label>
                  <input type="checkbox" checked={skipDetails} onChange={(e) => setSkipDetails(e.target.checked)} /> Fill pass/alter/reject
                  details later
                </label>
              </div>
              {!skipDetails && (
                <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                  <input type="number" min={0} placeholder="Pass qty" value={passQty} onChange={(e) => setPassQty(e.target.value)} style={{ width: 110 }} />
                  <input type="number" min={0} placeholder="Alter qty" value={alterQty} onChange={(e) => setAlterQty(e.target.value)} style={{ width: 110 }} />
                  <input type="number" min={0} placeholder="Reject qty" value={rejectQty} onChange={(e) => setRejectQty(e.target.value)} style={{ width: 110 }} />
                </div>
              )}
              <button type="submit">Save QC entry</button>
            </form>
          </section>

          <section style={{ marginBottom: 32 }}>
            <h2>QC entries for {selectedJob?.jobDisplayId}</h2>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={th}>Date</th>
                  <th style={th}>Op ref</th>
                  <th style={th}>Total</th>
                  <th style={th}>Pass/Alter/Reject</th>
                  <th style={th}>Status</th>
                  <th style={th}></th>
                </tr>
              </thead>
              <tbody>
                {entries.map((entry) => (
                  <tr key={entry.id}>
                    <td style={td}>{entry.date}</td>
                    <td style={td}>{entry.opRef ?? '—'}</td>
                    <td style={td}>{entry.totalChecked}</td>
                    <td style={td}>
                      {entry.status === 'COMPLETED' ? (
                        `${entry.passQty}/${entry.alterQty}/${entry.rejectQty}`
                      ) : (
                        <div style={{ display: 'flex', gap: 4 }}>
                          <input
                            type="number"
                            min={0}
                            placeholder="Pass"
                            style={{ width: 60 }}
                            value={fillValues[entry.id]?.passQty ?? ''}
                            onChange={(e) =>
                              setFillValues((prev) => ({ ...prev, [entry.id]: { passQty: e.target.value, alterQty: prev[entry.id]?.alterQty ?? '', rejectQty: prev[entry.id]?.rejectQty ?? '' } }))
                            }
                          />
                          <input
                            type="number"
                            min={0}
                            placeholder="Alter"
                            style={{ width: 60 }}
                            value={fillValues[entry.id]?.alterQty ?? ''}
                            onChange={(e) =>
                              setFillValues((prev) => ({ ...prev, [entry.id]: { passQty: prev[entry.id]?.passQty ?? '', alterQty: e.target.value, rejectQty: prev[entry.id]?.rejectQty ?? '' } }))
                            }
                          />
                          <input
                            type="number"
                            min={0}
                            placeholder="Reject"
                            style={{ width: 60 }}
                            value={fillValues[entry.id]?.rejectQty ?? ''}
                            onChange={(e) =>
                              setFillValues((prev) => ({ ...prev, [entry.id]: { passQty: prev[entry.id]?.passQty ?? '', alterQty: prev[entry.id]?.alterQty ?? '', rejectQty: e.target.value } }))
                            }
                          />
                        </div>
                      )}
                    </td>
                    <td style={td}>{entry.status}</td>
                    <td style={td}>{entry.status === 'PENDING_DETAILS' && <button onClick={() => handleFillDetails(entry)}>Save details</button>}</td>
                  </tr>
                ))}
                {entries.length === 0 && (
                  <tr>
                    <td style={td} colSpan={6}>
                      No QC entries for this job.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </section>

          <section>
            <h2>Rework</h2>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={th}>Alter qty</th>
                  <th style={th}>Done/Reject so far</th>
                  <th style={th}>Status</th>
                  <th style={th}></th>
                </tr>
              </thead>
              <tbody>
                {rework.map((r) => (
                  <tr key={r.id}>
                    <td style={td}>{r.alterQty}</td>
                    <td style={td}>
                      {r.reworkDone}/{r.reworkReject}
                    </td>
                    <td style={td}>{r.status}</td>
                    <td style={td}>
                      {r.status === 'PENDING' && (
                        <div style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                          <input
                            type="number"
                            min={0}
                            placeholder="Done"
                            style={{ width: 70 }}
                            value={reworkValues[r.id]?.done ?? ''}
                            onChange={(e) => setReworkValues((prev) => ({ ...prev, [r.id]: { done: e.target.value, reject: prev[r.id]?.reject ?? '' } }))}
                          />
                          <input
                            type="number"
                            min={0}
                            placeholder="Reject"
                            style={{ width: 70 }}
                            value={reworkValues[r.id]?.reject ?? ''}
                            onChange={(e) => setReworkValues((prev) => ({ ...prev, [r.id]: { done: prev[r.id]?.done ?? '', reject: e.target.value } }))}
                          />
                          <button onClick={() => handleRecordResult(r)}>Save</button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
                {rework.length === 0 && (
                  <tr>
                    <td style={td} colSpan={4}>
                      No rework records for this job.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </section>
        </>
      )}
    </div>
  );
}
