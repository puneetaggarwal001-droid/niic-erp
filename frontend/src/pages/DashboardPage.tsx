import { useEffect, useRef, useState } from 'react';
import { exportBackup, getDashboard, restoreBackup, type Dashboard } from '../api/dashboard';
import { STATUS_COLOUR, STATUS_LABEL, type SampleStatus } from '../api/sampling';

function extractError(err: unknown, fallback: string): string {
  const anyErr = err as { response?: { data?: { message?: string } } };
  return anyErr?.response?.data?.message ?? fallback;
}

const cardStyle: React.CSSProperties = {
  border: '1px solid #e5e7eb',
  borderRadius: 10,
  padding: '14px 16px',
  minWidth: 130,
  flex: '1 1 130px',
  background: '#fff',
};

function Kpi({ label, value, accent, hint }: { label: string; value: number | string; accent?: string; hint?: string }) {
  return (
    <div style={cardStyle}>
      <div style={{ fontSize: 12, color: '#6b7280', textTransform: 'uppercase', letterSpacing: 0.4 }}>{label}</div>
      <div style={{ fontSize: 28, fontWeight: 700, color: accent ?? '#1B2E72', lineHeight: 1.2 }}>{value}</div>
      {hint && <div style={{ fontSize: 12, color: '#9ca3af' }}>{hint}</div>}
    </div>
  );
}

function Section({ title, colour, children }: { title: string; colour: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: 24 }}>
      <h3 style={{ margin: '0 0 10px', borderLeft: `4px solid ${colour}`, paddingLeft: 8 }}>{title}</h3>
      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>{children}</div>
    </div>
  );
}

/** Highlight a "pending / needs attention" number in amber when non-zero. */
function attention(n: number): string | undefined {
  return n > 0 ? '#b8860b' : undefined;
}

export default function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getDashboard().then(setData).catch((err) => setError(extractError(err, 'Could not load dashboard.')));
  }, []);

  if (error) return <p style={{ color: '#c0392b' }}>{error}</p>;
  if (!data) return <p>Loading…</p>;

  const d = data;
  const pipelineOrder: SampleStatus[] = ['DRAFT', 'IN_REVIEW', 'SELECTED', 'PPS_DONE', 'PPM_DONE', 'REJECTED', 'CLOSED'];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
        <h2 style={{ margin: 0 }}>Management Dashboard</h2>
        <button onClick={() => window.print()}>🖨 Print</button>
      </div>
      <p style={{ color: '#9ca3af', fontSize: 13, marginTop: 4 }}>Live snapshot across all modules.</p>

      <Section title="Workforce" colour="#1B2E72">
        <Kpi label="Employees" value={d.hr.activeEmployees} hint={`${d.hr.totalEmployees} total`} />
        <Kpi label="Present today" value={d.hr.presentToday} accent="#1e7e34" />
        <Kpi label="Gate passes today" value={d.hr.gatePassesToday} accent={attention(d.hr.gatePassesToday)} />
      </Section>

      <Section title="Production" colour="#0e7490">
        <Kpi label="Active jobs" value={d.production.activeJobs} />
        <Kpi label="Units today" value={d.production.unitsToday} accent="#1e7e34" />
        <Kpi label="Job requests" value={d.production.pendingJobRequests} accent={attention(d.production.pendingJobRequests)} hint="pending" />
        <Kpi label="Edit requests" value={d.production.pendingEditRequests} accent={attention(d.production.pendingEditRequests)} hint="pending" />
        <Kpi label="Routing changes" value={d.production.pendingRoutingRequests} accent={attention(d.production.pendingRoutingRequests)} hint="pending" />
        <Kpi label="QC to fill" value={d.production.pendingQc} accent={attention(d.production.pendingQc)} />
        <Kpi label="Transfers" value={d.production.pendingTransfers} accent={attention(d.production.pendingTransfers)} hint="pending receipt" />
      </Section>

      <Section title="Store & Inventory" colour="#b45309">
        <Kpi label="Items" value={d.store.totalItems} />
        <Kpi label="Item approvals" value={d.store.pendingItemApprovals} accent={attention(d.store.pendingItemApprovals)} hint="pending" />
        <Kpi label="Below reorder" value={d.store.belowReorder} accent={d.store.belowReorder > 0 ? '#c0392b' : undefined} />
        <Kpi label="Open POs" value={d.store.openPurchaseOrders} />
        <Kpi label="Requisitions" value={d.store.pendingRequisitions} accent={attention(d.store.pendingRequisitions)} hint="unfulfilled" />
      </Section>

      <Section title="Sampling" colour="#7c3aed">
        <Kpi label="Active samples" value={d.sampling.activeSamples} />
        <Kpi label="Requests" value={d.sampling.pendingRequests} accent={attention(d.sampling.pendingRequests)} hint="open" />
      </Section>
      <div style={{ marginTop: -12, marginBottom: 24 }}>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {pipelineOrder.map((st) => (
            <div
              key={st}
              style={{
                border: `1px solid ${STATUS_COLOUR[st]}`,
                borderRadius: 8,
                padding: '4px 10px',
                fontSize: 13,
                color: STATUS_COLOUR[st],
                background: '#fff',
              }}
            >
              {STATUS_LABEL[st]}: <strong>{d.sampling.pipeline[st] ?? 0}</strong>
            </div>
          ))}
        </div>
      </div>

      <Section title="Payroll" colour="#334155">
        <Kpi
          label="Latest run"
          value={d.payroll.lastRunMonth ?? '—'}
          hint={d.payroll.lastRunStatus ? d.payroll.lastRunStatus.toLowerCase() : 'no runs yet'}
        />
      </Section>

      <BackupPanel />
    </div>
  );
}

