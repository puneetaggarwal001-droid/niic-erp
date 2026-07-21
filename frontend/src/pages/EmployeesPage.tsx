import { useEffect, useState } from 'react';
import { listEmployees } from '../api/employees';
import type { Employee } from '../api/types';

export default function EmployeesPage() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listEmployees()
      .then(setEmployees)
      .catch(() => setError('Could not load employees.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading…</p>;
  if (error) return <p style={{ color: '#dc2626' }}>{error}</p>;

  return (
    <div>
      <h1>Employees</h1>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ textAlign: 'left', borderBottom: '1px solid #ddd' }}>
            <th>Emp ID</th>
            <th>Name</th>
            <th>Designation</th>
            <th>Phone</th>
          </tr>
        </thead>
        <tbody>
          {employees.map((e) => (
            <tr key={e.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
              <td>{e.empId}</td>
              <td>{e.name}</td>
              <td>{e.designationName}</td>
              <td>{e.phone}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {employees.length === 0 && <p>No employees yet.</p>}
    </div>
  );
}
