import { useEffect, useState } from 'react';
import { getGatePassConfig, issueGatePass, listGatePasses, type GatePass } from '../api/gatePasses';
import { listEmployees } from '../api/employees';
import type { Employee } from '../api/types';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };

function today(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
}

function extractError(err: unknown, fallback: string): string {
  const anyErr = err as { response?: { data?: { message?: string } } };
  return anyErr?.response?.data?.message ?? fallback;
}

// Opens a print window with the three legacy copies: Employee, Gate Guard, HR.
function printPass(pass: GatePass) {
  const copies = ['Employee Copy', 'Gate Guard Copy', 'HR Copy'];
  const block = (copy: string) => `
    <div class="pass">
      <h2>NIIC — Gate Pass</h2>
      <div class="copy">${copy}</div>
      <table>
        <tr><td class="k">Pass #</td><td>${pass.id}</td></tr>
        <tr><td class="k">Employee</td><td>${pass.empId} — ${escapeHtml(pass.employeeName)}</td></tr>
        <tr><td class="k">Designation</td><td>${escapeHtml(pass.designationName ?? '-')}</td></tr>
        <tr><td class="k">Date</td><td>${pass.date}</td></tr>
        <tr><td class="k">Purpose</td><td>${escapeHtml(pass.purpose)}</td></tr>
        ${pass.penalty ? '<tr><td class="k">Note</td><td class="pen">Exceeds monthly limit — penalty</td></tr>' : ''}
      </table>
      <div class="sign">Authorised signature: ____________________</div>
    </div>`;
  const html = `<!doctype html><html><head><title>Gate Pass ${pass.id}</title><style>
    body { font-family: sans-serif; margin: 0; }
    .pass { border: 1px dashed #666; padding: 16px; margin: 12px; page-break-inside: avoid; }
    .copy { font-weight: bold; color: #1B2E72; margin-bottom: 8px; }
    table { border-collapse: collapse; }
    td { padding: 3px 8px; vertical-align: top; }
    td.k { font-weight: bold; width: 120px; }
    td.pen { color: #c0392b; font-weight: bold; }
    .sign { margin-top: 16px; }
    h2 { margin: 0 0 4px; }
  </style></head><body>${copies.map(block).join('')}</body></html>`;
  const w = window.open('', '_blank');
  if (w) {
    w.document.write(html);
    w.document.close();
    w.focus();
    w.print();
  }
}

function escapeHtml(s: string): string {
  return s.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]!));
}

export default function GatePassPage() {
  const [date, setDate] = useState(today());
  const [passes, setPasses] = useState<GatePass[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [employeeId, setEmployeeId] = useState('');
  const [purpose, setPurpose] = useState('');
  const [monthlyLimit, setMonthlyLimit] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  function refresh() {
    listGatePasses(date).then(setPasses).catch(() => setError('Could not load gate passes.'));
  }
  useEffect(() => {
    listEmployees().then(setEmployees).catch(() => undefined);
    getGatePassConfig().then((c) => setMonthlyLimit(c.monthlyLimit)).catch(() => undefined);
  }, []);
  useEffect(refresh, [date]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    try {
      const pass = await issueGatePass({ employeeId: Number(employeeId), date, purpose: purpose.trim() });
      setMessage(
        pass.penalty
          ? `Pass issued — but this exceeds the monthly limit of ${monthlyLimit}. Penalty flagged.`
          : 'Gate pass issued.',
      );
      setPurpose('');
      refresh();
      printPass(pass);
    } catch (err) {
      setError(extractError(err, 'Could not issue the gate pass.'));
    }
  }

  return (
    <div>
      <h2>Gate Pass</h2>
      {monthlyLimit !== null && (
        <p style={{ color: '#666', fontSize: 14, marginTop: -8 }}>
          Requires attendance on the date. Monthly limit: {monthlyLimit} passes — beyond that is penalty-flagged.
        </p>
      )}

      <form onSubmit={submit} style={{ display: 'flex', gap: 8, alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: 12 }}>
        <label style={{ fontSize: 13 }}>
          Date
          <br />
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ padding: 6 }} />
        </label>
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
        <label style={{ fontSize: 13, flex: 1, minWidth: 200 }}>
          Purpose
          <br />
          <input value={purpose} onChange={(e) => setPurpose(e.target.value)} required style={{ padding: 6, width: '100%' }} />
        </label>
        <button type="submit">Issue &amp; print</button>
      </form>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>#</th>
            <th style={th}>Employee</th>
            <th style={th}>Purpose</th>
            <th style={th}>Flag</th>
            <th style={th}>Issued by</th>
            <th style={th}></th>
          </tr>
        </thead>
        <tbody>
          {passes.map((p) => (
            <tr key={p.id}>
              <td style={td}>{p.id}</td>
              <td style={td}>
                {p.empId} — {p.employeeName}
              </td>
              <td style={td}>{p.purpose}</td>
              <td style={td}>{p.penalty ? <span style={{ color: '#c0392b' }}>Penalty</span> : 'OK'}</td>
              <td style={td}>{p.issuedByUsername ?? '—'}</td>
              <td style={td}>
                <button onClick={() => printPass(p)}>Print</button>
              </td>
            </tr>
          ))}
          {passes.length === 0 && (
            <tr>
              <td style={td} colSpan={6}>
                No gate passes on {date}.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
