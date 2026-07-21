import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  approveRoutingChangeRequest,
  findRoutingTemplate,
  getRoutingForJob,
  listJobs,
  listOperations,
  listRoutingChangeRequests,
  listWorkstations,
  rejectRoutingChangeRequest,
  saveRouting,
} from '../../api/production';
import type { Job, Operation, Routing, RoutingChangeRequest, Workstation } from '../../api/productionTypes';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };

interface OpRow {
  operationId: number | '';
  dependsOnOperationIds: number[];
}

interface WsRow {
  workstationId: number | '';
  operations: OpRow[];
}

function fromRouting(routing: Routing): WsRow[] {
  return routing.workstations.map((ws) => ({
    workstationId: ws.workstationId,
    operations: ws.operations.map((op) => ({ operationId: op.operationId, dependsOnOperationIds: op.dependsOnOperationIds })),
  }));
}

export default function RoutingPage() {
  const { auth } = useAuth();
  const isAdmin = auth?.role === 'ADMIN';

  const [jobs, setJobs] = useState<Job[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [operations, setOperations] = useState<Operation[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<number | ''>('');
  const [rows, setRows] = useState<WsRow[]>([]);
  const [existing, setExisting] = useState<Routing | null>(null);
  const [reason, setReason] = useState('');
  const [changeRequests, setChangeRequests] = useState<RoutingChangeRequest[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    listJobs(true).then(setJobs).catch(() => setError('Could not load jobs.'));
    listWorkstations().then(setWorkstations).catch(() => setError('Could not load workstations.'));
    listOperations().then(setOperations).catch(() => setError('Could not load operations.'));
    if (isAdmin) refreshChangeRequests();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAdmin]);

  function refreshChangeRequests() {
    listRoutingChangeRequests().then(setChangeRequests).catch(() => setError('Could not load routing change requests.'));
  }

  const selectedJob = jobs.find((j) => j.id === selectedJobId) ?? null;

  useEffect(() => {
    setError(null);
    setMessage(null);
    setReason('');
    if (selectedJobId === '') {
      setRows([]);
      setExisting(null);
      return;
    }
    getRoutingForJob(selectedJobId)
      .then((r) => {
        setExisting(r);
        setRows(fromRouting(r));
      })
      .catch(() => {
        setExisting(null);
        setRows([]);
      });
  }, [selectedJobId]);

  async function handleLoadTemplate() {
    if (!selectedJob) return;
    setError(null);
    try {
      const template = await findRoutingTemplate(selectedJob.styleCode, selectedJob.modelNo);
      if (template) {
        setRows(fromRouting(template));
        setMessage('Loaded routing template from a previous job with the same style/model.');
      } else {
        setMessage('No existing routing found for this style/model to use as a template.');
      }
    } catch {
      setError('Could not load routing template.');
    }
  }

  function addWorkstationRow() {
    setRows((prev) => [...prev, { workstationId: '', operations: [] }]);
  }

  function removeWorkstationRow(index: number) {
    setRows((prev) => prev.filter((_, i) => i !== index));
  }

  function updateWorkstationRow(index: number, workstationId: number) {
    setRows((prev) => prev.map((r, i) => (i === index ? { ...r, workstationId, operations: [] } : r)));
  }

  function addOperationRow(wsIndex: number) {
    setRows((prev) =>
      prev.map((r, i) => (i === wsIndex ? { ...r, operations: [...r.operations, { operationId: '', dependsOnOperationIds: [] }] } : r)),
    );
  }

  function removeOperationRow(wsIndex: number, opIndex: number) {
    setRows((prev) =>
      prev.map((r, i) => (i === wsIndex ? { ...r, operations: r.operations.filter((_, j) => j !== opIndex) } : r)),
    );
  }

  function updateOperationId(wsIndex: number, opIndex: number, operationId: number) {
    setRows((prev) =>
      prev.map((r, i) =>
        i === wsIndex ? { ...r, operations: r.operations.map((o, j) => (j === opIndex ? { ...o, operationId } : o)) } : r,
      ),
    );
  }

  function toggleDependency(wsIndex: number, opIndex: number, dependencyOperationId: number) {
    setRows((prev) =>
      prev.map((r, i) =>
        i === wsIndex
          ? {
              ...r,
              operations: r.operations.map((o, j) => {
                if (j !== opIndex) return o;
                const has = o.dependsOnOperationIds.includes(dependencyOperationId);
                return {
                  ...o,
                  dependsOnOperationIds: has
                    ? o.dependsOnOperationIds.filter((id) => id !== dependencyOperationId)
                    : [...o.dependsOnOperationIds, dependencyOperationId],
                };
              }),
            }
          : r,
      ),
    );
  }

  const allChosenOperationIds = rows.flatMap((r) => r.operations.map((o) => o.operationId).filter((id): id is number => id !== ''));

  function operationName(id: number): string {
    return operations.find((op) => op.id === id)?.name ?? `#${id}`;
  }

  async function handleSave() {
    if (selectedJobId === '') return;
    setError(null);
    setMessage(null);
    if (rows.length === 0 || rows.some((r) => r.workstationId === '' || r.operations.length === 0 || r.operations.some((o) => o.operationId === ''))) {
      setError('Every workstation needs at least one operation selected.');
      return;
    }
    if (!isAdmin && !reason.trim()) {
      setError('A reason is required when requesting a routing change.');
      return;
    }
    try {
      const result = await saveRouting(
        {
          jobId: selectedJobId,
          workstations: rows.map((r) => ({
            workstationId: r.workstationId as number,
            operations: r.operations.map((o) => ({
              operationId: o.operationId as number,
              dependsOnOperationIds: o.dependsOnOperationIds,
            })),
          })),
        },
        isAdmin ? undefined : reason,
      );
      if (result.routing) {
        setExisting(result.routing);
        setMessage('Routing saved.');
      } else {
        setMessage('Routing change submitted for admin approval.');
      }
    } catch {
      setError('Could not save routing.');
    }
  }

  async function handleApprove(id: number) {
    try {
      await approveRoutingChangeRequest(id);
      refreshChangeRequests();
    } catch {
      setError('Could not approve routing change request.');
    }
  }

  async function handleReject(id: number) {
    try {
      await rejectRoutingChangeRequest(id);
      refreshChangeRequests();
    } catch {
      setError('Could not reject routing change request.');
    }
  }

  return (
    <div>
      <h1>Routing</h1>
      {error && <p style={{ color: '#dc2626' }}>{error}</p>}
      {message && <p style={{ color: '#15803d' }}>{message}</p>}

      <div style={{ marginBottom: 16 }}>
        <select value={selectedJobId} onChange={(e) => setSelectedJobId(e.target.value ? Number(e.target.value) : '')}>
          <option value="">Select a job</option>
          {jobs.map((j) => (
            <option key={j.id} value={j.id}>
              {j.jobDisplayId} — {j.modelNo}
            </option>
          ))}
        </select>
        {selectedJob && (
          <button onClick={handleLoadTemplate} style={{ marginLeft: 8 }}>
            Load template from same style/model
          </button>
        )}
      </div>

      {selectedJobId !== '' && (
        <section style={{ marginBottom: 32 }}>
          <h2>{existing ? 'Edit routing' : 'New routing'}</h2>
          {rows.map((row, wsIndex) => (
            <div key={wsIndex} style={{ border: '1px solid #eee', padding: 8, marginBottom: 8 }}>
              <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                <select
                  value={row.workstationId}
                  onChange={(e) => updateWorkstationRow(wsIndex, Number(e.target.value))}
                >
                  <option value="">Select workstation</option>
                  {workstations.map((ws) => (
                    <option key={ws.id} value={ws.id}>
                      {ws.name}
                    </option>
                  ))}
                </select>
                <button type="button" onClick={() => removeWorkstationRow(wsIndex)}>
                  Remove workstation
                </button>
              </div>
              {row.operations.map((op, opIndex) => {
                const candidateDeps = allChosenOperationIds.filter((id) => id !== op.operationId);
                return (
                  <div key={opIndex} style={{ marginBottom: 8, paddingLeft: 16 }}>
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                      <select
                        value={op.operationId}
                        onChange={(e) => updateOperationId(wsIndex, opIndex, Number(e.target.value))}
                      >
                        <option value="">Select operation</option>
                        {operations
                          .filter((o) => row.workstationId === '' || o.workstationId === row.workstationId)
                          .map((o) => (
                            <option key={o.id} value={o.id}>
                              {o.name}
                            </option>
                          ))}
                      </select>
                      <button type="button" onClick={() => removeOperationRow(wsIndex, opIndex)}>
                        Remove operation
                      </button>
                    </div>
                    {candidateDeps.length > 0 && (
                      <div style={{ marginTop: 4 }}>
                        <span>Depends on: </span>
                        {candidateDeps.map((depId) => (
                          <label key={depId} style={{ marginRight: 12 }}>
                            <input
                              type="checkbox"
                              checked={op.dependsOnOperationIds.includes(depId)}
                              onChange={() => toggleDependency(wsIndex, opIndex, depId)}
                            />{' '}
                            {operationName(depId)}
                          </label>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })}
              <button type="button" onClick={() => addOperationRow(wsIndex)}>
                Add operation
              </button>
            </div>
          ))}
          <button type="button" onClick={addWorkstationRow} style={{ marginBottom: 12 }}>
            Add workstation
          </button>

          {!isAdmin && (
            <div style={{ marginBottom: 12 }}>
              <input
                placeholder="Reason for change (required)"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                style={{ width: 400 }}
              />
            </div>
          )}
          <div>
            <button onClick={handleSave}>{isAdmin ? 'Save routing' : 'Submit change request'}</button>
          </div>
        </section>
      )}

      {isAdmin && (
        <section>
          <h2>Pending routing change requests</h2>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={th}>Job</th>
                <th style={th}>Reason</th>
                <th style={th}>Requested by</th>
                <th style={th}></th>
              </tr>
            </thead>
            <tbody>
              {changeRequests.map((r) => (
                <tr key={r.id}>
                  <td style={td}>{r.jobDisplayId}</td>
                  <td style={td}>{r.reason}</td>
                  <td style={td}>{r.requestedByUsername}</td>
                  <td style={td}>
                    <button onClick={() => handleApprove(r.id)}>Approve</button>{' '}
                    <button onClick={() => handleReject(r.id)}>Reject</button>
                  </td>
                </tr>
              ))}
              {changeRequests.length === 0 && (
                <tr>
                  <td style={td} colSpan={4}>
                    No pending routing change requests.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </section>
      )}
    </div>
  );
}
