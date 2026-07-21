import { useEffect, useState } from 'react';
import { listEmployees } from '../../api/employees';
import {
  closeOperation,
  listJobs,
  listOperationClosures,
  listOperations,
  listWorkstations,
  reopenOperationClosure,
} from '../../api/production';
import type { Employee } from '../../api/types';
import type { ClosureReason, Job, Operation, OperationClosure, Workstation } from '../../api/productionTypes';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

function errorMessage(err: unknown, fallback: string): string {
  const maybe = err as { response?: { data?: { message?: string } } };
  return maybe?.response?.data?.message ?? fallback;
}

export default function OperationClosuresPage() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [operations, setOperations] = useState<Operation[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [jobId, setJobId] = useState<number | ''>('');
  const [closures, setClosures] = useState<OperationClosure[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [colourId, setColourId] = useState<number | ''>('');
  const [sizeId, setSizeId] = useState<number | ''>('');
  const [workstationId, setWorkstationId] = useState<number | ''>('');
  const [operationId, setOperationId] = useState<number | ''>('');
  const [doneQty, setDoneQty] = useState('');
  const [plannedQty, setPlannedQty] = useState('');
  const [reason, setReason] = useState<ClosureReason>('RAW_MATERIAL');
  const [reworkQty, setReworkQty] = useState('');
  const [rejectionQty, setRejectionQty] = useState('');
  const [notes, setNotes] = useState('');
  const [closedByEmployeeId, setClosedByEmployeeId] = useState<number | ''>('');
  const [date, setDate] = useState(today());

  const selectedJob = jobs.find((j) => j.id === jobId) ?? null;
  const selectedColour = selectedJob?.colours.find((c) => c.id === colourId) ?? null;

  useEffect(() => {
    listJobs(true).then(setJobs).catch(() => setError('Could not load jobs.'));
    listWorkstations().then(setWorkstations).catch(() => setError('Could not load workstations.'));
    listOperations().then(setOperations).catch(() => setError('Could not load operations.'));
    listEmployees().then(setEmployees).catch(() => setError('Could not load employees.'));
  }, []);

  function refreshClosures(id: number) {
    listOperationClosures(id).then(setClosures).catch(() => setError('Could not load operation closures.'));
  }

  useEffect(() => {
    if (jobId === '') {
      setClosures([]);
      return;
    }
    refreshClosures(jobId);
  }, [jobId]);

  async function handleClose(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (jobId === '' || workstationId === '' || operationId === '' || closedByEmployeeId === '') return;
    try {
      await closeOperation({
        jobId,
        colourId: colourId === '' ? undefined : colourId,
        sizeId: sizeId === '' ? undefined : sizeId,
        workstationId,
        operationId,
        doneQtyAtClosure: Number(doneQty),
        plannedQty: Number(plannedQty),
        reason,
        reworkQty: reason === 'REWORK' ? Number(reworkQty) : undefined,
        rejectionQty: reason === 'REJECTION' ? Number(rejectionQty) : undefined,
        notes: notes || undefined,
        closedByEmployeeId,
        date,
      });
      setMessage('Operation closed.');
      setDoneQty('');
      setPlannedQty('');
      setReworkQty('');
      setRejectionQty('');
      setNotes('');
      refreshClosures(jobId);
    } catch (err) {
      setError(errorMessage(err, 'Could not close operation.'));
    }
  }

  async function handleReopen(id: number) {
    if (jobId === '') return;
    setError(null);
    try {
      await reopenOperationClosure(id);
      refreshClosures(jobId);
    } catch {
      setError('Could not reopen operation.');
    }
  }

  return (
    <div>
      <h1>Operation Closures</h1>
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
            <h2>Close an operation</h2>
            <form onSubmit={handleClose}>
              <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
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
                <select
                  value={workstationId}
                  onChange={(e) => {
                    setWorkstationId(e.target.value ? Number(e.target.value) : '');
                    setOperationId('');
                  }}
                  required
                >
                  <option value="">Workstation</option>
                  {workstations.map((ws) => (
                    <option key={ws.id} value={ws.id}>
                      {ws.name}
                    </option>
                  ))}
                </select>
                <select value={operationId} onChange={(e) => setOperationId(e.target.value ? Number(e.target.value) : '')} required>
                  <option value="">Operation</option>
                  {operations
                    .filter((op) => workstationId === '' || op.workstationId === workstationId)
                    .map((op) => (
                      <option key={op.id} value={op.id}>
                        {op.name}
                      </option>
                    ))}
                </select>
              </div>
              <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
                <input type="number" min={0} placeholder="Done qty at closure" value={doneQty} onChange={(e) => setDoneQty(e.target.value)} required style={{ width: 160 }} />
                <input type="number" min={0} placeholder="Planned qty" value={plannedQty} onChange={(e) => setPlannedQty(e.target.value)} required style={{ width: 120 }} />
                <select value={reason} onChange={(e) => setReason(e.target.value as ClosureReason)}>
                  <option value="RAW_MATERIAL">Raw material</option>
                  <option value="REWORK">Rework</option>
                  <option value="REJECTION">Rejection</option>
                  <option value="COMPONENT_LOSS">Component loss</option>
                  <option value="OTHER">Other</option>
                </select>
                {reason === 'REWORK' && (
                  <input type="number" min={0} placeholder="Rework qty" value={reworkQty} onChange={(e) => setReworkQty(e.target.value)} required style={{ width: 120 }} />
                )}
                {reason === 'REJECTION' && (
                  <input type="number" min={0} placeholder="Rejection qty" value={rejectionQty} onChange={(e) => setRejectionQty(e.target.value)} required style={{ width: 120 }} />
                )}
              </div>
              <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
                <select value={closedByEmployeeId} onChange={(e) => setClosedByEmployeeId(e.target.value ? Number(e.target.value) : '')} required>
                  <option value="">Closed by (employee)</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.empId} — {emp.name}
                    </option>
                  ))}
                </select>
                <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
                <input placeholder="Notes (optional)" value={notes} onChange={(e) => setNotes(e.target.value)} style={{ minWidth: 200 }} />
              </div>
              <button type="submit">Close operation</button>
            </form>
          </section>

          <section>
            <h2>Closures for {selectedJob?.jobDisplayId}</h2>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={th}>Workstation</th>
                  <th style={th}>Operation</th>
                  <th style={th}>Done/Planned</th>
                  <th style={th}>Reason</th>
                  <th style={th}>Closed by</th>
                  <th style={th}>Date</th>
                  <th style={th}></th>
                </tr>
              </thead>
              <tbody>
                {closures.map((c) => (
                  <tr key={c.id}>
                    <td style={td}>{c.workstationName}</td>
                    <td style={td}>{c.operationName}</td>
                    <td style={td}>
                      {c.doneQtyAtClosure}/{c.plannedQty}
                    </td>
                    <td style={td}>{c.reason}</td>
                    <td style={td}>{c.closedByEmployeeName}</td>
                    <td style={td}>{c.date}</td>
                    <td style={td}>
                      <button onClick={() => handleReopen(c.id)}>Reopen</button>
                    </td>
                  </tr>
                ))}
                {closures.length === 0 && (
                  <tr>
                    <td style={td} colSpan={7}>
                      No closures for this job.
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
