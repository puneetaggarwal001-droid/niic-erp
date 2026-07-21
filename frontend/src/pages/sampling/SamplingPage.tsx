import { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  addPhoto,
  cancelRequest,
  closeSample,
  completePpm,
  completeRequest,
  createRequest,
  createSample,
  deletePhoto,
  getSample,
  listRequests,
  listSamples,
  rejectSample,
  reviseSample,
  savePpm,
  savePps,
  selectSample,
  startRequest,
  STATUS_COLOUR,
  STATUS_LABEL,
  submitSample,
  updateSample,
  type LineForm,
  type PpmSectionKey,
  type SampleDetail,
  type SampleForm,
  type SampleRequest,
  type SampleStatus,
  type SampleSummary,
} from '../../api/sampling';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px', fontSize: 13 };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px', fontSize: 13 };
const input: React.CSSProperties = { padding: 6 };
const card: React.CSSProperties = { border: '1px solid #e5e7eb', borderRadius: 8, padding: 16, marginBottom: 16 };

function extractError(err: unknown, fallback: string): string {
  const anyErr = err as { response?: { data?: { message?: string } } };
  return anyErr?.response?.data?.message ?? fallback;
}

function Badge({ status }: { status: SampleStatus }) {
  return (
    <span
      style={{
        background: STATUS_COLOUR[status],
        color: '#fff',
        borderRadius: 10,
        padding: '2px 10px',
        fontSize: 12,
        fontWeight: 700,
        whiteSpace: 'nowrap',
      }}
    >
      {STATUS_LABEL[status]}
    </span>
  );
}

/** Downscale + compress an image file to a JPEG data URL (max 900px, ~0.7 quality). */
function compressToDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const img = new Image();
      img.onload = () => {
        const MAX = 900;
        let { width: w, height: h } = img;
        if (w > MAX || h > MAX) {
          if (w > h) {
            h = Math.round((h * MAX) / w);
            w = MAX;
          } else {
            w = Math.round((w * MAX) / h);
            h = MAX;
          }
        }
        const canvas = document.createElement('canvas');
        canvas.width = w;
        canvas.height = h;
        canvas.getContext('2d')?.drawImage(img, 0, 0, w, h);
        resolve(canvas.toDataURL('image/jpeg', 0.7));
      };
      img.onerror = reject;
      img.src = e.target?.result as string;
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

type Tab = 'samples' | 'requests';

export default function SamplingPage() {
  const [tab, setTab] = useState<Tab>('samples');
  const tabs: { key: Tab; label: string }[] = [
    { key: 'samples', label: 'Samples' },
    { key: 'requests', label: 'Requests' },
  ];
  return (
    <div>
      <h2>Sampling</h2>
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
              borderBottom: tab === t.key ? '3px solid #7c3aed' : '3px solid transparent',
            }}
          >
            {t.label}
          </button>
        ))}
      </div>
      {tab === 'samples' ? <SamplesTab /> : <RequestsTab />}
    </div>
  );
}

// ===========================================================================
// Samples
// ===========================================================================

type View = { kind: 'list' } | { kind: 'form'; id: number | null } | { kind: 'detail'; id: number };

function SamplesTab() {
  const [view, setView] = useState<View>({ kind: 'list' });
  if (view.kind === 'form') {
    return <SampleForm id={view.id} onDone={() => setView({ kind: 'list' })} onOpen={(id) => setView({ kind: 'detail', id })} />;
  }
  if (view.kind === 'detail') {
    return (
      <SampleDetailView
        id={view.id}
        onBack={() => setView({ kind: 'list' })}
        onEdit={(id) => setView({ kind: 'form', id })}
        onOpen={(id) => setView({ kind: 'detail', id })}
      />
    );
  }
  return <SampleList onNew={() => setView({ kind: 'form', id: null })} onOpen={(id) => setView({ kind: 'detail', id })} />;
}

