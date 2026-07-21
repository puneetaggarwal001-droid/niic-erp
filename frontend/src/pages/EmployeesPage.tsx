import { useEffect, useMemo, useState } from 'react';
import {
  createDesignation,
  createEmployee,
  deactivateEmployee,
  listDesignations,
  listEmployees,
  updateEmployee,
} from '../api/employees';
import { listWorkstations } from '../api/production';
import type { Designation, Employee, EmployeeRequest, SalaryType } from '../api/types';
import type { Workstation } from '../api/productionTypes';

const SALARY_TYPES: { value: SalaryType; label: string }[] = [
  { value: 'SALARIED', label: 'Salaried (monthly)' },
  { value: 'PC_RATE', label: 'Per-piece rate' },
  { value: 'CONTRACTOR', label: 'Contractor' },
];

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

interface FormState {
  name: string;
  aadhar: string;
  phone: string;
  designationId: number | '';
  dateOfJoining: string;
  salaryType: SalaryType;
  salary: string;
  pcRate: string;
  contractorName: string;
  empType: 'REGULAR' | 'VISITOR';
  department: string;
  address: string;
  notes: string;
  validTill: string;
  authorizedWorkstations: string[];
}

const emptyForm: FormState = {
  name: '',
  aadhar: '',
  phone: '',
  designationId: '',
  dateOfJoining: todayIso(),
  salaryType: 'PC_RATE',
  salary: '',
  pcRate: '',
  contractorName: '',
  empType: 'REGULAR',
  department: '',
  address: '',
  notes: '',
  validTill: '',
  authorizedWorkstations: [],
};

function formFromEmployee(e: Employee): FormState {
  return {
    name: e.name,
    aadhar: e.aadhar,
    phone: e.phone,
    designationId: e.designationId,
    dateOfJoining: e.dateOfJoining,
    salaryType: e.salaryType,
    salary: e.salary != null ? String(e.salary) : '',
    pcRate: e.pcRate != null ? String(e.pcRate) : '',
    contractorName: e.contractorName ?? '',
    empType: e.empType,
    department: e.department ?? '',
    address: e.address ?? '',
    notes: e.notes ?? '',
    validTill: e.validTill ?? '',
    authorizedWorkstations: [...e.authorizedWorkstations],
  };
}

