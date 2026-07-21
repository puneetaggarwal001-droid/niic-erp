import { useEffect, useState } from 'react';
import {
  addAdvance,
  addContractor,
  createBill,
  deactivateContractor,
  finalizeBill,
  finalizeRun,
  generateRun,
  listAdvances,
  listBills,
  listContractors,
  listRuns,
  type Advance,
  type Contractor,
  type ContractorBill,
  type ContractorRateType,
  type PayrollRun,
} from '../api/payroll';
import { listEmployees } from '../api/employees';
import type { Employee } from '../api/types';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };
const tdNum: React.CSSProperties = { ...td, textAlign: 'right' };

function inr(n: number | null): string {
  if (n === null || n === undefined) return '—';
  return `₹${n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

function extractError(err: unknown, fallback: string): string {
  const anyErr = err as { response?: { data?: { message?: string } } };
  return anyErr?.response?.data?.message ?? fallback;
}

type Tab = 'advances' | 'runs' | 'contractors' | 'bills';

export default function PayrollPage() {
  const [tab, setTab] = useState<Tab>('runs');
  const tabs: { key: Tab; label: string }[] = [
    { key: 'runs', label: 'Payroll Run' },
    { key: 'advances', label: 'Advances' },
    { key: 'contractors', label: 'Contractors' },
    { key: 'bills', label: 'Contractor Bills' },
  ];

  return (
    <div>
      <h2>Payroll</h2>
      <div style={{ display: 'flex', gap: 8, borderBottom: '2px solid #eee', marginBottom: 16 }}>
        {tabs.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            style={{
              border: 'none',
              background: 'none',
              padding: '8px 12px',
              cursor: 'pointer',
              fontWeight: tab === t.key ? 700 : 400,
              borderBottom: tab === t.key ? '3px solid #1B2E72' : '3px solid transparent',
            }}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'runs' && <RunsTab />}
      {tab === 'advances' && <AdvancesTab />}
      {tab === 'contractors' && <ContractorsTab />}
      {tab === 'bills' && <BillsTab />}
    </div>
  );
}

function RunsTab() {
  const [month, setMonth] = useState(currentMonth());
  const [runs, setRuns] = useState<PayrollRun[]>([]);
  const [selected, setSelected] = useState<PayrollRun | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  function refresh() {
    listRuns().then(setRuns).catch(() => setError('Could not load runs.'));
  }
  useEffect(refresh, []);

  async function generate() {
    setError(null);
    setMessage(null);
    setBusy(true);
    try {
      const run = await generateRun(month);
      setSelected(run);
      setMessage(`Draft generated for ${month} (${run.lines.length} employees).`);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not generate the run.'));
    } finally {
      setBusy(false);
    }
  }

  async function doFinalize(run: PayrollRun) {
    setError(null);
    setMessage(null);
    try {
      const updated = await finalizeRun(run.id);
      setSelected(updated);
      setMessage(`Run for ${run.periodMonth} finalized.`);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not finalize.'));
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end', marginBottom: 12 }}>
        <label style={{ fontSize: 13 }}>
          Month
          <br />
          <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} style={{ padding: 6 }} />
        </label>
        <button onClick={generate} disabled={busy}>
          {busy ? 'Generating…' : 'Generate draft'}
        </button>
      </div>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
        <div>
          <h3 style={{ marginBottom: 4 }}>Runs</h3>
          <table style={{ borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={th}>Month</th>
                <th style={th}>Status</th>
                <th style={{ ...th, textAlign: 'right' }}>Net</th>
                <th style={th}></th>
              </tr>
            </thead>
            <tbody>
              {runs.map((r) => (
                <tr key={r.id}>
                  <td style={td}>{r.periodMonth}</td>
                  <td style={td}>{r.status}</td>
                  <td style={tdNum}>{inr(r.totalNet)}</td>
                  <td style={td}>
                    <button onClick={() => setSelected(r)}>View</button>
                  </td>
                </tr>
              ))}
              {runs.length === 0 && (
                <tr>
                  <td style={td} colSpan={4}>
                    No runs yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {selected && (
        <div style={{ marginTop: 24 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <h3 style={{ margin: 0 }}>
              {selected.periodMonth} — {selected.status}
            </h3>
            {selected.status === 'DRAFT' && (
              <button onClick={() => doFinalize(selected)} style={{ color: '#1e7e34' }}>
                Finalize
              </button>
            )}
            <span>Gross {inr(selected.totalGross)}</span>
            <span>Advances {inr(selected.totalAdvances)}</span>
            <span>
              <strong>Net {inr(selected.totalNet)}</strong>
            </span>
          </div>
          <table style={{ borderCollapse: 'collapse', marginTop: 8, width: '100%' }}>
            <thead>
              <tr>
                <th style={th}>Employee</th>
                <th style={th}>Type</th>
                <th style={{ ...th, textAlign: 'right' }}>Present</th>
                <th style={{ ...th, textAlign: 'right' }}>OT min</th>
                <th style={{ ...th, textAlign: 'right' }}>Pieces</th>
                <th style={{ ...th, textAlign: 'right' }}>Gross</th>
                <th style={{ ...th, textAlign: 'right' }}>Advances</th>
                <th style={{ ...th, textAlign: 'right' }}>Net</th>
              </tr>
            </thead>
            <tbody>
              {selected.lines.map((l) => (
                <tr key={l.employeeId}>
                  <td style={td}>
                    {l.empId} — {l.name}
                  </td>
                  <td style={td}>{l.salaryType}</td>
                  <td style={tdNum}>{l.presentDays}</td>
                  <td style={tdNum}>{l.overtimeMinutes}</td>
                  <td style={tdNum}>{l.salaryType === 'PC_RATE' ? l.totalPieces : '—'}</td>
                  <td style={tdNum}>{inr(l.grossPay)}</td>
                  <td style={tdNum}>{inr(l.advancesDeducted)}</td>
                  <td style={tdNum}>{inr(l.netPay)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function AdvancesTab() {
  const [month, setMonth] = useState(currentMonth());
  const [advances, setAdvances] = useState<Advance[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [employeeId, setEmployeeId] = useState('');
  const [amount, setAmount] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  function refresh() {
    listAdvances(month).then(setAdvances).catch(() => setError('Could not load advances.'));
  }
  useEffect(() => {
    listEmployees().then(setEmployees).catch(() => undefined);
  }, []);
  useEffect(refresh, [month]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    try {
      await addAdvance({
        employeeId: Number(employeeId),
        periodMonth: month,
        amount: Number(amount),
        reason: reason || undefined,
      });
      setMessage('Advance recorded.');
      setAmount('');
      setReason('');
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not record advance.'));
    }
  }

  return (
    <div>
      <div style={{ marginBottom: 12 }}>
        <label style={{ fontSize: 13 }}>
          Month{' '}
          <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} style={{ padding: 6 }} />
        </label>
      </div>
      <form onSubmit={submit} style={{ display: 'flex', gap: 8, alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: 12 }}>
        <label style={{ fontSize: 13 }}>
          Employee
          <br />
          <select value={employeeId} onChange={(e) => setEmployeeId(e.target.value)} required style={{ padding: 6 }}>
            <option value="">Select…</option>
            {employees.map((e) => (
              <option key={e.id} value={e.id}>
                {e.empId} — {e.name}
              </option>
            ))}
          </select>
        </label>
        <label style={{ fontSize: 13 }}>
          Amount
          <br />
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
            style={{ padding: 6, width: 110 }}
          />
        </label>
        <label style={{ fontSize: 13 }}>
          Reason
          <br />
          <input value={reason} onChange={(e) => setReason(e.target.value)} style={{ padding: 6 }} />
        </label>
        <button type="submit">Add advance</button>
      </form>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>Employee</th>
            <th style={{ ...th, textAlign: 'right' }}>Amount</th>
            <th style={th}>Reason</th>
            <th style={th}>Status</th>
          </tr>
        </thead>
        <tbody>
          {advances.map((a) => (
            <tr key={a.id}>
              <td style={td}>
                {a.empId} — {a.employeeName}
              </td>
              <td style={tdNum}>{inr(a.amount)}</td>
              <td style={td}>{a.reason ?? '—'}</td>
              <td style={td}>{a.deducted ? 'Deducted' : 'Pending'}</td>
            </tr>
          ))}
          {advances.length === 0 && (
            <tr>
              <td style={td} colSpan={4}>
                No advances for {month}.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function ContractorsTab() {
  const [contractors, setContractors] = useState<Contractor[]>([]);
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [error, setError] = useState<string | null>(null);

  function refresh() {
    listContractors().then(setContractors).catch(() => setError('Could not load contractors.'));
  }
  useEffect(refresh, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await addContractor(name.trim(), phone || undefined);
      setName('');
      setPhone('');
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not add contractor.'));
    }
  }

  async function remove(c: Contractor) {
    setError(null);
    try {
      await deactivateContractor(c.id);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not deactivate.'));
    }
  }

  return (
    <div>
      <form onSubmit={submit} style={{ display: 'flex', gap: 8, alignItems: 'flex-end', marginBottom: 12 }}>
        <label style={{ fontSize: 13 }}>
          Name
          <br />
          <input value={name} onChange={(e) => setName(e.target.value)} required style={{ padding: 6 }} />
        </label>
        <label style={{ fontSize: 13 }}>
          Phone
          <br />
          <input value={phone} onChange={(e) => setPhone(e.target.value)} style={{ padding: 6 }} />
        </label>
        <button type="submit">Add contractor</button>
      </form>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      <table style={{ borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th style={th}>Name</th>
            <th style={th}>Phone</th>
            <th style={th}></th>
          </tr>
        </thead>
        <tbody>
          {contractors.map((c) => (
            <tr key={c.id}>
              <td style={td}>{c.name}</td>
              <td style={td}>{c.phone ?? '—'}</td>
              <td style={td}>
                <button onClick={() => remove(c)} style={{ color: '#c0392b' }}>
                  Deactivate
                </button>
              </td>
            </tr>
          ))}
          {contractors.length === 0 && (
            <tr>
              <td style={td} colSpan={3}>
                No contractors yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function BillsTab() {
  const [month, setMonth] = useState(currentMonth());
  const [bills, setBills] = useState<ContractorBill[]>([]);
  const [contractors, setContractors] = useState<Contractor[]>([]);
  const [contractorId, setContractorId] = useState('');
  const [rateType, setRateType] = useState<ContractorRateType>('PER_OPERATION');
  const [quantity, setQuantity] = useState('');
  const [rate, setRate] = useState('');
  const [advances, setAdvances] = useState('');
  const [notes, setNotes] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  function refresh() {
    listBills(month).then(setBills).catch(() => setError('Could not load bills.'));
  }
  useEffect(() => {
    listContractors().then(setContractors).catch(() => undefined);
  }, []);
  useEffect(refresh, [month]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    try {
      await createBill({
        contractorId: Number(contractorId),
        periodMonth: month,
        rateType,
        quantity: Number(quantity),
        rate: Number(rate),
        advances: advances ? Number(advances) : undefined,
        notes: notes || undefined,
      });
      setMessage('Bill created.');
      setQuantity('');
      setRate('');
      setAdvances('');
      setNotes('');
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not create bill.'));
    }
  }

  async function doFinalize(b: ContractorBill) {
    setError(null);
    try {
      await finalizeBill(b.id);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not finalize bill.'));
    }
  }

  return (
    <div>
      <div style={{ marginBottom: 12 }}>
        <label style={{ fontSize: 13 }}>
          Month{' '}
          <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} style={{ padding: 6 }} />
        </label>
      </div>
      <form onSubmit={submit} style={{ display: 'flex', gap: 8, alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: 12 }}>
        <label style={{ fontSize: 13 }}>
          Contractor
          <br />
          <select value={contractorId} onChange={(e) => setContractorId(e.target.value)} required style={{ padding: 6 }}>
            <option value="">Select…</option>
            {contractors.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <label style={{ fontSize: 13 }}>
          Rate type
          <br />
          <select value={rateType} onChange={(e) => setRateType(e.target.value as ContractorRateType)} style={{ padding: 6 }}>
            <option value="PER_OPERATION">Per operation</option>
            <option value="FINISHED_PIECES">Finished pieces</option>
          </select>
        </label>
        <label style={{ fontSize: 13 }}>
          Quantity
          <br />
          <input type="number" min="0" step="0.01" value={quantity} onChange={(e) => setQuantity(e.target.value)} required style={{ padding: 6, width: 90 }} />
        </label>
        <label style={{ fontSize: 13 }}>
          Rate
          <br />
          <input type="number" min="0" step="0.01" value={rate} onChange={(e) => setRate(e.target.value)} required style={{ padding: 6, width: 90 }} />
        </label>
        <label style={{ fontSize: 13 }}>
          Advances
          <br />
          <input type="number" min="0" step="0.01" value={advances} onChange={(e) => setAdvances(e.target.value)} style={{ padding: 6, width: 90 }} />
        </label>
        <label style={{ fontSize: 13 }}>
          Notes
          <br />
          <input value={notes} onChange={(e) => setNotes(e.target.value)} style={{ padding: 6 }} />
        </label>
        <button type="submit">Create bill</button>
      </form>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>Contractor</th>
            <th style={th}>Rate type</th>
            <th style={{ ...th, textAlign: 'right' }}>Qty</th>
            <th style={{ ...th, textAlign: 'right' }}>Rate</th>
            <th style={{ ...th, textAlign: 'right' }}>Amount</th>
            <th style={{ ...th, textAlign: 'right' }}>Advances</th>
            <th style={{ ...th, textAlign: 'right' }}>Net</th>
            <th style={th}>Status</th>
            <th style={th}></th>
          </tr>
        </thead>
        <tbody>
          {bills.map((b) => (
            <tr key={b.id}>
              <td style={td}>{b.contractorName}</td>
              <td style={td}>{b.rateType === 'PER_OPERATION' ? 'Per op' : 'Finished pcs'}</td>
              <td style={tdNum}>{b.quantity}</td>
              <td style={tdNum}>{inr(b.rate)}</td>
              <td style={tdNum}>{inr(b.amount)}</td>
              <td style={tdNum}>{inr(b.advancesDeducted)}</td>
              <td style={tdNum}>{inr(b.netPayable)}</td>
              <td style={td}>{b.status}</td>
              <td style={td}>
                {b.status === 'DRAFT' && (
                  <button onClick={() => doFinalize(b)} style={{ color: '#1e7e34' }}>
                    Finalize
                  </button>
                )}
              </td>
            </tr>
          ))}
          {bills.length === 0 && (
            <tr>
              <td style={td} colSpan={9}>
                No bills for {month}.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
