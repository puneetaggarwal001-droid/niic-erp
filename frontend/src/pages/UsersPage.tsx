import { useEffect, useState } from 'react';
import { RIGHTS, createUser, listUsers, updateUser } from '../api/users';
import type { AppUser, Role } from '../api/types';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px', verticalAlign: 'top' };

const ROLES: Role[] = ['ADMIN', 'ENTRY_USER', 'STORE_ADMIN'];

interface FormState {
  username: string;
  password: string;
  role: Role;
  active: boolean;
  rights: string[];
}

function emptyForm(): FormState {
  return { username: '', password: '', role: 'ENTRY_USER', active: true, rights: [] };
}

function formFromUser(u: AppUser): FormState {
  return { username: u.username, password: '', role: u.role, active: u.active, rights: [...u.rights] };
}

export default function UsersPage() {
  const [users, setUsers] = useState<AppUser[]>([]);
  const [editing, setEditing] = useState<AppUser | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm());
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  function refresh() {
    listUsers().then(setUsers).catch(() => setError('Could not load users.'));
  }

  useEffect(() => {
    refresh();
  }, []);

  function openCreate() {
    setEditing(null);
    setForm(emptyForm());
    setFormError(null);
    setShowForm(true);
  }

  function openEdit(u: AppUser) {
    setEditing(u);
    setForm(formFromUser(u));
    setFormError(null);
    setShowForm(true);
  }

  function toggleRight(key: string) {
    setForm((f) => ({
      ...f,
      rights: f.rights.includes(key) ? f.rights.filter((r) => r !== key) : [...f.rights, key],
    }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFormError(null);
    setMessage(null);
    // Admins implicitly hold every right, so don't send an explicit set for them.
    const rights = form.role === 'ADMIN' ? [] : form.rights;
    try {
      if (editing) {
        await updateUser(editing.id, {
          role: form.role,
          rights,
          active: form.active,
          password: form.password.trim() ? form.password : undefined,
        });
        setMessage(`Updated ${editing.username}.`);
      } else {
        await createUser({
          username: form.username.trim(),
          password: form.password,
          role: form.role,
          rights,
        });
        setMessage(`Created ${form.username.trim()}.`);
      }
      setShowForm(false);
      setEditing(null);
      refresh();
    } catch (err) {
      setFormError(extractError(err, editing ? 'Could not update user.' : 'Could not create user.'));
    }
  }

  async function toggleActive(u: AppUser) {
    setError(null);
    setMessage(null);
    try {
      await updateUser(u.id, { role: u.role, rights: u.rights, active: !u.active });
      setMessage(`${u.username} ${u.active ? 'deactivated' : 'reactivated'}.`);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not change user status.'));
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Users</h2>
        <button onClick={openCreate}>+ New user</button>
      </div>

      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      {showForm && (
        <form
          onSubmit={handleSubmit}
          style={{ border: '1px solid #ddd', borderRadius: 6, padding: 16, margin: '12px 0', maxWidth: 520 }}
        >
          <h3 style={{ marginTop: 0 }}>{editing ? `Edit ${editing.username}` : 'New user'}</h3>
          {formError && <p style={{ color: '#c0392b' }}>{formError}</p>}

          <div style={{ marginBottom: 10 }}>
            <label style={{ display: 'block', fontSize: 13, marginBottom: 2 }}>Username</label>
            <input
              value={form.username}
              disabled={editing !== null}
              onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))}
              required
              style={{ width: '100%', padding: 6 }}
            />
          </div>

          <div style={{ marginBottom: 10 }}>
            <label style={{ display: 'block', fontSize: 13, marginBottom: 2 }}>
              {editing ? 'New password (leave blank to keep current)' : 'Password'}
            </label>
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
              required={editing === null}
              minLength={editing ? undefined : 6}
              style={{ width: '100%', padding: 6 }}
            />
          </div>

          <div style={{ marginBottom: 10 }}>
            <label style={{ display: 'block', fontSize: 13, marginBottom: 2 }}>Role</label>
            <select
              value={form.role}
              onChange={(e) => setForm((f) => ({ ...f, role: e.target.value as Role }))}
              style={{ width: '100%', padding: 6 }}
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>

          {editing && (
            <div style={{ marginBottom: 10 }}>
              <label style={{ fontSize: 14 }}>
                <input
                  type="checkbox"
                  checked={form.active}
                  onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
                />{' '}
                Active
              </label>
            </div>
          )}

          {form.role !== 'ADMIN' && (
            <div style={{ marginBottom: 10 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 4 }}>Rights</label>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 4 }}>
                {RIGHTS.map((r) => (
                  <label key={r.key} style={{ fontSize: 14 }}>
                    <input
                      type="checkbox"
                      checked={form.rights.includes(r.key)}
                      onChange={() => toggleRight(r.key)}
                    />{' '}
                    {r.label}
                  </label>
                ))}
              </div>
            </div>
          )}
          {form.role === 'ADMIN' && (
            <p style={{ fontSize: 13, color: '#666' }}>Admins implicitly have every right.</p>
          )}

          <div style={{ display: 'flex', gap: 8 }}>
            <button type="submit">{editing ? 'Save changes' : 'Create user'}</button>
            <button type="button" onClick={() => setShowForm(false)}>
              Cancel
            </button>
          </div>
        </form>
      )}

      <table style={{ borderCollapse: 'collapse', width: '100%', marginTop: 12 }}>
        <thead>
          <tr>
            <th style={th}>Username</th>
            <th style={th}>Role</th>
            <th style={th}>Rights</th>
            <th style={th}>Status</th>
            <th style={th}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td style={td}>{u.username}</td>
              <td style={td}>{u.role}</td>
              <td style={td}>{u.role === 'ADMIN' ? 'all' : u.rights.join(', ') || '—'}</td>
              <td style={td}>{u.active ? 'Active' : 'Inactive'}</td>
              <td style={td}>
                <button onClick={() => openEdit(u)} style={{ marginRight: 6 }}>
                  Edit
                </button>
                <button
                  onClick={() => toggleActive(u)}
                  style={{ color: u.active ? '#c0392b' : '#1e7e34' }}
                >
                  {u.active ? 'Deactivate' : 'Reactivate'}
                </button>
              </td>
            </tr>
          ))}
          {users.length === 0 && (
            <tr>
              <td style={td} colSpan={5}>
                No users yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function extractError(err: unknown, fallback: string): string {
  const anyErr = err as { response?: { data?: { message?: string } } };
  return anyErr?.response?.data?.message ?? fallback;
}