export default function EmployeesPage() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [designations, setDesignations] = useState<Designation[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [showForm, setShowForm] = useState(false);
  // The employee being edited, or null when adding a new one.
  const [editing, setEditing] = useState<Employee | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [newDesignation, setNewDesignation] = useState('');
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([listEmployees(), listDesignations(), listWorkstations()])
      .then(([emps, desigs, ws]) => {
        setEmployees(emps);
        setDesignations(desigs);
        setWorkstations(ws);
      })
      .catch(() => setError('Could not load employees.'))
      .finally(() => setLoading(false));
  }, []);

  // Only workstations with a code can be authorized — production entry checks
  // authorization by workstation code, not name.
  const codedWorkstations = useMemo(() => workstations.filter((w) => w.code), [workstations]);

  function update<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  function toggleWorkstation(code: string) {
    setForm((prev) => ({
      ...prev,
      authorizedWorkstations: prev.authorizedWorkstations.includes(code)
        ? prev.authorizedWorkstations.filter((c) => c !== code)
        : [...prev.authorizedWorkstations, code],
    }));
  }

  function openAddForm() {
    setEditing(null);
    setForm({ ...emptyForm, dateOfJoining: todayIso() });
    setFormError(null);
    setShowForm(true);
  }

  function startEdit(employee: Employee) {
    setEditing(employee);
    setForm(formFromEmployee(employee));
    setFormError(null);
    setMessage(null);
    setShowForm(true);
  }

  function closeForm() {
    setShowForm(false);
    setEditing(null);
    setFormError(null);
  }

  async function handleDeactivate(employee: Employee) {
    if (!window.confirm(`Deactivate ${employee.name} (${employee.empId})? They'll no longer appear in the active list.`)) {
      return;
    }
    setError(null);
    setMessage(null);
    try {
      await deactivateEmployee(employee.id);
      setEmployees((prev) => prev.filter((e) => e.id !== employee.id));
      if (editing?.id === employee.id) closeForm();
      setMessage(`Deactivated ${employee.name} (${employee.empId}).`);
    } catch (err) {
      setError(extractError(err, 'Could not deactivate employee.'));
    }
  }

  async function handleAddDesignation() {
    const name = newDesignation.trim();
    if (!name) return;
    setFormError(null);
    try {
      const created = await createDesignation(name);
      setDesignations((prev) => [...prev, created].sort((a, b) => a.name.localeCompare(b.name)));
      update('designationId', created.id);
      setNewDesignation('');
    } catch (err) {
      setFormError(extractError(err, 'Could not add designation.'));
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFormError(null);
    setMessage(null);

    if (!form.name.trim() || !form.aadhar.trim() || !form.phone.trim() || form.designationId === '') {
      setFormError('Name, Aadhaar, phone and designation are required.');
      return;
    }

    const request: EmployeeRequest = {
      name: form.name.trim(),
      aadhar: form.aadhar.trim(),
      phone: form.phone.trim(),
      designationId: form.designationId,
      dateOfJoining: form.dateOfJoining,
      salaryType: form.salaryType,
      empType: form.empType,
      authorizedWorkstations: form.authorizedWorkstations,
      ...(form.address.trim() ? { address: form.address.trim() } : {}),
      ...(form.department.trim() ? { department: form.department.trim() } : {}),
      ...(form.notes.trim() ? { notes: form.notes.trim() } : {}),
      ...(form.validTill ? { validTill: form.validTill } : {}),
      ...(form.salaryType === 'SALARIED' && form.salary ? { salary: Number(form.salary) } : {}),
      ...(form.salaryType === 'PC_RATE' && form.pcRate ? { pcRate: Number(form.pcRate) } : {}),
      ...(form.salaryType === 'CONTRACTOR' && form.contractorName.trim()
        ? { contractorName: form.contractorName.trim() }
        : {}),
      // Carry over fields the form doesn't manage so an edit doesn't wipe them.
      ...(editing?.photoUrl ? { photoUrl: editing.photoUrl } : {}),
    };

    setSaving(true);
    try {
      if (editing) {
        const updated = await updateEmployee(editing.id, request);
        setEmployees((prev) => prev.map((e) => (e.id === updated.id ? updated : e)));
        setMessage(`Updated ${updated.name} (${updated.empId}).`);
      } else {
        const created = await createEmployee(request);
        setEmployees((prev) => [...prev, created]);
        setMessage(`Added ${created.name} (${created.empId}).`);
      }
      closeForm();
    } catch (err) {
      setFormError(extractError(err, editing ? 'Could not update employee.' : 'Could not add employee.'));
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p>Loading…</p>;
  if (error) return <p style={{ color: '#dc2626' }}>{error}</p>;

  return (
    <div>
      <h1>Employees</h1>
      {message && <p style={{ color: '#15803d' }}>{message}</p>}

      <div style={{ marginBottom: 16 }}>
        {showForm ? (
          <button type="button" onClick={closeForm}>
            Cancel
          </button>
        ) : (
          <button type="button" onClick={openAddForm}>
            + Add employee
          </button>
        )}
      </div>

      {showForm && (
        <section style={{ marginBottom: 24 }}>
          <h2>{editing ? `Edit ${editing.empId}` : 'New employee'}</h2>
          {formError && <p style={{ color: '#dc2626' }}>{formError}</p>}
          <form onSubmit={handleSubmit}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 12 }}>
              <label>
                Name *
                <input value={form.name} onChange={(e) => update('name', e.target.value)} />
              </label>
              <label>
                Designation *
                <select
                  value={form.designationId}
                  onChange={(e) => update('designationId', e.target.value ? Number(e.target.value) : '')}
                >
                  <option value="">Select designation</option>
                  {designations.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Aadhaar * (12 digits)
                <input
                  value={form.aadhar}
                  onChange={(e) => update('aadhar', e.target.value.replace(/\D/g, '').slice(0, 12))}
                  inputMode="numeric"
                  placeholder="123412341234"
                />
              </label>
              <label>
                Phone * (10 digits)
                <input
                  value={form.phone}
                  onChange={(e) => update('phone', e.target.value.replace(/\D/g, '').slice(0, 10))}
                  inputMode="numeric"
                  placeholder="9876543210"
                />
              </label>
              <label>
                Date of joining *
                <input type="date" value={form.dateOfJoining} onChange={(e) => update('dateOfJoining', e.target.value)} />
              </label>
              <label>
                Pay type *
                <select value={form.salaryType} onChange={(e) => update('salaryType', e.target.value as SalaryType)}>
                  {SALARY_TYPES.map((t) => (
                    <option key={t.value} value={t.value}>
                      {t.label}
                    </option>
                  ))}
                </select>
              </label>
              {form.salaryType === 'SALARIED' && (
                <label>
                  Monthly salary
                  <input
                    value={form.salary}
                    onChange={(e) => update('salary', e.target.value)}
                    inputMode="decimal"
                    placeholder="e.g. 25000"
                  />
                </label>
              )}
              {form.salaryType === 'PC_RATE' && (
                <label>
                  Per-piece rate
                  <input
                    value={form.pcRate}
                    onChange={(e) => update('pcRate', e.target.value)}
                    inputMode="decimal"
                    placeholder="e.g. 5"
                  />
                </label>
              )}
              {form.salaryType === 'CONTRACTOR' && (
                <label>
                  Contractor name
                  <input value={form.contractorName} onChange={(e) => update('contractorName', e.target.value)} />
                </label>
              )}
              <label>
                Employee type
                <select value={form.empType} onChange={(e) => update('empType', e.target.value as 'REGULAR' | 'VISITOR')}>
                  <option value="REGULAR">Regular</option>
                  <option value="VISITOR">Visitor</option>
                </select>
              </label>
              <label>
                Valid till (optional)
                <input type="date" value={form.validTill} onChange={(e) => update('validTill', e.target.value)} />
              </label>
              <label>
                Department
                <input value={form.department} onChange={(e) => update('department', e.target.value)} />
              </label>
              <label style={{ gridColumn: '1 / -1' }}>
                Address
                <input value={form.address} onChange={(e) => update('address', e.target.value)} />
              </label>
              <label style={{ gridColumn: '1 / -1' }}>
                Notes
                <input value={form.notes} onChange={(e) => update('notes', e.target.value)} />
              </label>
            </div>

            <div style={{ marginTop: 16 }}>
              <strong>Authorized workstations</strong>
              <p style={{ margin: '4px 0', color: '#6b7280', fontSize: 13 }}>
                An employee can only log production at workstations they're authorized for. Leave all unchecked to allow
                any workstation.
              </p>
              {codedWorkstations.length === 0 ? (
                <p style={{ color: '#6b7280' }}>No workstations with a code yet — add them on the Masters page.</p>
              ) : (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12 }}>
                  {codedWorkstations.map((w) => (
                    <label key={w.id} style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                      <input
                        type="checkbox"
                        checked={form.authorizedWorkstations.includes(w.code as string)}
                        onChange={() => toggleWorkstation(w.code as string)}
                      />
                      {w.name} ({w.code})
                    </label>
                  ))}
                </div>
              )}
            </div>

            <div style={{ marginTop: 16, display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ color: '#6b7280', fontSize: 13 }}>Missing a designation?</span>
              <input
                value={newDesignation}
                onChange={(e) => setNewDesignation(e.target.value)}
                placeholder="New designation name"
                style={{ width: 220 }}
              />
              <button type="button" onClick={handleAddDesignation}>
                Add designation
              </button>
            </div>

            <div style={{ marginTop: 16 }}>
              <button type="submit" disabled={saving}>
                {saving ? 'Saving…' : editing ? 'Update employee' : 'Save employee'}
              </button>
            </div>
          </form>
        </section>
      )}

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ textAlign: 'left', borderBottom: '1px solid #ddd' }}>
            <th>Emp ID</th>
            <th>Name</th>
            <th>Designation</th>
            <th>Phone</th>
            <th>Workstations</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {employees.map((e) => (
            <tr key={e.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
              <td>{e.empId}</td>
              <td>{e.name}</td>
              <td>{e.designationName}</td>
              <td>{e.phone}</td>
              <td>{e.authorizedWorkstations.length ? e.authorizedWorkstations.join(', ') : '—'}</td>
              <td style={{ whiteSpace: 'nowrap', textAlign: 'right' }}>
                <button type="button" onClick={() => startEdit(e)}>
                  Edit
                </button>{' '}
                <button
                  type="button"
                  onClick={() => handleDeactivate(e)}
                  style={{ color: '#dc2626', borderColor: '#dc2626' }}
                >
                  Deactivate
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {employees.length === 0 && <p>No employees yet.</p>}
    </div>
  );
}

function extractError(err: unknown, fallback: string): string {
  const anyErr = err as { response?: { data?: { message?: string } } };
  return anyErr?.response?.data?.message ?? fallback;
}