function SampleList({ onNew, onOpen }: { onNew: () => void; onOpen: (id: number) => void }) {
  const [samples, setSamples] = useState<SampleSummary[]>([]);
  const [status, setStatus] = useState<'' | SampleStatus>('');
  const [includeClosed, setIncludeClosed] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listSamples(status || undefined, includeClosed)
      .then(setSamples)
      .catch(() => setError('Could not load samples.'));
  }, [status, includeClosed]);

  return (
    <div>
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 12, flexWrap: 'wrap' }}>
        <button onClick={onNew} style={{ background: '#7c3aed', color: '#fff', border: 'none', padding: '8px 14px', borderRadius: 6, cursor: 'pointer' }}>
          + New sample
        </button>
        <select value={status} onChange={(e) => setStatus(e.target.value as '' | SampleStatus)} style={input}>
          <option value="">All statuses</option>
          {(['DRAFT', 'IN_REVIEW', 'SELECTED', 'PPS_DONE', 'PPM_DONE', 'REJECTED', 'CLOSED'] as SampleStatus[]).map((s) => (
            <option key={s} value={s}>
              {STATUS_LABEL[s]}
            </option>
          ))}
        </select>
        <label style={{ fontSize: 13 }}>
          <input type="checkbox" checked={includeClosed} onChange={(e) => setIncludeClosed(e.target.checked)} /> include closed/rejected
        </label>
      </div>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>Sample #</th>
            <th style={th}>Name</th>
            <th style={th}>Style</th>
            <th style={th}>Category</th>
            <th style={th}>Date</th>
            <th style={th}>Status</th>
          </tr>
        </thead>
        <tbody>
          {samples.map((s) => (
            <tr key={s.id}>
              <td style={{ ...td, fontWeight: 600 }}>
                <button
                  onClick={() => onOpen(s.id)}
                  style={{ background: 'none', border: 'none', padding: 0, font: 'inherit', fontWeight: 600, color: '#1B2E72', cursor: 'pointer' }}
                >
                  {s.sampleNo}
                  {s.revNum > 0 && <span style={{ color: '#7c3aed' }}> ·R{s.revNum}</span>}
                </button>
              </td>
              <td style={td}>{s.name}</td>
              <td style={td}>{s.style ?? '—'}</td>
              <td style={td}>{s.category ?? '—'}</td>
              <td style={td}>{s.date ?? '—'}</td>
              <td style={td}>
                <Badge status={s.status} />
                {s.closedRemark && <span style={{ color: '#9ca3af', fontSize: 11 }}> ({s.closedRemark.toLowerCase().replace(/_/g, ' ')})</span>}
              </td>
            </tr>
          ))}
          {samples.length === 0 && (
            <tr>
              <td style={td} colSpan={6}>
                No samples.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

const UNITS = ['Metres', 'Kg', 'Pcs', 'Pairs', 'Litres', 'Nos'];

function SampleForm({ id, onDone, onOpen }: { id: number | null; onDone: () => void; onOpen: (id: number) => void }) {
  const [loading, setLoading] = useState(id !== null);
  const [date, setDate] = useState<string>('');
  const [name, setName] = useState('');
  const [style, setStyle] = useState('');
  const [category, setCategory] = useState('');
  const [designer, setDesigner] = useState('');
  const [reference, setReference] = useState('');
  const [notes, setNotes] = useState('');
  const [rm, setRm] = useState<LineForm[]>([]);
  const [sfg, setSfg] = useState<LineForm[]>([]);
  const [ops, setOps] = useState<LineForm[]>([]);
  const [sampleNo, setSampleNo] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (id === null) return;
    getSample(id)
      .then((s) => {
        setDate(s.date ?? '');
        setName(s.name);
        setStyle(s.style ?? '');
        setCategory(s.category ?? '');
        setDesigner(s.designer ?? '');
        setReference(s.reference ?? '');
        setNotes(s.notes ?? '');
        setRm(s.rawMaterials.map(stripLine));
        setSfg(s.sfgItems.map(stripLine));
        setOps(s.operations.map(stripLine));
        setSampleNo(s.sampleNo);
      })
      .catch(() => setError('Could not load sample.'))
      .finally(() => setLoading(false));
  }, [id]);

  function buildForm(): SampleForm {
    return {
      date: date || undefined,
      name: name.trim(),
      style: style || undefined,
      category: category || undefined,
      designer: designer || undefined,
      reference: reference || undefined,
      notes: notes || undefined,
      rawMaterials: rm,
      sfgItems: sfg,
      operations: ops,
    };
  }

  async function save(submit: boolean) {
    setError(null);
    if (!name.trim()) {
      setError('Sample name is required.');
      return;
    }
    setBusy(true);
    try {
      const saved = id === null ? await createSample(buildForm(), submit) : await updateSample(id, buildForm(), submit);
      onOpen(saved.id);
    } catch (err) {
      setError(extractError(err, 'Could not save the sample.'));
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <p>Loading…</p>;

  return (
    <div>
      <button onClick={onDone} style={{ marginBottom: 12 }}>
        ← Back
      </button>
      <h3 style={{ marginTop: 0 }}>{id === null ? 'New sample' : `Edit ${sampleNo ?? 'sample'}`}</h3>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      <div style={card}>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <Field label="Date">
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} style={input} />
          </Field>
          <Field label="Name *" grow>
            <input value={name} onChange={(e) => setName(e.target.value)} style={{ ...input, width: '100%' }} />
          </Field>
          <Field label="Style">
            <input value={style} onChange={(e) => setStyle(e.target.value)} style={input} />
          </Field>
          <Field label="Category">
            <input value={category} onChange={(e) => setCategory(e.target.value)} style={input} />
          </Field>
          <Field label="Designer">
            <input value={designer} onChange={(e) => setDesigner(e.target.value)} style={input} />
          </Field>
          <Field label="Reference">
            <input value={reference} onChange={(e) => setReference(e.target.value)} style={input} />
          </Field>
          <Field label="Notes" grow>
            <input value={notes} onChange={(e) => setNotes(e.target.value)} style={{ ...input, width: '100%' }} />
          </Field>
        </div>
      </div>

      <LineEditor title="Raw materials" rows={rm} setRows={setRm} withColour />
      <LineEditor title="Semi-finished items" rows={sfg} setRows={setSfg} />
      <LineEditor title="Operations" rows={ops} setRows={setOps} nameOnly />

      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={() => save(false)} disabled={busy}>
          Save draft
        </button>
        <button
          onClick={() => save(true)}
          disabled={busy}
          style={{ background: '#7c3aed', color: '#fff', border: 'none', padding: '8px 14px', borderRadius: 6 }}
        >
          Save &amp; submit for review
        </button>
      </div>
    </div>
  );
}

function stripLine(l: { itemId: number | null; name: string | null; description: string | null; colour: string | null; qty: number | null; unit: string | null }): LineForm {
  return {
    itemId: l.itemId ?? undefined,
    name: l.name ?? undefined,
    description: l.description ?? undefined,
    colour: l.colour ?? undefined,
    qty: l.qty ?? undefined,
    unit: l.unit ?? undefined,
  };
}

function LineEditor({
  title,
  rows,
  setRows,
  withColour,
  nameOnly,
}: {
  title: string;
  rows: LineForm[];
  setRows: (fn: (prev: LineForm[]) => LineForm[]) => void;
  withColour?: boolean;
  nameOnly?: boolean;
}) {
  const set = (i: number, patch: Partial<LineForm>) => setRows((prev) => prev.map((r, j) => (j === i ? { ...r, ...patch } : r)));
  return (
    <div style={card}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>{title}</div>
      {rows.map((r, i) => (
        <div key={i} style={{ display: 'flex', gap: 6, marginBottom: 6, flexWrap: 'wrap' }}>
          <input placeholder="Name" value={r.name ?? ''} onChange={(e) => set(i, { name: e.target.value })} style={{ ...input, flex: 1, minWidth: 140 }} />
          {!nameOnly && (
            <input placeholder="Description" value={r.description ?? ''} onChange={(e) => set(i, { description: e.target.value })} style={{ ...input, flex: 1, minWidth: 140 }} />
          )}
          {withColour && (
            <input placeholder="Colour" value={r.colour ?? ''} onChange={(e) => set(i, { colour: e.target.value })} style={{ ...input, width: 100 }} />
          )}
          {!nameOnly && (
            <>
              <input
                type="number"
                placeholder="Qty"
                value={r.qty ?? ''}
                onChange={(e) => set(i, { qty: e.target.value ? Number(e.target.value) : undefined })}
                style={{ ...input, width: 80 }}
              />
              <select value={r.unit ?? 'Pcs'} onChange={(e) => set(i, { unit: e.target.value })} style={input}>
                {UNITS.map((u) => (
                  <option key={u}>{u}</option>
                ))}
              </select>
            </>
          )}
          <button type="button" onClick={() => setRows((prev) => prev.filter((_, j) => j !== i))}>
            ✕
          </button>
        </div>
      ))}
      <button type="button" onClick={() => setRows((prev) => [...prev, {}])}>
        + add row
      </button>
    </div>
  );
}

function Field({ label, children, grow }: { label: string; children: React.ReactNode; grow?: boolean }) {
  return (
    <label style={{ fontSize: 13, flex: grow ? 1 : undefined, minWidth: grow ? 160 : undefined }}>
      {label}
      <br />
      {children}
    </label>
  );
}

// ---- Sample detail --------------------------------------------------------

const PPM_SECTIONS: { key: PpmSectionKey; label: string; col1: string; col2: string }[] = [
  { key: 'SOP', label: 'SOP', col1: 'Workstation', col2: 'Operation' },
  { key: 'TRIM', label: 'Trimming', col1: 'Detail', col2: 'Method' },
  { key: 'MARK', label: 'Marking', col1: 'Detail', col2: 'Method' },
  { key: 'CHECK', label: 'Checkpoints', col1: 'Checkpoint', col2: 'Criteria' },
  { key: 'PACK', label: 'Packing', col1: 'Step', col2: 'Detail' },
];

function SampleDetailView({
  id,
  onBack,
  onEdit,
  onOpen,
}: {
  id: number;
  onBack: () => void;
  onEdit: (id: number) => void;
  onOpen: (id: number) => void;
}) {
  const { auth } = useAuth();
  const isAdmin = auth?.role === 'ADMIN';
  const [sample, setSample] = useState<SampleDetail | null>(null);
  const [section, setSection] = useState<'info' | 'pps' | 'ppm'>('info');
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  function reload() {
    getSample(id).then(setSample).catch(() => setError('Could not load sample.'));
  }
  useEffect(reload, [id]);

  async function act(fn: () => Promise<SampleDetail>, ok: string) {
    setError(null);
    setMessage(null);
    try {
      const updated = await fn();
      setSample(updated);
      setMessage(ok);
    } catch (err) {
      setError(extractError(err, 'Action failed.'));
    }
  }

  if (!sample) return <p>{error ?? 'Loading…'}</p>;

  const s = sample;
  const canEdit = s.status === 'DRAFT' || s.status === 'IN_REVIEW';
  const ppsReady = s.status === 'SELECTED' || s.status === 'PPS_DONE' || s.status === 'PPM_DONE';
  const ppmReady = s.status === 'PPS_DONE' || s.status === 'PPM_DONE';
  const terminal = s.status === 'CLOSED' || s.status === 'REJECTED';

  return (
    <div>
      <button onClick={onBack} style={{ marginBottom: 12 }}>
        ← Back to list
      </button>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
        <h3 style={{ margin: 0 }}>
          {s.sampleNo}
          {s.revNum > 0 && <span style={{ color: '#7c3aed' }}> ·R{s.revNum}</span>} — {s.name}
        </h3>
        <Badge status={s.status} />
      </div>
      <div style={{ display: 'flex', gap: 8, margin: '12px 0', flexWrap: 'wrap' }}>
        {s.status === 'DRAFT' && <button onClick={() => act(() => submitSample(id), 'Submitted for review.')}>Submit for review</button>}
        {s.status === 'IN_REVIEW' && isAdmin && (
          <>
            <button onClick={() => act(() => selectSample(id), 'Sample selected.')} style={{ color: '#1e7e34' }}>
              Select
            </button>
            <button onClick={() => act(() => rejectSample(id), 'Sample rejected.')} style={{ color: '#c0392b' }}>
              Reject
            </button>
          </>
        )}
        {canEdit && <button onClick={() => onEdit(id)}>Edit</button>}
        {!terminal && s.status !== 'DRAFT' && (
          <button onClick={() => act(() => reviseSample(id).then((rev) => { onOpen(rev.id); return rev; }), 'New revision created.')}>
            Create revision
          </button>
        )}
        {!terminal && isAdmin && (
          <button onClick={() => act(() => closeSample(id), 'Sample closed.')} style={{ color: '#6b7280' }}>
            Close
          </button>
        )}
      </div>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      <div style={{ display: 'flex', gap: 6, borderBottom: '1px solid #eee', marginBottom: 12 }}>
        {(['info', 'pps', 'ppm'] as const).map((k) => (
          <button
            key={k}
            onClick={() => setSection(k)}
            style={{
              border: 'none',
              background: 'none',
              padding: '6px 10px',
              cursor: 'pointer',
              fontWeight: section === k ? 700 : 400,
              borderBottom: section === k ? '2px solid #7c3aed' : '2px solid transparent',
            }}
          >
            {k === 'info' ? 'Info' : k.toUpperCase()}
          </button>
        ))}
      </div>

      {section === 'info' && <InfoSection sample={s} onChanged={reload} />}
      {section === 'pps' && <PpsSection sample={s} editable={ppsReady} onSaved={setSample} onError={setError} onChanged={reload} />}
      {section === 'ppm' && <PpmEditor sample={s} editable={ppmReady} onSaved={setSample} onError={setError} onChanged={reload} />}
    </div>
  );
}

function InfoSection({ sample, onChanged }: { sample: SampleDetail; onChanged: () => void }) {
  const s = sample;
  return (
    <div>
      <div style={card}>
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap', fontSize: 14 }}>
          <div><strong>Style:</strong> {s.style ?? '—'}</div>
          <div><strong>Category:</strong> {s.category ?? '—'}</div>
          <div><strong>Designer:</strong> {s.designer ?? '—'}</div>
          <div><strong>Reference:</strong> {s.reference ?? '—'}</div>
          <div><strong>Created by:</strong> {s.createdBy ?? '—'}</div>
        </div>
        {s.notes && <p style={{ marginBottom: 0 }}><strong>Notes:</strong> {s.notes}</p>}
      </div>
      <LineTable title="Raw materials" rows={s.rawMaterials} withColour />
      <LineTable title="Semi-finished items" rows={s.sfgItems} />
      <LineTable title="Operations" rows={s.operations} nameOnly />
      <PhotoStrip sampleId={s.id} section="FORM" photos={s.photos.filter((p) => p.section === 'FORM')} onChanged={onChanged} />
    </div>
  );
}

function LineTable({ title, rows, withColour, nameOnly }: { title: string; rows: SampleDetail['rawMaterials']; withColour?: boolean; nameOnly?: boolean }) {
  if (rows.length === 0) return null;
  return (
    <div style={{ marginBottom: 12 }}>
      <div style={{ fontWeight: 600, marginBottom: 4 }}>{title}</div>
      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>Name</th>
            {!nameOnly && <th style={th}>Description</th>}
            {withColour && <th style={th}>Colour</th>}
            {!nameOnly && <th style={{ ...th, textAlign: 'right' }}>Qty</th>}
            {!nameOnly && <th style={th}>Unit</th>}
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.id}>
              <td style={td}>{r.name ?? '—'}</td>
              {!nameOnly && <td style={td}>{r.description ?? '—'}</td>}
              {withColour && <td style={td}>{r.colour ?? '—'}</td>}
              {!nameOnly && <td style={{ ...td, textAlign: 'right' }}>{r.qty ?? '—'}</td>}
              {!nameOnly && <td style={td}>{r.unit ?? '—'}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function PhotoStrip({
  sampleId,
  section,
  photos,
  onChanged,
}: {
  sampleId: number;
  section: 'FORM' | 'PPS' | PpmSectionKey;
  photos: SampleDetail['photos'];
  onChanged: () => void;
}) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onFiles(files: FileList | null) {
    if (!files || files.length === 0) return;
    setBusy(true);
    setError(null);
    try {
      for (const f of Array.from(files)) {
        const dataUrl = await compressToDataUrl(f);
        await addPhoto(sampleId, section, dataUrl);
      }
      onChanged();
    } catch (err) {
      setError(extractError(err, 'Could not upload photo.'));
    } finally {
      setBusy(false);
    }
  }

  async function remove(photoId: number) {
    setError(null);
    try {
      await deletePhoto(sampleId, photoId);
      onChanged();
    } catch (err) {
      setError(extractError(err, 'Could not delete photo.'));
    }
  }

  return (
    <div style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
        <span style={{ fontWeight: 600 }}>Photos</span>
        <label style={{ fontSize: 13, cursor: 'pointer', color: '#7c3aed' }}>
          {busy ? 'Uploading…' : '+ add'}
          <input type="file" accept="image/*" multiple style={{ display: 'none' }} onChange={(e) => onFiles(e.target.files)} disabled={busy} />
        </label>
      </div>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        {photos.map((p) => (
          <div key={p.id} style={{ position: 'relative' }}>
            <img src={p.dataUrl} alt={p.caption ?? ''} style={{ width: 90, height: 90, objectFit: 'cover', borderRadius: 6, border: '1px solid #ddd' }} />
            <button
              onClick={() => remove(p.id)}
              style={{ position: 'absolute', top: -8, right: -8, background: '#fff', border: '1px solid #ddd', borderRadius: '50%', width: 22, height: 22, cursor: 'pointer' }}
            >
              ✕
            </button>
          </div>
        ))}
        {photos.length === 0 && <span style={{ color: '#9ca3af', fontSize: 13 }}>No photos.</span>}
      </div>
    </div>
  );
}

function PpsSection({
  sample,
  editable,
  onSaved,
  onError,
  onChanged,
}: {
  sample: SampleDetail;
  editable: boolean;
  onSaved: (s: SampleDetail) => void;
  onError: (e: string) => void;
  onChanged: () => void;
}) {
  const [fabric, setFabric] = useState(sample.pps.fabricDetails ?? '');
  const [designCount, setDesignCount] = useState<string>(sample.pps.designCount != null ? String(sample.pps.designCount) : '');
  const [instructions, setInstructions] = useState(sample.pps.specialInstructions ?? '');
  const [colours, setColours] = useState<string[]>(sample.pps.colours.length ? sample.pps.colours : ['']);
  const [sizes, setSizes] = useState<string[]>(sample.pps.sizes.length ? sample.pps.sizes : ['']);
  const [busy, setBusy] = useState(false);
  const approved = !!sample.pps.approvedAt;

  async function save(approve: boolean) {
    setBusy(true);
    try {
      const updated = await savePps(
        sample.id,
        {
          fabricDetails: fabric || undefined,
          designCount: designCount ? Number(designCount) : undefined,
          specialInstructions: instructions || undefined,
          colours: colours.map((c) => c.trim()).filter(Boolean),
          sizes: sizes.map((c) => c.trim()).filter(Boolean),
        },
        approve,
      );
      onSaved(updated);
    } catch (err) {
      onError(extractError(err, 'Could not save PPS.'));
    } finally {
      setBusy(false);
    }
  }

  if (!editable) {
    return <p style={{ color: '#9ca3af' }}>PPS becomes available once the sample is selected.</p>;
  }

  return (
    <div>
      {approved && (
        <p style={{ color: '#8b5cf6' }}>
          ✓ PPS approved by {sample.pps.approvedBy} — you can still update it below.
        </p>
      )}
      <div style={card}>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <Field label="Fabric details *" grow>
            <input value={fabric} onChange={(e) => setFabric(e.target.value)} style={{ ...input, width: '100%' }} />
          </Field>
          <Field label="No. of designs *">
            <input type="number" min="0" value={designCount} onChange={(e) => setDesignCount(e.target.value)} style={{ ...input, width: 100 }} />
          </Field>
          <Field label="Special instructions" grow>
            <input value={instructions} onChange={(e) => setInstructions(e.target.value)} style={{ ...input, width: '100%' }} />
          </Field>
        </div>
        <div style={{ display: 'flex', gap: 24, marginTop: 12, flexWrap: 'wrap' }}>
          <TagList label="Colours" items={colours} setItems={setColours} />
          <TagList label="Sizes" items={sizes} setItems={setSizes} />
        </div>
      </div>
      <PhotoStrip sampleId={sample.id} section="PPS" photos={sample.photos.filter((p) => p.section === 'PPS')} onChanged={onChanged} />
      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={() => save(false)} disabled={busy}>
          Save PPS draft
        </button>
        <button
          onClick={() => save(true)}
          disabled={busy}
          style={{ background: '#8b5cf6', color: '#fff', border: 'none', padding: '8px 14px', borderRadius: 6 }}
        >
          {approved ? 'Re-approve PPS' : 'Approve PPS'}
        </button>
      </div>
    </div>
  );
}

function TagList({ label, items, setItems }: { label: string; items: string[]; setItems: (fn: (prev: string[]) => string[]) => void }) {
  return (
    <div>
      <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 4 }}>{label}</div>
      {items.map((v, i) => (
        <div key={i} style={{ display: 'flex', gap: 4, marginBottom: 4 }}>
          <input value={v} onChange={(e) => setItems((prev) => prev.map((x, j) => (j === i ? e.target.value : x)))} style={{ ...input, width: 120 }} />
          <button type="button" onClick={() => setItems((prev) => prev.filter((_, j) => j !== i))}>
            ✕
          </button>
        </div>
      ))}
      <button type="button" onClick={() => setItems((prev) => [...prev, ''])}>
        + add
      </button>
    </div>
  );
}

