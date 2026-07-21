import { useEffect, useState } from 'react';
import {
  getRejects,
  getWip,
  type JobRejects,
  type JobWip,
  type RejectsResponse,
  type WipResponse,
} from '../../api/reports';

function extractError(err: unknown, fallback: string): string {
  const anyErr = err as { response?: { data?: { message?: string } } };
  return anyErr?.response?.data?.message ?? fallback;
}

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };
const thNum: React.CSSProperties = { ...th, textAlign: 'right' };
const tdNum: React.CSSProperties = { ...td, textAlign: 'right' };

function monthStart(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
}

function today(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
}

type Tab = 'wip' | 'rejects';

export default function ProductionReportsPage() {
  const [tab, setTab] = useState<Tab>('wip');
  const tabStyle = (t: Tab): React.CSSProperties => ({
    padding: '6px 14px',
    border: '1px solid #ccc',
    borderBottom: tab === t ? '2px solid #1B2E72' : '1px solid #ccc',
    background: tab === t ? '#eef1fa' : '#fff',
    fontWeight: tab === t ? 600 : 400,
    cursor: 'pointer',
  });

  return (
    <div>
      <h2>Production Reports</h2>
      <div style={{ display: 'flex', gap: 4, marginBottom: 16 }}>
        <button style={tabStyle('wip')} onClick={() => setTab('wip')}>
          Work in Progress
        </button>
        <button style={tabStyle('rejects')} onClick={() => setTab('rejects')}>
          Rejects
        </button>
      </div>
      {tab === 'wip' ? <WipTab /> : <RejectsTab />}
    </div>
  );
}

// ---- WIP ----