function BackupPanel() {
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);

  function today(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  }

  async function download() {
    setError(null);
    setMessage(null);
    setBusy(true);
    try {
      const blob = await exportBackup();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `niic-erp-backup-${today()}.json`;
      a.click();
      URL.revokeObjectURL(url);
      setMessage('Backup downloaded.');
    } catch (err) {
      setError(extractError(err, 'Could not export backup.'));
    } finally {
      setBusy(false);
    }
  }

  async function onRestoreFile(file: File | undefined) {
    if (!file) return;
    setError(null);
    setMessage(null);
    let snapshot: unknown;
    try {
      snapshot = JSON.parse(await file.text());
    } catch {
      setError('That file is not valid JSON.');
      return;
    }
    const ok = window.confirm(
      'Restore will DELETE all current data and replace it with the contents of this backup. This cannot be undone. Continue?',
    );
    if (!ok) return;
    setBusy(true);
    try {
      const result = await restoreBackup(snapshot);
      setMessage(`Restored ${result.restoredRows} rows across ${result.restoredTables} tables. Reloading…`);
      setTimeout(() => window.location.reload(), 1500);
    } catch (err) {
      setError(extractError(err, 'Could not restore backup.'));
    } finally {
      setBusy(false);
      if (fileRef.current) fileRef.current.value = '';
    }
  }

  return (
    <div style={{ marginTop: 12, border: '1px solid #e5e7eb', borderRadius: 10, padding: 16 }} className="no-print">
      <h3 style={{ margin: '0 0 8px' }}>Backup &amp; Restore</h3>
      <p style={{ fontSize: 13, color: '#6b7280', marginTop: 0 }}>
        Export a full JSON snapshot of the database, or restore from one. Restore replaces all existing data.
      </p>
      <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
        <button onClick={download} disabled={busy}>
          {busy ? 'Working…' : '⬇ Export backup'}
        </button>
        <button onClick={() => fileRef.current?.click()} disabled={busy} style={{ color: '#c0392b' }}>
          ⬆ Restore from file…
        </button>
        <input
          ref={fileRef}
          type="file"
          accept="application/json,.json"
          style={{ display: 'none' }}
          onChange={(e) => onRestoreFile(e.target.files?.[0])}
        />
      </div>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}
    </div>
  );
}
