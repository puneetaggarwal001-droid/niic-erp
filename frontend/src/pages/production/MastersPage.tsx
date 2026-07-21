import { useEffect, useState } from 'react';
import {
  createOperation,
  createPcRate,
  createWorkstation,
  listOperations,
  listPcRates,
  listStyles,
  listWorkstations,
  updateWorkstation,
} from '../../api/production';
import type { Operation, PcRate, Style, Workstation } from '../../api/productionTypes';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };

export default function MastersPage() {
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [operations, setOperations] = useState<Operation[]>([]);
  const [styles, setStyles] = useState<Style[]>([]);
  const [pcRates, setPcRates] = useState<PcRate[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [newWsName, setNewWsName] = useState('');
  const [newWsCode, setNewWsCode] = useState('');

  const [opWorkstationId, setOpWorkstationId] = useState<number | ''>('');
  const [newOpName, setNewOpName] = useState('');

  const [rateWorkstationId, setRateWorkstationId] = useState<number | ''>('');
  const [rateStyleCode, setRateStyleCode] = useState('');
  const [rateModelNo, setRateModelNo] = useState('');
  const [rateOperationId, setRateOperationId] = useState<number | ''>('');
  const [rateAmount, setRateAmount] = useState('');
  const [rateEffectiveDate, setRateEffectiveDate] = useState(() => new Date().toISOString().slice(0, 10));

  function refreshWorkstations() {
    listWorkstations().then(setWorkstations).catch(() => setError('Could not load workstations.'));
  }

  function refreshOperations() {
    listOperations().then(setOperations).catch(() => setError('Could not load operations.'));
  }

  useEffect(() => {
    refreshWorkstations();
    refreshOperations();
    listStyles().then(setStyles).catch(() => setError('Could not load styles.'));
  }, []);

  useEffect(() => {
    if (rateWorkstationId === '' || !rateStyleCode || !rateModelNo) {
      setPcRates([]);
      return;
    }
    listPcRates(rateWorkstationId, rateStyleCode, rateModelNo)
      .then(setPcRates)
      .catch(() => setError('Could not load PC rates.'));
  }, [rateWorkstationId, rateStyleCode, rateModelNo]);

  async function handleAddWorkstation(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await createWorkstation(newWsName, newWsCode || undefined);
      setNewWsName('');
      setNewWsCode('');
      refreshWorkstations();
    } catch {
      setError('Could not create workstation. Name may already be in use.');
    }
  }

  async function handleToggleWorkstation(ws: Workstation) {
    setError(null);
    try {
      await updateWorkstation(ws.id, { name: ws.name, code: ws.code ?? undefined, active: !ws.active });
      refreshWorkstations();
    } catch {
      setError('Could not update workstation.');
    }
  }

  async function handleAddOperation(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    if (opWorkstationId === '') return;
    try {
      await createOperation(opWorkstationId, newOpName);
      setNewOpName('');
      refreshOperations();
    } catch {
      setError('Could not create operation. It may already exist for this workstation.');
    }
  }

  async function handleAddPcRate(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    if (rateWorkstationId === '' || rateOperationId === '') return;
    try {
      await createPcRate({
        workstationId: rateWorkstationId,
        styleCode: rateStyleCode,
        modelNo: rateModelNo,
        operationId: rateOperationId,
        rate: Number(rateAmount),
        effectiveDate: rateEffectiveDate,
      });
      setRateAmount('');
      listPcRates(rateWorkstationId, rateStyleCode, rateModelNo).then(setPcRates);
    } catch {
      setError('Could not create PC rate.');
    }
  }

  return (
    <div>
      <h1>Production Masters</h1>
      {error && <p style={{ color: '#dc2626' }}>{error}</p>}

      <section style={{ marginBottom: 32 }}>
        <h2>Workstations</h2>
        <form onSubmit={handleAddWorkstation} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
          <input placeholder="Name" value={newWsName} onChange={(e) => setNewWsName(e.target.value)} required />
          <input placeholder="Code (optional)" value={newWsCode} onChange={(e) => setNewWsCode(e.target.value)} />
          <button type="submit">Add workstation</button>
        </form>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={th}>Name</th>
              <th style={th}>Code</th>
              <th style={th}>Active</th>
              <th style={th}></th>
            </tr>
          </thead>
          <tbody>
            {workstations.map((ws) => (
              <tr key={ws.id}>
                <td style={td}>{ws.name}</td>
                <td style={td}>{ws.code ?? '—'}</td>
                <td style={td}>{ws.active ? 'Yes' : 'No'}</td>
                <td style={td}>
                  <button onClick={() => handleToggleWorkstation(ws)}>{ws.active ? 'Deactivate' : 'Activate'}</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>Operations</h2>
        <form onSubmit={handleAddOperation} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
          <select value={opWorkstationId} onChange={(e) => setOpWorkstationId(e.target.value ? Number(e.target.value) : '')} required>
            <option value="">Select workstation</option>
            {workstations.map((ws) => (
              <option key={ws.id} value={ws.id}>
                {ws.name}
              </option>
            ))}
          </select>
          <input placeholder="Operation name" value={newOpName} onChange={(e) => setNewOpName(e.target.value)} required />
          <button type="submit">Add operation</button>
        </form>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={th}>Workstation</th>
              <th style={th}>Operation</th>
              <th style={th}>Active</th>
            </tr>
          </thead>
          <tbody>
            {operations.map((op) => (
              <tr key={op.id}>
                <td style={td}>{op.workstationName}</td>
                <td style={td}>{op.name}</td>
                <td style={td}>{op.active ? 'Yes' : 'No'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section>
        <h2>PC Rates</h2>
        <form onSubmit={handleAddPcRate} style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
          <select value={rateWorkstationId} onChange={(e) => setRateWorkstationId(e.target.value ? Number(e.target.value) : '')} required>
            <option value="">Workstation</option>
            {workstations.map((ws) => (
              <option key={ws.id} value={ws.id}>
                {ws.name}
              </option>
            ))}
          </select>
          <select value={rateStyleCode} onChange={(e) => setRateStyleCode(e.target.value)} required>
            <option value="">Style</option>
            {styles.map((s) => (
              <option key={s.code} value={s.code}>
                {s.label}
              </option>
            ))}
          </select>
          <input placeholder="Model no." value={rateModelNo} onChange={(e) => setRateModelNo(e.target.value)} required />
          <select
            value={rateOperationId}
            onChange={(e) => setRateOperationId(e.target.value ? Number(e.target.value) : '')}
            required
          >
            <option value="">Operation</option>
            {operations
              .filter((op) => rateWorkstationId === '' || op.workstationId === rateWorkstationId)
              .map((op) => (
                <option key={op.id} value={op.id}>
                  {op.name}
                </option>
              ))}
          </select>
          <input
            type="number"
            step="0.01"
            placeholder="Rate"
            value={rateAmount}
            onChange={(e) => setRateAmount(e.target.value)}
            required
            style={{ width: 100 }}
          />
          <input type="date" value={rateEffectiveDate} onChange={(e) => setRateEffectiveDate(e.target.value)} required />
          <button type="submit">Add rate</button>
        </form>
        {rateWorkstationId === '' || !rateStyleCode || !rateModelNo ? (
          <p>Select workstation, style and model no. above to view existing rates.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={th}>Operation</th>
                <th style={th}>Rate</th>
                <th style={th}>Effective</th>
                <th style={th}>Active</th>
              </tr>
            </thead>
            <tbody>
              {pcRates.map((r) => (
                <tr key={r.id}>
                  <td style={td}>{r.operationName}</td>
                  <td style={td}>{r.rate}</td>
                  <td style={td}>{r.effectiveDate}</td>
                  <td style={td}>{r.active ? 'Yes' : 'No'}</td>
                </tr>
              ))}
              {pcRates.length === 0 && (
                <tr>
                  <td style={td} colSpan={4}>
                    No rates yet for this combination.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
