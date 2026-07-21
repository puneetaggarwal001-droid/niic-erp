import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { listEmployees } from '../../api/employees';
import {
  approveProductionEditRequest,
  consolidateProductionEditRequest,
  listJobs,
  listOperations,
  listProductionEditRequests,
  listProductionEntriesForDate,
  listWorkstations,
  rejectProductionEditRequest,
  saveProductionEntry,
  submitProductionEditRequest,
} from '../../api/production';
import type { Employee } from '../../api/types';
import type { Job, Operation, ProductionEditRequest, ProductionEntry, Side, Workstation } from '../../api/productionTypes';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

interface OpLine {
  workstationId: number | '';
  operationId: number | '';
  quantity: string;
}

function emptyOpLine(): OpLine {
  return { workstationId: '', operationId: '', quantity: '' };
}

function errorMessage(err: unknown, fallback: string): string {
  const maybe = err as { response?: { data?: { message?: string } } };
  return maybe?.response?.data?.message ?? fallback;
}

export default function ProductionEntryPage() {
  const { auth } = useAuth();
  const isAdmin = auth?.role === 'ADMIN';

  const [date, setDate] = useState(today());
  const [jobs, setJobs] = useState<Job[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [operations, setOperations] = useState<Operation[]>([]);
  const [entries, setEntries] = useState<ProductionEntry[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [employeeId, setEmployeeId] = useState<number | ''>('');
  const [jobId, setJobId] = useState<number | ''>('');
  const [colourId, setColourId] = useState<number | ''>('');
  const [sizeId, setSizeId] = useState<number | ''>('');
  const [side, setSide] = useState<Side | ''>('');
  const [opLines, setOpLines] = useState<OpLine[]>([emptyOpLine()]);

  const [editRequests, setEditRequests] = useState<ProductionEditRequest[]>([]);
  const [editJobId, setEditJobId] = useState<number | ''>('');
  const [editColourId, setEditColourId] = useState<number | ''>('');
  const [editSizeId, setEditSizeId] = useState<number | ''>('');
  const [editReason, setEditReason] = useState('');
  const [consolidating, setConsolidating] = useState<ProductionEditRequest | null>(null);
  const [consolidateEmployeeId, setConsolidateEmployeeId] = useState<number | ''>('');
  const [consolidateOpLines, setConsolidateOpLines] = useState<OpLine[]>([emptyOpLine()]);

  const selectedJob = jobs.find((j) => j.id === jobId) ?? null;
  const selectedColour = selectedJob?.colours.find((c) => c.id === colourId) ?? null;
  const editJob = jobs.find((j) => j.id === editJobId) ?? null;
  const editColour = editJob?.colours.find((c) => c.id === editColourId) ?? null;

  function refreshEntries(d: string) {
    listProductionEntriesForDate(d).then(setEntries).catch(() => setError('Could not load production entries.'));
  }

  function refreshEditRequests() {
    listProductionEditRequests(!isAdmin)
      .then(setEditRequests)
      .catch(() => setError('Could not load edit requests.'));
  }

  useEffect(() => {
    listJobs(true).then(setJobs).catch(() => setError('Could not load jobs.'));
    listEmployees().then(setEmployees).catch(() => setError('Could not load employees.'));
    listWorkstations().then(setWorkstations).catch(() => setError('Could not load workstations.'));
    listOperations().then(setOperations).catch(() => setError('Could not load operations.'));
    refreshEditRequests();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAdmin]);

  useEffect(() => {
    refreshEntries(date);
  }, [date]);

  function updateOpLine(lines: OpLine[], setLines: (l: OpLine[]) => void, index: number, patch: Partial<OpLine>) {
    setLines(lines.map((l, i) => (i === index ? { ...l, ...patch } : l)));
  }

  async function handleSubmitEntry(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (employeeId === '' || jobId === '') return;
    try {
      await saveProductionEntry({
        date,
        employeeId,
        jobId,
        colourId: colourId === '' ? undefined : colourId,
        sizeId: sizeId === '' ? undefined : sizeId,
        side: side === '' ? undefined : side,
        operations: opLines
          .filter((l) => l.workstationId !== '' && l.operationId !== '' && l.quantity !== '')
          .map((l) => ({ workstationId: l.workstationId as number, operationId: l.operationId as number, quantity: Number(l.quantity) })),
      });
      setMessage('Production entry saved.');
      setOpLines([emptyOpLine()]);
      refreshEntries(date);
    } catch (err) {
      setError(errorMessage(err, 'Could not save production entry.'));
    }
  }

  async function handleSubmitEditRequest(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (editJobId === '') return;
    try {
      await submitProductionEditRequest({
        jobId: editJobId,
        colourId: editColourId === '' ? undefined : editColourId,
        sizeId: editSizeId === '' ? undefined : editSizeId,
        reason: editReason,
      });
      setMessage('Edit request submitted.');
      setEditReason('');
      refreshEditRequests();
    } catch (err) {
      setError(errorMessage(err, 'Could not submit edit request.'));
    }
  }

  async function handleApproveEdit(id: number) {
    try {
      await approveProductionEditRequest(id);
      refreshEditRequests();
    } catch {
      setError('Could not approve edit request.');
    }
  }

  async function handleRejectEdit(id: number) {
    try {
      await rejectProductionEditRequest(id);
      refreshEditRequests();
    } catch {
      setError('Could not reject edit request.');
    }
  }

  function startConsolidate(request: ProductionEditRequest) {
    setConsolidating(request);
    setConsolidateEmployeeId('');
    setConsolidateOpLines([emptyOpLine()]);
    setError(null);
    setMessage(null);
  }

  async function handleConsolidate(e: React.FormEvent) {
    e.preventDefault();
    if (!consolidating || consolidateEmployeeId === '') return;
    setError(null);
    try {
      await consolidateProductionEditRequest(consolidating.id, {
        date,
        employeeId: consolidateEmployeeId,
        jobId: consolidating.jobId,
        colourId: consolidating.colourId ?? undefined,
        sizeId: consolidating.sizeId ?? undefined,
        operations: consolidateOpLines
          .filter((l) => l.workstationId !== '' && l.operationId !== '' && l.quantity !== '')
          .map((l) => ({ workstationId: l.workstationId as number, operationId: l.operationId as number, quantity: Number(l.quantity) })),
      });
      setMessage('Entry consolidated.');
      setConsolidating(null);
      refreshEditRequests();
      refreshEntries(date);
    } catch (err) {
      setError(errorMessage(err, 'Could not consolidate entry.'));
    }
  }

  return (
    <div>
      <h1>Production Entries</h1>
      {error && <p style={{ color: '#dc2626' }}>{error}</p>}
      {message && <p style={{ color: '#15803d' }}>{message}</p>}

      <section style={{ marginBottom: 32 }}>
        <h2>Log production</h2>
        <form onSubmit={handleSubmitEntry}>
          <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
            <select value={employeeId} onChange={(e) => setEmployeeId(e.target.value ? Number(e.target.value) : '')} required>
              <option value="">Employee</option>
              {employees.map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.empId} — {emp.name}
                </option>
              ))}
            </select>
            <select
              value={jobId}
              onChange={(e) => {
                setJobId(e.target.value ? Number(e.target.value) : '');
                setColourId('');
                setSizeId('');
              }}
              required
            >
              <option value="">Job</option>
              {jobs.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.jobDisplayId} — {j.modelNo}
                </option>
              ))}
            </select>
            {selectedJob && (
              <select
                value={colourId}
                onChange={(e) => {
                  setColourId(e.target.value ? Number(e.target.value) : '');
                  setSizeId('');
                }}
              >
                <option value="">Colour</option>
                {selectedJob.colours.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            )}
            {selectedColour && (
              <select value={sizeId} onChange={(e) => setSizeId(e.target.value ? Number(e.target.value) : '')}>
                <option value="">Size</option>
                {selectedColour.sizes.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.size} (planned {s.plannedQty})
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
          </div>

          {opLines.map((line, i) => (
            <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
              <select
                value={line.workstationId}
                onChange={(e) => updateOpLine(opLines, setOpLines, i, { workstationId: e.target.value ? Number(e.target.value) : '', operationId: '' })}
              >
                <option value="">Workstation</option>
                {workstations.map((ws) => (
                  <option key={ws.id} value={ws.id}>
                    {ws.name}
                  </option>
                ))}
              </select>
              <select
                value={line.operationId}
                onChange={(e) => updateOpLine(opLines, setOpLines, i, { operationId: e.target.value ? Number(e.target.value) : '' })}
              >
                <option value="">Operation</option>
                {operations
                  .filter((op) => line.workstationId === '' || op.workstationId === line.workstationId)
                  .map((op) => (
                    <option key={op.id} value={op.id}>
                      {op.name}
                    </option>
                  ))}
              </select>
              <input
                type="number"
                min={1}
                placeholder="Qty"
                value={line.quantity}
                onChange={(e) => updateOpLine(opLines, setOpLines, i, { quantity: e.target.value })}
                style={{ width: 80 }}
              />
              {opLines.length > 1 && (
                <button type="button" onClick={() => setOpLines(opLines.filter((_, j) => j !== i))}>
                  Remove
                </button>
              )}
            </div>
          ))}
          <button type="button" onClick={() => setOpLines([...opLines, emptyOpLine()])} style={{ marginBottom: 12 }}>
            Add operation line
          </button>
          <div>
            <button type="submit">Save entry</button>
          </div>
        </form>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>Entries for {date}</h2>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={th}>Employee</th>
              <th style={th}>Job</th>
              <th style={th}>Colour/Size/Side</th>
              <th style={th}>Operations</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((entry) => (
              <tr key={entry.id}>
                <td style={td}>{entry.employeeName}</td>
                <td style={td}>{entry.jobDisplayId}</td>
                <td style={td}>
                  {[
                    jobs.find((j) => j.id === entry.jobId)?.colours.find((c) => c.id === entry.colourId)?.name,
                    jobs
                      .find((j) => j.id === entry.jobId)
                      ?.colours.flatMap((c) => c.sizes)
                      .find((s) => s.id === entry.sizeId)?.size,
                    entry.side,
                  ]
                    .filter(Boolean)
                    .join(' / ') || '—'}
                </td>
                <td style={td}>{entry.operations.map((op) => `${op.workstationName}/${op.operationName}: ${op.quantity}`).join(', ')}</td>
              </tr>
            ))}
            {entries.length === 0 && (
              <tr>
                <td style={td} colSpan={4}>
                  No entries for this date.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>

      <section>
        <h2>Edit requests</h2>
        <form onSubmit={handleSubmitEditRequest} style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
          <select
            value={editJobId}
            onChange={(e) => {
              setEditJobId(e.target.value ? Number(e.target.value) : '');
              setEditColourId('');
              setEditSizeId('');
            }}
            required
          >
            <option value="">Job</option>
            {jobs.map((j) => (
              <option key={j.id} value={j.id}>
                {j.jobDisplayId}
              </option>
            ))}
          </select>
          {editJob && (
            <select
              value={editColourId}
              onChange={(e) => {
                setEditColourId(e.target.value ? Number(e.target.value) : '');
                setEditSizeId('');
              }}
            >
              <option value="">Colour (optional)</option>
              {editJob.colours.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          )}
          {editColour && (
            <select value={editSizeId} onChange={(e) => setEditSizeId(e.target.value ? Number(e.target.value) : '')}>
              <option value="">Size (optional)</option>
              {editColour.sizes.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.size}
                </option>
              ))}
            </select>
          )}
          <input placeholder="Reason" value={editReason} onChange={(e) => setEditReason(e.target.value)} required style={{ minWidth: 200 }} />
          <button type="submit">Submit request</button>
        </form>

        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={th}>Job</th>
              <th style={th}>Requested by</th>
              <th style={th}>Reason</th>
              <th style={th}>Status</th>
              <th style={th}></th>
            </tr>
          </thead>
          <tbody>
            {editRequests.map((r) => (
              <tr key={r.id}>
                <td style={td}>{r.jobDisplayId}</td>
                <td style={td}>{r.requestedByUsername}</td>
                <td style={td}>{r.reason}</td>
                <td style={td}>{r.status}</td>
                <td style={td}>
                  {isAdmin && r.status === 'PENDING' && (
                    <>
                      <button onClick={() => handleApproveEdit(r.id)}>Approve</button>{' '}
                      <button onClick={() => handleRejectEdit(r.id)}>Reject</button>
                    </>
                  )}
                  {!isAdmin && r.status === 'APPROVED' && !r.used && (
                    <button onClick={() => startConsolidate(r)}>Consolidate</button>
                  )}
                </td>
              </tr>
            ))}
            {editRequests.length === 0 && (
              <tr>
                <td style={td} colSpan={5}>
                  No edit requests.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {consolidating && (
          <div style={{ border: '1px solid #eee', padding: 8, marginTop: 12 }}>
            <h3>
              Consolidate entry for {consolidating.jobDisplayId} (request #{consolidating.id})
            </h3>
            <form onSubmit={handleConsolidate}>
              <div style={{ marginBottom: 8 }}>
                <select value={consolidateEmployeeId} onChange={(e) => setConsolidateEmployeeId(e.target.value ? Number(e.target.value) : '')} required>
                  <option value="">Employee</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.empId} — {emp.name}
                    </option>
                  ))}
                </select>
              </div>
              {consolidateOpLines.map((line, i) => (
                <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
                  <select
                    value={line.workstationId}
                    onChange={(e) =>
                      updateOpLine(consolidateOpLines, setConsolidateOpLines, i, { workstationId: e.target.value ? Number(e.target.value) : '', operationId: '' })
                    }
                  >
                    <option value="">Workstation</option>
                    {workstations.map((ws) => (
                      <option key={ws.id} value={ws.id}>
                        {ws.name}
                      </option>
                    ))}
                  </select>
                  <select
                    value={line.operationId}
                    onChange={(e) => updateOpLine(consolidateOpLines, setConsolidateOpLines, i, { operationId: e.target.value ? Number(e.target.value) : '' })}
                  >
                    <option value="">Operation</option>
                    {operations
                      .filter((op) => line.workstationId === '' || op.workstationId === line.workstationId)
                      .map((op) => (
                        <option key={op.id} value={op.id}>
                          {op.name}
                        </option>
                      ))}
                  </select>
                  <input
                    type="number"
                    min={1}
                    placeholder="Qty"
                    value={line.quantity}
                    onChange={(e) => updateOpLine(consolidateOpLines, setConsolidateOpLines, i, { quantity: e.target.value })}
                    style={{ width: 80 }}
                  />
                </div>
              ))}
              <button type="button" onClick={() => setConsolidateOpLines([...consolidateOpLines, emptyOpLine()])}>
                Add operation line
              </button>
              <div style={{ marginTop: 8 }}>
                <button type="submit">Replace entry</button>{' '}
                <button type="button" onClick={() => setConsolidating(null)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}
      </section>
    </div>
  );
}