function PpmEditor({
  sample,
  editable,
  onSaved,
  onError,
  onChanged,
}: {
  sample: SampleDetail;
  editable: boolean;
  onSaved: (s: SampleDetail) => void;
  onError: (e: string) => void;
  onChanged: () => void;
}) {
  const [active, setActive] = useState<PpmSectionKey>('SOP');
  const meta = useMemo(() => PPM_SECTIONS.find((m) => m.key === active)!, [active]);
  const initial = sample.ppm[active.toLowerCase() as 'sop'] ?? [];
  const [rows, setRows] = useState(initial.map((r) => ({ text1: r.text1 ?? '', text2: r.text2 ?? '', remark: r.remark ?? '' })));
  const [busy, setBusy] = useState(false);

  // Reset the editable rows when switching section or when the sample reloads.
  useEffect(() => {
    const cur = sample.ppm[active.toLowerCase() as 'sop'] ?? [];
    setRows(cur.map((r) => ({ text1: r.text1 ?? '', text2: r.text2 ?? '', remark: r.remark ?? '' })));
  }, [active, sample]);

  async function save() {
    setBusy(true);
    try {
      const updated = await savePpm(sample.id, active, rows.filter((r) => r.text1 || r.text2 || r.remark));
      onSaved(updated);
    } catch (err) {
      onError(extractError(err, 'Could not save PPM section.'));
    } finally {
      setBusy(false);
    }
  }

  async function complete() {
    setBusy(true);
    try {
      const updated = await completePpm(sample.id);
      onSaved(updated);
    } catch (err) {
      onError(extractError(err, 'Could not complete PPM.'));
    } finally {
      setBusy(false);
    }
  }

  if (!editable) {
    return <p style={{ color: '#9ca3af' }}>PPM becomes available once the PPS is approved.</p>;
  }

  const photoSection = active;

  return (
    <div>
      <div style={{ display: 'flex', gap: 6, marginBottom: 12, flexWrap: 'wrap' }}>
        {PPM_SECTIONS.map((m) => (
          <button
            key={m.key}
            onClick={() => setActive(m.key)}
            style={{
              padding: '4px 10px',
              borderRadius: 6,
              border: '1px solid #ddd',
              cursor: 'pointer',
              background: active === m.key ? '#ede9fe' : '#fff',
              color: active === m.key ? '#7c3aed' : '#333',
              fontWeight: active === m.key ? 700 : 400,
            }}
          >
            {m.label} ({(sample.ppm[m.key.toLowerCase() as 'sop'] ?? []).length})
          </button>
        ))}
      </div>
      <div style={card}>
        <div style={{ fontWeight: 600, marginBottom: 8 }}>{meta.label}</div>
        {rows.map((r, i) => (
          <div key={i} style={{ display: 'flex', gap: 6, marginBottom: 6, flexWrap: 'wrap' }}>
            <input placeholder={meta.col1} value={r.text1} onChange={(e) => setRows((prev) => prev.map((x, j) => (j === i ? { ...x, text1: e.target.value } : x)))} style={{ ...input, flex: 1, minWidth: 130 }} />
            <input placeholder={meta.col2} value={r.text2} onChange={(e) => setRows((prev) => prev.map((x, j) => (j === i ? { ...x, text2: e.target.value } : x)))} style={{ ...input, flex: 1, minWidth: 130 }} />
            <input placeholder="Remark" value={r.remark} onChange={(e) => setRows((prev) => prev.map((x, j) => (j === i ? { ...x, remark: e.target.value } : x)))} style={{ ...input, flex: 1, minWidth: 130 }} />
            <button type="button" onClick={() => setRows((prev) => prev.filter((_, j) => j !== i))}>
              ✕
            </button>
          </div>
        ))}
        <button type="button" onClick={() => setRows((prev) => [...prev, { text1: '', text2: '', remark: '' }])}>
          + add row
        </button>
      </div>
      <PhotoStrip sampleId={sample.id} section={photoSection} photos={sample.photos.filter((p) => p.section === photoSection)} onChanged={onChanged} />
      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={save} disabled={busy}>
          Save {meta.label}
        </button>
        <button
          onClick={complete}
          disabled={busy}
          style={{ background: '#10b981', color: '#fff', border: 'none', padding: '8px 14px', borderRadius: 6 }}
        >
          {sample.status === 'PPM_DONE' ? 'PPM complete ✓' : 'Mark PPM complete'}
        </button>
      </div>
    </div>
  );
}

