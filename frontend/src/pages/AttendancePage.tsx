import { useEffect, useState } from 'react';
import { listAttendanceForDate } from '../api/attendance';
import type { AttendanceRecord } from '../api/types';

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

export default function AttendancePage() {
  const [date, setDate] = useState(today());
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    listAttendanceForDate(date)
      .then(setRecords)
      .catch(() => setError('Could not load attendance for this date.'))
      .finally(() => setLoading(false));
  }, [date]);

  return (
    <div>
      <h1>Attendance</h1>
      <input type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ marginBottom: 16 }} />
      {loading && <p>Loading…</p>}
      {error && <p style={{ color: '#dc2626' }}>{error}</p>}
      {!loading && !error && (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ textAlign: 'left', borderBottom: '1px solid #ddd' }}>
              <th>Emp ID</th>
              <th>Name</th>
              <th>Designation</th>
              <th>Entry</th>
              <th>Exit</th>
            </tr>
          </thead>
          <tbody>
            {records.map((r) => (
              <tr key={r.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                <td>{r.empId}</td>
                <td>{r.employeeName}</td>
                <td>{r.designationName}</td>
                <td>{r.entryTime ?? '—'}</td>
                <td>{r.exitTime ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {!loading && !error && records.length === 0 && <p>No attendance recorded for this date.</p>}
    </div>
  );
}
