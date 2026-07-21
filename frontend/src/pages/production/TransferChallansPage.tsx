import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  createTransferChallan,
  listJobs,
  listTransferChallans,
  listWorkstations,
  receiveTransferChallan,
  rejectTransferChallan,
} from '../../api/production';
import type { ChallanStatus, Job, TransferChallan, Workstation } from '../../api/productionTypes';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };

function errorMessage(err: unknown, fallback: string): string {
  const maybe = err as { response?: { data?: { message?: string } } };
  return maybe?.response?.data?.message ?? fallback;
}

interface ItemRow {
  itemName: string;
  itemUnit: string;
  qty: string;
}

function emptyItem(): ItemRow {
  return { itemName: '', itemUnit: 'PCS', qty: '' };
}

export default function TransferChallansPage() {
  const { auth } = useAuth();
  const canReceive = auth?.role === 'ADMIN' || auth?.role === 'STORE_ADMIN';

  const [jobs, setJobs] = useState<Job[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [status, setStatus] = useState<ChallanStatus>('PENDING');
  const [challans, setChallans] = useState<TransferChallan[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [jobId, setJobId] = useState<number | ''>('');
  const [fromWorkstationId, setFromWorkstationId] = useState<number | ''>('');
  const [toWorkstationId, setToWorkstationId] = useState<number | ''>('');
  const [remarks, setRemarks] = useState('');
  const [items, setItems] = useState<ItemRow[]>([emptyItem()]);
  const [rejectReasons, setRejectReasons] = useState<Record<number, string>>({});

  function refresh() {
    listTransferChallans(status).then(setChallans).catch(() => setError('Could not load transfer challans.'));
  }

  useEffect(() => {
    listJobs(true).then(setJobs).catch(() => setError('Could not load jobs.'));
    listWorkstations().then(setWorkstations).catch(() => setError('Could not load workstations.'));
  }, []);

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status]);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (jobId === '' || fromWorkstationId === '' || toWorkstationId === '') return;
    try {
      await createTransferChallan({
        jobId,
        fromWorkstationId,
        toWorkstationId,
        remarks: remarks || undefined,
        items: items
          .filter((i) => i.itemName && i.itemUnit && i.qty)
          .map((i) => ({ itemName: i.itemName, itemUnit: i.itemUnit, qty: Number(i.qty) })),
      });
      setMessage('Transfer challan created.');
      setRemarks('');
      setItems([emptyItem()]);
      refresh();
    } catch (err) {
      setError(errorMessage(err, 'Could not create transfer challan.'));
    }
  }

  async function handleReceive(id: number) {
    setError(null);
    try {
      await receiveTransferChallan(id);
      refresh();
    } catch (err) {
      setError(errorMessage(err, 'Could not receive transfer challan.'));
    }
  }

  async function handleReject(id: number) {
    const reason = rejectReasons[id];
    if (!reason) {
      setError('Enter a rejection reason first.');
      return;
    }
    setError(null);
    try {
      await rejectTransferChallan(id, reason);
      refresh();
    } catch (err) {
      setError(errorMessage(err, 'Could not reject transfer challan.'));
    }
  }

  return (
    <div>
      <h1>Transfer Challans</h1>
      {error && <p style={{ color: '#dc2626' }}>{error}</p>}
      {message && <p style={{ color: '#15803d' }}>{message}</p>}

      <section style={{ marginBottom: 32 }}>
        <h2>Create transfer challan</h2>
        <form onSubmit={handleCreate}>
          <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
            <select value={jobId} onChange={(e) => setJobId(e.target.value ? Number(e.target.value) : '')} required>
              <option value="">Job</option>
              {jobs.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.jobDisplayId}
                </option>
              ))}
            </select>
            <select value={fromWorkstationId} onChange={(e) => setFromWorkstationId(e.target.value ? Number(e.target.value) : '')} required>
              <option value="">From workstation</option>
              {workstations.map((ws) => (
                <option key={ws.id} value={ws.id}>
                  {ws.name}
                </option>
              ))}
            </select>
            <select value={toWorkstationId} onChange={(e) => setToWorkstationId(e.target.value ? Number(e.target.value) : '')} required>
              <option value="">To workstation</option>
              {workstations.map((ws) => (
                <option key={ws.id} value={ws.id}>
                  {ws.name}
                </option>
              ))}
            </select>
            <input placeholder="Remarks (optional)" value={remarks} onChange={(e) => setRemarks(e.target.value)} style={{ minWidth: 200 }} />
          </div>

          {items.map((item, i) => (
            <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
              <input
                placeholder="Item name"
                value={item.itemName}
                onChange={(e) => setItems(items.map((it, j) => (j === i ? { ...it, itemName: e.target.value } : it)))}
                required
              />
              <input
                placeholder="Unit"
                value={item.itemUnit}
                onChange={(e) => setItems(items.map((it, j) => (j === i ? { ...it, itemUnit: e.target.value } : it)))}
                style={{ width: 80 }}
                required
              />
              <input
                type="number"
                min={0.01}
                step="0.01"
                placeholder="Qty"
                value={item.qty}
                onChange={(e) => setItems(items.map((it, j) => (j === i ? { ...it, qty: e.target.value } : it)))}
                style={{ width: 100 }}
                required
              />
              {items.length > 1 && (
                <button type="button" onClick={() => setItems(items.filter((_, j) => j !== i))}>
                  Remove
                </button>
              )}
            </div>
          ))}
          <button type="button" onClick={() => setItems([...items, emptyItem()])} style={{ marginBottom: 12 }}>
            Add item
          </button>
          <div>
            <button type="submit">Create challan</button>
          </div>
        </form>
      </section>

      <section>
        <h2>Challans</h2>
        <div style={{ marginBottom: 12 }}>
          <select value={status} onChange={(e) => setStatus(e.target.value as ChallanStatus)}>
            <option value="PENDING">Pending</option>
            <option value="RECEIVED">Received</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={th}>Challan no.</th>
              <th style={th}>Job</th>
              <th style={th}>From → To</th>
              <th style={th}>Items</th>
              <th style={th}>Status</th>
              {canReceive && <th style={th}></th>}
            </tr>
          </thead>
          <tbody>
            {challans.map((c) => (
              <tr key={c.id}>
                <td style={td}>{c.challanNo}</td>
                <td style={td}>{c.jobDisplayId}</td>
                <td style={td}>
                  {c.fromWorkstationName} → {c.toWorkstationName}
                </td>
                <td style={td}>{c.items.map((i) => `${i.itemName} (${i.qty} ${i.itemUnit})`).join(', ')}</td>
                <td style={td}>{c.status}</td>
                {canReceive && (
                  <td style={td}>
                    {c.status === 'PENDING' && (
                      <div style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                        <button onClick={() => handleReceive(c.id)}>Receive</button>
                        <input
                          placeholder="Rejection reason"
                          value={rejectReasons[c.id] ?? ''}
                          onChange={(e) => setRejectReasons((prev) => ({ ...prev, [c.id]: e.target.value }))}
                          style={{ width: 140 }}
                        />
                        <button onClick={() => handleReject(c.id)}>Reject</button>
                      </div>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {challans.length === 0 && (
              <tr>
                <td style={td} colSpan={canReceive ? 6 : 5}>
                  No transfer challans.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}