// ===========================================================================
// Requests
// ===========================================================================

function RequestsTab() {
  const { auth } = useAuth();
  const isAdmin = auth?.role === 'ADMIN';
  const [requests, setRequests] = useState<SampleRequest[]>([]);
  const [samples, setSamples] = useState<SampleSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  // New-request form (admin)
  const [reqType, setReqType] = useState<'NEW' | 'CHANGE'>('NEW');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState('NORMAL');
  const [refSampleId, setRefSampleId] = useState('');

  function reload() {
    listRequests().then(setRequests).catch(() => setError('Could not load requests.'));
  }
  useEffect(reload, []);
  useEffect(() => {
    listSamples(undefined, true).then(setSamples).catch(() => undefined);
  }, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    try {
      await createRequest({
        reqType,
        title: title.trim(),
        description: description || undefined,
        priority,
        refSampleId: reqType === 'CHANGE' && refSampleId ? Number(refSampleId) : undefined,
      });
      setMessage('Request sent to sampler.');
      setTitle('');
      setDescription('');
      setRefSampleId('');
      reload();
    } catch (err) {
      setError(extractError(err, 'Could not create request.'));
    }
  }

  async function act(fn: () => Promise<SampleRequest>, ok: string) {
    setError(null);
    setMessage(null);
    try {
      await fn();
      setMessage(ok);
      reload();
    } catch (err) {
      setError(extractError(err, 'Action failed.'));
    }
  }

  async function complete(r: SampleRequest) {
    const options = samples.filter((s) => s.status !== 'CLOSED' && s.status !== 'REJECTED');
    const choice = window.prompt(
      `Link the produced sample to complete "${r.title}".\nEnter a sample number:\n` +
        options.map((s) => `  ${s.sampleNo} — ${s.name}`).join('\n'),
    );
    if (!choice) return;
    const match = samples.find((s) => s.sampleNo.toLowerCase() === choice.trim().toLowerCase());
    if (!match) {
      setError(`No sample matches "${choice}".`);
      return;
    }
    act(() => completeRequest(r.id, match.id), `Request completed with ${match.sampleNo}.`);
  }

  return (
    <div>
      {isAdmin && (
        <form onSubmit={submit} style={card}>
          <div style={{ fontWeight: 600, marginBottom: 8 }}>New request to sampler</div>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <Field label="Type">
              <select value={reqType} onChange={(e) => setReqType(e.target.value as 'NEW' | 'CHANGE')} style={input}>
                <option value="NEW">New sample</option>
                <option value="CHANGE">Change existing</option>
              </select>
            </Field>
            {reqType === 'CHANGE' && (
              <Field label="Sample to change">
                <select value={refSampleId} onChange={(e) => setRefSampleId(e.target.value)} style={input} required>
                  <option value="">Select…</option>
                  {samples.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.sampleNo} — {s.name}
                    </option>
                  ))}
                </select>
              </Field>
            )}
            <Field label="Title *" grow>
              <input value={title} onChange={(e) => setTitle(e.target.value)} required style={{ ...input, width: '100%' }} />
            </Field>
            <Field label="Priority">
              <select value={priority} onChange={(e) => setPriority(e.target.value)} style={input}>
                <option value="LOW">Low</option>
                <option value="NORMAL">Normal</option>
                <option value="HIGH">High</option>
              </select>
            </Field>
            <Field label="Description" grow>
              <input value={description} onChange={(e) => setDescription(e.target.value)} style={{ ...input, width: '100%' }} />
            </Field>
            <button type="submit" style={{ background: '#7c3aed', color: '#fff', border: 'none', padding: '8px 14px', borderRadius: 6 }}>
              Send request
            </button>
          </div>
        </form>
      )}
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>Type</th>
            <th style={th}>Title</th>
            <th style={th}>Ref / Result</th>
            <th style={th}>Priority</th>
            <th style={th}>Status</th>
            <th style={th}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((r) => (
            <tr key={r.id}>
              <td style={td}>{r.reqType}</td>
              <td style={td}>
                {r.title}
                {r.description && <div style={{ color: '#9ca3af', fontSize: 12 }}>{r.description}</div>}
              </td>
              <td style={td}>
                {r.refSampleNo && <div>ref: {r.refSampleNo}</div>}
                {r.completedSampleNo && <div>→ {r.completedSampleNo}</div>}
                {r.adminResolvedRemark && (
                  <div style={{ color: '#9ca3af', fontSize: 12 }}>resolved: {r.adminResolvedRemark.toLowerCase().replace(/_/g, ' ')}</div>
                )}
              </td>
              <td style={td}>{r.priority}</td>
              <td style={td}>{r.status.replace(/_/g, ' ')}</td>
              <td style={td}>
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                  {r.status === 'PENDING' && <button onClick={() => act(() => startRequest(r.id), 'Marked in progress.')}>Start</button>}
                  {(r.status === 'PENDING' || r.status === 'IN_PROGRESS') && <button onClick={() => complete(r)}>Complete…</button>}
                  {isAdmin && r.status !== 'COMPLETED' && r.status !== 'CANCELLED' && (
                    <button onClick={() => act(() => cancelRequest(r.id), 'Request cancelled.')} style={{ color: '#c0392b' }}>
                      Cancel
                    </button>
                  )}
                </div>
              </td>
            </tr>
          ))}
          {requests.length === 0 && (
            <tr>
              <td style={td} colSpan={6}>
                No requests.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