function WipTab() {
  const [data, setData] = useState<WipResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [openJob, setOpenJob] = useState<number | null>(null);

  useEffect(() => {
    getWip().then(setData).catch((err) => setError(extractError(err, 'Could not load the WIP report.')));
  }, []);

  if (error) return <p style={{ color: '#c0392b' }}>{error}</p>;
  if (!data) return <p>Loading…</p>;

  return (
    <div>
      <p style={{ color: '#666', fontSize: 14, marginTop: 0 }}>
        Per active job: pieces logged in production entries vs pieces accounted for by operation closures. Open =
        produced − closed.
      </p>
      <div style={{ background: '#f4f6fb', border: '1px solid #dbe1f0', borderRadius: 6, padding: 12, marginBottom: 16 }}>
        Total open WIP across active jobs: <strong>{data.totalOpen}</strong> pcs
      </div>
      {data.jobs.length === 0 && <p>No active jobs.</p>}
      {data.jobs.length > 0 && (
        <table style={{ borderCollapse: 'collapse', minWidth: 560 }}>
          <thead>
            <tr>
              <th style={th}>Job</th>
              <th style={th}>Model</th>
              <th style={thNum}>Planned</th>
              <th style={thNum}>Produced</th>
              <th style={thNum}>Closed</th>
              <th style={thNum}>Open</th>
              <th style={th} />
            </tr>
          </thead>
          <tbody>
            {data.jobs.map((j) => (
              <WipJobRows key={j.jobId} job={j} open={openJob === j.jobId} onToggle={() => setOpenJob(openJob === j.jobId ? null : j.jobId)} />
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function WipJobRows({ job, open, onToggle }: { job: JobWip; open: boolean; onToggle: () => void }) {
  return (
    <>
      <tr>
        <td style={td}>
          <strong>{job.jobDisplayId}</strong>
        </td>
        <td style={td}>{job.modelNo}</td>
        <td style={tdNum}>{job.plannedQty}</td>
        <td style={tdNum}>{job.produced}</td>
        <td style={tdNum}>{job.closed}</td>
        <td style={{ ...tdNum, fontWeight: 700, color: job.open > 0 ? '#b8860b' : '#1e7e34' }}>{job.open}</td>
        <td style={td}>
          <button onClick={onToggle} disabled={job.operations.length === 0}>
            {open ? 'Hide ops' : 'Ops'}
          </button>
        </td>
      </tr>
      {open && (
        <tr>
          <td colSpan={7} style={{ ...td, background: '#fafbff', padding: '8px 24px' }}>
            <table style={{ borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={th}>Workstation</th>
                  <th style={th}>Operation</th>
                  <th style={thNum}>Produced</th>
                  <th style={thNum}>Closed</th>
                  <th style={thNum}>Open</th>
                </tr>
              </thead>
              <tbody>
                {job.operations.map((o, i) => (
                  <tr key={i}>
                    <td style={td}>{o.workstation}</td>
                    <td style={td}>{o.operation}</td>
                    <td style={tdNum}>{o.produced}</td>
                    <td style={tdNum}>{o.closed}</td>
                    <td style={{ ...tdNum, fontWeight: 600 }}>{o.open}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </td>
        </tr>
      )}
    </>
  );
}

// ---- Rejects ----

function RejectsTab() {
  const [from, setFrom] = useState(monthStart());
  const [to, setTo] = useState(today());
  const [data, setData] = useState<RejectsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [openJob, setOpenJob] = useState<number | null>(null);

  async function run() {
    setError(null);
    setLoading(true);
    try {
      setData(await getRejects(from, to));
      setOpenJob(null);
    } catch (err) {
      setError(extractError(err, 'Could not load the rejects report. Check the date range.'));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <p style={{ color: '#666', fontSize: 14, marginTop: 0 }}>
        QC outcomes per job over a period: checked, passed, sent to alter, rejected outright, plus rejects out of
        rework.
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
        <button onClick={run} disabled={loading}>
          {loading ? 'Loading…' : 'Run report'}
        </button>
      </div>

      {error && <p style={{ color: '#c0392b' }}>{error}</p>}

      {data && (
        <div>
          <div style={{ background: '#f4f6fb', border: '1px solid #dbe1f0', borderRadius: 6, padding: 12, marginBottom: 16 }}>
            <strong>
              {data.from} → {data.to}
            </strong>
            <span style={{ marginLeft: 16 }}>
              Checked: <strong>{data.totalChecked}</strong>
            </span>
            <span style={{ marginLeft: 16 }}>
              Passed: <strong style={{ color: '#1e7e34' }}>{data.totalPassed}</strong>
            </span>
            <span style={{ marginLeft: 16 }}>
              Alter: <strong style={{ color: '#b8860b' }}>{data.totalAlter}</strong>
            </span>
            <span style={{ marginLeft: 16 }}>
              Rejected: <strong style={{ color: '#c0392b' }}>{data.totalRejected}</strong>
            </span>
            <span style={{ marginLeft: 16 }}>
              Rework rejects: <strong style={{ color: '#c0392b' }}>{data.totalReworkReject}</strong>
            </span>
          </div>

          {data.jobs.length === 0 && <p>No QC activity in this range.</p>}
          {data.jobs.length > 0 && (
            <table style={{ borderCollapse: 'collapse', minWidth: 640 }}>
              <thead>
                <tr>
                  <th style={th}>Job</th>
                  <th style={th}>Model</th>
                  <th style={thNum}>Checked</th>
                  <th style={thNum}>Passed</th>
                  <th style={thNum}>Alter</th>
                  <th style={thNum}>Rejected</th>
                  <th style={thNum}>Rework rej.</th>
                  <th style={thNum}>Reject %</th>
                  <th style={th} />
                </tr>
              </thead>
              <tbody>
                {data.jobs.map((j) => (
                  <RejectJobRows
                    key={j.jobId}
                    job={j}
                    open={openJob === j.jobId}
                    onToggle={() => setOpenJob(openJob === j.jobId ? null : j.jobId)}
                  />
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

function RejectJobRows({ job, open, onToggle }: { job: JobRejects; open: boolean; onToggle: () => void }) {
  return (
    <>
      <tr>
        <td style={td}>
          <strong>{job.jobDisplayId}</strong>
        </td>
        <td style={td}>{job.modelNo}</td>
        <td style={tdNum}>{job.checked}</td>
        <td style={{ ...tdNum, color: '#1e7e34' }}>{job.passed}</td>
        <td style={{ ...tdNum, color: '#b8860b' }}>{job.alter}</td>
        <td style={{ ...tdNum, color: '#c0392b', fontWeight: 600 }}>{job.rejected}</td>
        <td style={{ ...tdNum, color: '#c0392b' }}>{job.reworkReject}</td>
        <td style={tdNum}>{job.rejectPct}%</td>
        <td style={td}>
          <button onClick={onToggle} disabled={job.operations.length === 0}>
            {open ? 'Hide ops' : 'Ops'}
          </button>
        </td>
      </tr>
      {open && (
        <tr>
          <td colSpan={9} style={{ ...td, background: '#fafbff', padding: '8px 24px' }}>
            <table style={{ borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={th}>Workstation</th>
                  <th style={th}>Operation</th>
                  <th style={thNum}>Checked</th>
                  <th style={thNum}>Passed</th>
                  <th style={thNum}>Alter</th>
                  <th style={thNum}>Rejected</th>
                </tr>
              </thead>
              <tbody>
                {job.operations.map((o, i) => (
                  <tr key={i}>
                    <td style={td}>{o.workstation}</td>
                    <td style={td}>{o.operation}</td>
                    <td style={tdNum}>{o.checked}</td>
                    <td style={{ ...tdNum, color: '#1e7e34' }}>{o.passed}</td>
                    <td style={{ ...tdNum, color: '#b8860b' }}>{o.alter}</td>
                    <td style={{ ...tdNum, color: '#c0392b' }}>{o.rejected}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </td>
        </tr>
      )}
    </>
  );
}
