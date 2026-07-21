import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  approveJobRequest,
  createJob,
  listJobRequests,
  listJobs,
  listStyles,
  rejectJobRequest,
  setJobActive,
  submitJobRequest,
} from '../../api/production';
import type { Job, JobColourRequest, JobRequest, Style, Unit } from '../../api/productionTypes';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };

interface ColourRow {
  name: string;
  sizes: { size: string; plannedQty: string }[];
}

function emptyColour(): ColourRow {
  return { name: '', sizes: [{ size: '', plannedQty: '' }] };
}

export default function JobsPage() {
  const { auth } = useAuth();
  const isAdmin = auth?.role === 'ADMIN';
  const canCreate = isAdmin || (auth?.rights.includes('create_job') ?? false);

  const [styles, setStyles] = useState<Style[]>([]);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [jobRequests, setJobRequests] = useState<JobRequest[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [styleCode, setStyleCode] = useState('');
  const [modelNo, setModelNo] = useState('');
  const [unit, setUnit] = useState<Unit>('PCS');
  const [colours, setColours] = useState<ColourRow[]>([emptyColour()]);

  function refreshJobs() {
    listJobs(false).then(setJobs).catch(() => setError('Could not load jobs.'));
  }

  function refreshJobRequests() {
    listJobRequests(!isAdmin)
      .then(setJobRequests)
      .catch(() => setError('Could not load job requests.'));
  }

  useEffect(() => {
    listStyles().then(setStyles).catch(() => setError('Could not load styles.'));
    refreshJobs();
    refreshJobRequests();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAdmin]);

  function updateColour(index: number, patch: Partial<ColourRow>) {
    setColours((prev) => prev.map((c, i) => (i === index ? { ...c, ...patch } : c)));
  }

  function updateSize(colourIndex: number, sizeIndex: number, patch: Partial<{ size: string; plannedQty: string }>) {
    setColours((prev) =>
      prev.map((c, i) =>
        i === colourIndex ? { ...c, sizes: c.sizes.map((s, j) => (j === sizeIndex ? { ...s, ...patch } : s)) } : c,
      ),
    );
  }

  function addColour() {
    setColours((prev) => [...prev, emptyColour()]);
  }

  function removeColour(index: number) {
    setColours((prev) => prev.filter((_, i) => i !== index));
  }

  function addSize(colourIndex: number) {
    setColours((prev) =>
      prev.map((c, i) => (i === colourIndex ? { ...c, sizes: [...c.sizes, { size: '', plannedQty: '' }] } : c)),
    );
  }

  function removeSize(colourIndex: number, sizeIndex: number) {
    setColours((prev) =>
      prev.map((c, i) => (i === colourIndex ? { ...c, sizes: c.sizes.filter((_, j) => j !== sizeIndex) } : c)),
    );
  }

  function resetForm() {
    setStyleCode('');
    setModelNo('');
    setUnit('PCS');
    setColours([emptyColour()]);
  }

  async function handleSubmitJob(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    const payloadColours: JobColourRequest[] = colours.map((c) => ({
      name: c.name,
      sizes: c.sizes.map((s) => ({ size: s.size, plannedQty: Number(s.plannedQty) })),
    }));
    try {
      if (isAdmin) {
        await createJob({ styleCode, modelNo, unit, colours: payloadColours });
        setMessage('Job created.');
        refreshJobs();
      } else {
        await submitJobRequest({ styleCode, modelNo, unit, colours: payloadColours });
        setMessage('Job request submitted for admin approval.');
        refreshJobRequests();
      }
      resetForm();
    } catch {
      setError('Could not save job. Check that all fields are filled in correctly.');
    }
  }

  async function handleToggleJob(job: Job) {
    setError(null);
    try {
      await setJobActive(job.id, !job.active);
      refreshJobs();
    } catch {
      setError('Could not update job.');
    }
  }

  async function handleApprove(id: number) {
    setError(null);
    try {
      await approveJobRequest(id);
      refreshJobRequests();
      refreshJobs();
    } catch {
      setError('Could not approve job request.');
    }
  }

  async function handleReject(id: number) {
    setError(null);
    try {
      await rejectJobRequest(id);
      refreshJobRequests();
    } catch {
      setError('Could not reject job request.');
    }
  }

  return (
    <div>
      <h1>Jobs</h1>
      {error && <p style={{ color: '#dc2626' }}>{error}</p>}
      {message && <p style={{ color: '#15803d' }}>{message}</p>}

      {canCreate ? (
        <section style={{ marginBottom: 32 }}>
          <h2>{isAdmin ? 'Create job' : 'Request a new job'}</h2>
          <form onSubmit={handleSubmitJob}>
            <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
              <select value={styleCode} onChange={(e) => setStyleCode(e.target.value)} required>
                <option value="">Style</option>
                {styles.map((s) => (
                  <option key={s.code} value={s.code}>
                    {s.label}
                  </option>
                ))}
              </select>
              <input placeholder="Model no." value={modelNo} onChange={(e) => setModelNo(e.target.value)} required />
              <select value={unit} onChange={(e) => setUnit(e.target.value as Unit)}>
                <option value="PCS">PCS</option>
                <option value="PAIR">PAIR</option>
              </select>
            </div>

            {colours.map((colour, ci) => (
              <div key={ci} style={{ border: '1px solid #eee', padding: 8, marginBottom: 8 }}>
                <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                  <input
                    placeholder="Colour name"
                    value={colour.name}
                    onChange={(e) => updateColour(ci, { name: e.target.value })}
                    required
                  />
                  {colours.length > 1 && (
                    <button type="button" onClick={() => removeColour(ci)}>
                      Remove colour
                    </button>
                  )}
                </div>
                {colour.sizes.map((size, si) => (
                  <div key={si} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
                    <input
                      placeholder="Size"
                      value={size.size}
                      onChange={(e) => updateSize(ci, si, { size: e.target.value })}
                      required
                    />
                    <input
                      type="number"
                      min={1}
                      placeholder="Planned qty"
                      value={size.plannedQty}
                      onChange={(e) => updateSize(ci, si, { plannedQty: e.target.value })}
                      required
                    />
                    {colour.sizes.length > 1 && (
                      <button type="button" onClick={() => removeSize(ci, si)}>
                        Remove size
                      </button>
                    )}
                  </div>
                ))}
                <button type="button" onClick={() => addSize(ci)}>
                  Add size
                </button>
              </div>
            ))}
            <button type="button" onClick={addColour} style={{ marginBottom: 12 }}>
              Add colour
            </button>
            <div>
              <button type="submit">{isAdmin ? 'Create job' : 'Submit request'}</button>
            </div>
          </form>
        </section>
      ) : (
        <p>You don't have permission to create jobs. Ask an admin to grant the "create_job" right.</p>
      )}

      <section style={{ marginBottom: 32 }}>
        <h2>Job requests {isAdmin ? '(pending)' : '(mine)'}</h2>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={th}>Style</th>
              <th style={th}>Model</th>
              <th style={th}>Requested by</th>
              <th style={th}>Status</th>
              {isAdmin && <th style={th}></th>}
            </tr>
          </thead>
          <tbody>
            {jobRequests.map((r) => (
              <tr key={r.id}>
                <td style={td}>{r.styleLabel}</td>
                <td style={td}>{r.modelNo}</td>
                <td style={td}>{r.requestedByUsername}</td>
                <td style={td}>{r.status}</td>
                {isAdmin && (
                  <td style={td}>
                    {r.status === 'PENDING' && (
                      <>
                        <button onClick={() => handleApprove(r.id)}>Approve</button>{' '}
                        <button onClick={() => handleReject(r.id)}>Reject</button>
                      </>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {jobRequests.length === 0 && (
              <tr>
                <td style={td} colSpan={isAdmin ? 5 : 4}>
                  No job requests.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>

      <section>
        <h2>All jobs</h2>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={th}>Job ID</th>
              <th style={th}>Style</th>
              <th style={th}>Model</th>
              <th style={th}>Unit</th>
              <th style={th}>Planned qty</th>
              <th style={th}>Routing set</th>
              <th style={th}>Active</th>
              {isAdmin && <th style={th}></th>}
            </tr>
          </thead>
          <tbody>
            {jobs.map((j) => (
              <tr key={j.id}>
                <td style={td}>{j.jobDisplayId}</td>
                <td style={td}>{j.styleLabel}</td>
                <td style={td}>{j.modelNo}</td>
                <td style={td}>{j.unit}</td>
                <td style={td}>{j.totalPlannedQty}</td>
                <td style={td}>{j.routingAssigned ? 'Yes' : 'No'}</td>
                <td style={td}>{j.active ? 'Yes' : 'No'}</td>
                {isAdmin && (
                  <td style={td}>
                    <button onClick={() => handleToggleJob(j)}>{j.active ? 'Deactivate' : 'Activate'}</button>
                  </td>
                )}
              </tr>
            ))}
            {jobs.length === 0 && (
              <tr>
                <td style={td} colSpan={isAdmin ? 8 : 7}>
                  No jobs yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}
