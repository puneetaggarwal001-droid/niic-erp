import { useEffect, useState } from 'react';
import { getEpr, type EprResponse } from '../api/reports';
import { listEmployees } from '../api/employees';
import type { Employee } from '../api/types';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };
const tdNum: React.CSSProperties = { ...td, textAlign: 'right' };

function inr(n: number): string {
  return `₹${n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

// First day of the current month, in local time, as YYYY-MM-DD.
function monthStart(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
}

function today(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
}

export default function EprPage() {
  const [from, setFrom] = useState(monthStart());
  const [to, setTo] = useState(today());
  const [employeeId, setEmployeeId] = useState<string>('');
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [report, setReport] = useState<EprResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listEmployees().then(setEmployees).catch(() => undefined);
  }, []);

  async function run() {
    setError(null);
    setLoading(true);
    try {
      const data = await getEpr(from, to, employeeId ? Number(employeeId) : undefined);
      setReport(data);
    } catch {
      setError('Could not load the report. Check the date range.');
    } finally {
      setLoading(false);
    }
  }

  // Only PC-rate employees earn piece-rate income, so the filter list is limited to them.
  const pcRateEmployees = employees.filter((e) => e.salaryType === 'PC_RATE');

  return (
    <div>
      <h2>Employee Production Report (EPR)</h2>
      <p style={{ color: '#666', fontSize: 14, marginTop: -8 }}>
        Piece-rate income = active PC rate (workstation × operation) × pieces logged. PC-rate employees only.
      </p>

      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-end', flexWrap: 'wrap', margin: '12px 0' }}>
        <label style={{ fontSize: 13 }}>
          From
          <br />
          <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} style={{ padding: 6 }} />
        </label>
        <label style={{ fontSize: 13 }}>
          To
          <br />
          <input type="date" value={to} onChange={(e) => setTo(e.target.value)} style={{ padding: 6 }} />
        </label>
        <label style={{ fontSize: 13 }}>
          Employee
          <br />
          <select value={employeeId} onChange={(e) => setEmployeeId(e.target.value)} style={{ padding: 6 }}>
            <option value="">All PC-rate employees</option>
            {pcRateEmployees.map((e) => (
              <option key={e.id} value={e.id}>
                {e.empId} — {e.name}
              </option>
            ))}
          </select>
        </label>
        <button onClick={run} disabled={loading}>
          {loading ? 'Loading…' : 'Run report'}
        </button>
      </div>

      {error && <p style={{ color: '#c0392b' }}>{error}</p>}

      {report && (
        <div>
          <div style={{ background: '#f4f6fb', border: '1px solid #dbe1f0', borderRadius: 6, padding: 12, marginBottom: 16 }}>
            <strong>{report.from} → {report.to}</strong>
            <span style={{ marginLeft: 16 }}>Total pieces: <strong>{report.grandTotalPieces}</strong></span>
            <span style={{ marginLeft: 16 }}>Total income: <strong>{inr(report.grandTotalIncome)}</strong></span>
          </div>

          {report.employees.length === 0 && <p>No piece-rate production in this range.</p>}

          {report.employees.map((emp) => (
            <div key={emp.employeeId} style={{ marginBottom: 24, border: '1px solid #e5e5e5', borderRadius: 6, padding: 12 }}>
              <h3 style={{ margin: '0 0 8px' }}>
                {emp.empId} — {emp.name}{' '}
                <span style={{ fontWeight: 400, color: '#555' }}>
                  ({emp.totalPieces} pcs, {inr(emp.totalIncome)})
                </span>
              </h3>

              <div style={{ display: 'flex', gap: 32, flexWrap: 'wrap' }}>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4 }}>By operation</div>
                  <table style={{ borderCollapse: 'collapse' }}>
                    <thead>
                      <tr>
                        <th style={th}>Workstation</th>
                        <th style={th}>Operation</th>
                        <th style={{ ...th, textAlign: 'right' }}>Rate</th>
                        <th style={{ ...th, textAlign: 'right' }}>Pcs</th>
                        <th style={{ ...th, textAlign: 'right' }}>Income</th>
                      </tr>
                    </thead>
                    <tbody>
                      {emp.operations.map((o, i) => (
                        <tr key={i}>
                          <td style={td}>{o.workstation}</td>
                          <td style={td}>{o.operation}</td>
                          <td style={tdNum}>{inr(o.rate)}</td>
                          <td style={tdNum}>{o.pieces}</td>
                          <td style={tdNum}>{inr(o.income)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div>
                  <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4 }}>By day</div>
                  <table style={{ borderCollapse: 'collapse' }}>
                    <thead>
                      <tr>
                        <th style={th}>Date</th>
                        <th style={{ ...th, textAlign: 'right' }}>Pcs</th>
                        <th style={{ ...th, textAlign: 'right' }}>Income</th>
                      </tr>
                    </thead>
                    <tbody>
                      {emp.days.map((d) => (
                        <tr key={d.date}>
                          <td style={td}>{d.date}</td>
                          <td style={tdNum}>{d.pieces}</td>
                          <td style={tdNum}>{inr(d.income)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
