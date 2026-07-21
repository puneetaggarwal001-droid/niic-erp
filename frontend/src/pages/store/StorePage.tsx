import { useEffect, useMemo, useState } from 'react';
import {
  addCategory,
  approveItem,
  createBom,
  createIssuance,
  createItem,
  createPurchaseOrder,
  createRequisition,
  deleteBom,
  fulfilRequisition,
  listBoms,
  listCategories,
  listIssuances,
  listItems,
  listLedger,
  listPendingItems,
  listPurchaseOrders,
  listRequisitions,
  markPoOrdered,
  receivePo,
  recordStock,
  rejectItem,
  runMrp,
  type Bom,
  type Issuance,
  type ItemType,
  type MrpResponse,
  type PurchaseOrder,
  type Requisition,
  type StockTxn,
  type StockTxnType,
  type StoreCategory,
  type StoreItem,
} from '../../api/store';
import { listJobs, listWorkstations } from '../../api/production';
import type { Job, Workstation } from '../../api/productionTypes';

const th: React.CSSProperties = { textAlign: 'left', borderBottom: '1px solid #ddd', padding: '4px 8px' };
const td: React.CSSProperties = { borderBottom: '1px solid #f0f0f0', padding: '4px 8px' };
const tdNum: React.CSSProperties = { ...td, textAlign: 'right' };
const input: React.CSSProperties = { padding: 6 };

function num(n: number | null | undefined): string {
  if (n === null || n === undefined) return '—';
  return n.toLocaleString('en-IN', { maximumFractionDigits: 3 });
}

function extractError(err: unknown, fallback: string): string {
  const anyErr = err as { response?: { data?: { message?: string } } };
  return anyErr?.response?.data?.message ?? fallback;
}

type Tab = 'items' | 'approvals' | 'bom' | 'mrp' | 'po' | 'issuance' | 'requisitions';

export default function StorePage() {
  const [tab, setTab] = useState<Tab>('items');
  const tabs: { key: Tab; label: string }[] = [
    { key: 'items', label: 'Items & Stock' },
    { key: 'approvals', label: 'Approvals' },
    { key: 'bom', label: 'BOM' },
    { key: 'mrp', label: 'MRP' },
    { key: 'po', label: 'Purchase Orders' },
    { key: 'issuance', label: 'Issuance' },
    { key: 'requisitions', label: 'Requisitions' },
  ];

  return (
    <div>
      <h2>Store &amp; Inventory</h2>
      <div style={{ display: 'flex', gap: 8, borderBottom: '2px solid #eee', marginBottom: 16, flexWrap: 'wrap' }}>
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
              borderBottom: tab === t.key ? '3px solid #1B2E72' : '3px solid transparent',
            }}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'items' && <ItemsTab />}
      {tab === 'approvals' && <ApprovalsTab />}
      {tab === 'bom' && <BomTab />}
      {tab === 'mrp' && <MrpTab />}
      {tab === 'po' && <PurchaseOrdersTab />}
      {tab === 'issuance' && <IssuanceTab />}
      {tab === 'requisitions' && <RequisitionsTab />}
    </div>
  );
}

// ---- Items & Stock ---------------------------------------------------------

const TXN_TYPES: StockTxnType[] = ['INWARD', 'ISSUE', 'RETURN', 'REJECT', 'ADJUST'];

function ItemsTab() {
  const [filter, setFilter] = useState<'' | ItemType>('');
  const [items, setItems] = useState<StoreItem[]>([]);
  const [categories, setCategories] = useState<StoreCategory[]>([]);
  const [selected, setSelected] = useState<StoreItem | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  // Create-item form
  const [name, setName] = useState('');
  const [itemType, setItemType] = useState<ItemType>('RM');
  const [unit, setUnit] = useState('PCS');
  const [categoryId, setCategoryId] = useState('');
  const [reorderLevel, setReorderLevel] = useState('');
  const [variants, setVariants] = useState<{ colour: string; size: string }[]>([]);

  // Inline add-category
  const [catName, setCatName] = useState('');
  const [catCode, setCatCode] = useState('');

  function refresh() {
    listItems(filter || undefined)
      .then(setItems)
      .catch(() => setError('Could not load items.'));
  }
  useEffect(refresh, [filter]);
  useEffect(() => {
    listCategories().then(setCategories).catch(() => undefined);
  }, []);

  async function submitItem(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    try {
      const created = await createItem({
        name: name.trim(),
        itemType,
        unit: unit.trim() || 'PCS',
        categoryId: categoryId ? Number(categoryId) : undefined,
        reorderLevel: reorderLevel ? Number(reorderLevel) : undefined,
        variants: itemType === 'FG' && variants.length ? variants : undefined,
      });
      setMessage(
        `Item ${created.itemCode} created${created.approvalStatus === 'PENDING_APPROVAL' ? ' (pending approval)' : ''}.`,
      );
      setName('');
      setReorderLevel('');
      setVariants([]);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not create item.'));
    }
  }

  async function submitCategory(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const c = await addCategory(catName.trim(), catCode.trim());
      setCategories((prev) => [...prev, c].sort((a, b) => a.name.localeCompare(b.name)));
      setCatName('');
      setCatCode('');
    } catch (err) {
      setError(extractError(err, 'Could not add category.'));
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
        <form onSubmit={submitItem} style={{ display: 'grid', gap: 8, maxWidth: 320 }}>
          <h3 style={{ margin: '0 0 4px' }}>New item</h3>
          <label style={{ fontSize: 13 }}>
            Name
            <br />
            <input value={name} onChange={(e) => setName(e.target.value)} required style={{ ...input, width: '100%' }} />
          </label>
          <div style={{ display: 'flex', gap: 8 }}>
            <label style={{ fontSize: 13, flex: 1 }}>
              Type
              <br />
              <select value={itemType} onChange={(e) => setItemType(e.target.value as ItemType)} style={{ ...input, width: '100%' }}>
                <option value="RM">Raw material</option>
                <option value="SFG">Semi-finished</option>
                <option value="FG">Finished good</option>
              </select>
            </label>
            <label style={{ fontSize: 13, width: 90 }}>
              Unit
              <br />
              <input value={unit} onChange={(e) => setUnit(e.target.value)} style={{ ...input, width: '100%' }} />
            </label>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <label style={{ fontSize: 13, flex: 1 }}>
              Category
              <br />
              <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} style={{ ...input, width: '100%' }}>
                <option value="">—</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.code})
                  </option>
                ))}
              </select>
            </label>
            <label style={{ fontSize: 13, width: 90 }}>
              Reorder
              <br />
              <input
                type="number"
                min="0"
                step="0.001"
                value={reorderLevel}
                onChange={(e) => setReorderLevel(e.target.value)}
                style={{ ...input, width: '100%' }}
              />
            </label>
          </div>
          {itemType === 'FG' && (
            <div style={{ border: '1px solid #eee', padding: 8, borderRadius: 4 }}>
              <div style={{ fontSize: 13, marginBottom: 4, fontWeight: 600 }}>Variants (colour / size)</div>
              {variants.map((v, i) => (
                <div key={i} style={{ display: 'flex', gap: 4, marginBottom: 4 }}>
                  <input
                    placeholder="Colour"
                    value={v.colour}
                    onChange={(e) =>
                      setVariants((prev) => prev.map((x, j) => (j === i ? { ...x, colour: e.target.value } : x)))
                    }
                    style={{ ...input, width: '50%' }}
                  />
                  <input
                    placeholder="Size"
                    value={v.size}
                    onChange={(e) =>
                      setVariants((prev) => prev.map((x, j) => (j === i ? { ...x, size: e.target.value } : x)))
                    }
                    style={{ ...input, width: '35%' }}
                  />
                  <button type="button" onClick={() => setVariants((prev) => prev.filter((_, j) => j !== i))}>
                    ×
                  </button>
                </div>
              ))}
              <button type="button" onClick={() => setVariants((prev) => [...prev, { colour: '', size: '' }])}>
                + variant
              </button>
            </div>
          )}
          <button type="submit">Create item</button>
        </form>

        <form onSubmit={submitCategory} style={{ display: 'grid', gap: 8, maxWidth: 220, alignContent: 'start' }}>
          <h3 style={{ margin: '0 0 4px' }}>New category</h3>
          <label style={{ fontSize: 13 }}>
            Name
            <br />
            <input value={catName} onChange={(e) => setCatName(e.target.value)} required style={{ ...input, width: '100%' }} />
          </label>
          <label style={{ fontSize: 13 }}>
            Code
            <br />
            <input value={catCode} onChange={(e) => setCatCode(e.target.value)} required style={{ ...input, width: '100%' }} />
          </label>
          <button type="submit">Add category</button>
        </form>
      </div>

      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      <div style={{ display: 'flex', gap: 12, alignItems: 'center', margin: '16px 0 8px' }}>
        <h3 style={{ margin: 0 }}>Items</h3>
        <select value={filter} onChange={(e) => setFilter(e.target.value as '' | ItemType)} style={input}>
          <option value="">All types</option>
          <option value="RM">Raw material</option>
          <option value="SFG">Semi-finished</option>
          <option value="FG">Finished good</option>
        </select>
      </div>

      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>Code</th>
            <th style={th}>Name</th>
            <th style={th}>Type</th>
            <th style={th}>Unit</th>
            <th style={th}>Category</th>
            <th style={{ ...th, textAlign: 'right' }}>On hand</th>
            <th style={{ ...th, textAlign: 'right' }}>Reorder</th>
            <th style={th}>Status</th>
            <th style={th}></th>
          </tr>
        </thead>
        <tbody>
          {items.map((it) => (
            <tr key={it.id} style={it.belowReorder ? { background: '#fff5f5' } : undefined}>
              <td style={td}>{it.itemCode}</td>
              <td style={td}>{it.name}</td>
              <td style={td}>{it.itemType}</td>
              <td style={td}>{it.unit}</td>
              <td style={td}>{it.categoryName ?? '—'}</td>
              <td style={{ ...tdNum, color: it.belowReorder ? '#c0392b' : undefined, fontWeight: it.belowReorder ? 700 : 400 }}>
                {num(it.onHand)}
              </td>
              <td style={tdNum}>{num(it.reorderLevel)}</td>
              <td style={td}>{it.approvalStatus === 'APPROVED' ? '✓' : it.approvalStatus === 'PENDING_APPROVAL' ? 'Pending' : 'Rejected'}</td>
              <td style={td}>
                <button onClick={() => setSelected(it)}>Stock</button>
              </td>
            </tr>
          ))}
          {items.length === 0 && (
            <tr>
              <td style={td} colSpan={9}>
                No items.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      {selected && <StockPanel item={selected} onClose={() => setSelected(null)} onChanged={refresh} />}
    </div>
  );
}

function StockPanel({ item, onClose, onChanged }: { item: StoreItem; onClose: () => void; onChanged: () => void }) {
  const [ledger, setLedger] = useState<StockTxn[]>([]);
  const [txnType, setTxnType] = useState<StockTxnType>('INWARD');
  const [variantId, setVariantId] = useState('');
  const [quantity, setQuantity] = useState('');
  const [reference, setReference] = useState('');
  const [note, setNote] = useState('');
  const [error, setError] = useState<string | null>(null);

  function refresh() {
    listLedger(item.id).then(setLedger).catch(() => setError('Could not load ledger.'));
  }
  useEffect(refresh, [item.id]);

  // Derive on-hand from the ledger so the header stays correct after each
  // recorded transaction (the `item` prop is a snapshot from when the panel
  // opened and does not update).
  const onHand = ledger.reduce((sum, t) => sum + t.quantity, 0);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await recordStock({
        itemId: item.id,
        variantId: variantId ? Number(variantId) : undefined,
        txnType,
        quantity: Number(quantity),
        reference: reference || undefined,
        note: note || undefined,
      });
      setQuantity('');
      setReference('');
      setNote('');
      refresh();
      onChanged();
    } catch (err) {
      setError(extractError(err, 'Could not record transaction.'));
    }
  }

  return (
    <div style={{ marginTop: 24, border: '1px solid #ddd', borderRadius: 6, padding: 16 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3 style={{ margin: 0 }}>
          {item.itemCode} — {item.name} <span style={{ fontWeight: 400 }}>(on hand {num(onHand)} {item.unit})</span>
        </h3>
        <button onClick={onClose}>Close</button>
      </div>

      {item.approvalStatus !== 'APPROVED' ? (
        <p style={{ color: '#c0392b' }}>Item is {item.approvalStatus.toLowerCase().replace('_', ' ')}; stock cannot be transacted.</p>
      ) : (
        <form onSubmit={submit} style={{ display: 'flex', gap: 8, alignItems: 'flex-end', flexWrap: 'wrap', margin: '12px 0' }}>
          <label style={{ fontSize: 13 }}>
            Type
            <br />
            <select value={txnType} onChange={(e) => setTxnType(e.target.value as StockTxnType)} style={input}>
              {TXN_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </label>
          {item.variants.length > 0 && (
            <label style={{ fontSize: 13 }}>
              Variant
              <br />
              <select value={variantId} onChange={(e) => setVariantId(e.target.value)} style={input}>
                <option value="">—</option>
                {item.variants.map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.colour} / {v.size}
                  </option>
                ))}
              </select>
            </label>
          )}
          <label style={{ fontSize: 13 }}>
            Quantity {txnType === 'ADJUST' && <span style={{ color: '#888' }}>(±)</span>}
            <br />
            <input
              type="number"
              step="0.001"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              required
              style={{ ...input, width: 110 }}
            />
          </label>
          <label style={{ fontSize: 13 }}>
            Reference
            <br />
            <input value={reference} onChange={(e) => setReference(e.target.value)} style={input} />
          </label>
          <label style={{ fontSize: 13 }}>
            Note
            <br />
            <input value={note} onChange={(e) => setNote(e.target.value)} style={input} />
          </label>
          <button type="submit">Record</button>
        </form>
      )}
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}

      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>Date</th>
            <th style={th}>Type</th>
            <th style={th}>Variant</th>
            <th style={{ ...th, textAlign: 'right' }}>Qty</th>
            <th style={th}>Reference</th>
            <th style={th}>Note</th>
            <th style={th}>By</th>
          </tr>
        </thead>
        <tbody>
          {ledger.map((t) => (
            <tr key={t.id}>
              <td style={td}>{t.txnDate}</td>
              <td style={td}>{t.txnType}</td>
              <td style={td}>{t.variantLabel ?? '—'}</td>
              <td style={{ ...tdNum, color: t.quantity < 0 ? '#c0392b' : '#1e7e34' }}>{num(t.quantity)}</td>
              <td style={td}>{t.reference ?? '—'}</td>
              <td style={td}>{t.note ?? '—'}</td>
              <td style={td}>{t.createdByUsername ?? '—'}</td>
            </tr>
          ))}
          {ledger.length === 0 && (
            <tr>
              <td style={td} colSpan={7}>
                No transactions.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

// ---- Approvals -------------------------------------------------------------

function ApprovalsTab() {
  const [pending, setPending] = useState<StoreItem[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  function refresh() {
    listPendingItems().then(setPending).catch(() => setError('Could not load pending items.'));
  }
  useEffect(refresh, []);

  async function approve(it: StoreItem) {
    setError(null);
    setMessage(null);
    try {
      await approveItem(it.id);
      setMessage(`${it.itemCode} approved.`);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not approve.'));
    }
  }

  async function reject(it: StoreItem) {
    const reason = window.prompt(`Reject ${it.itemCode} — reason?`) ?? '';
    setError(null);
    setMessage(null);
    try {
      await rejectItem(it.id, reason);
      setMessage(`${it.itemCode} rejected.`);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not reject.'));
    }
  }

  return (
    <div>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}
      <table style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th style={th}>Code</th>
            <th style={th}>Name</th>
            <th style={th}>Type</th>
            <th style={th}>Unit</th>
            <th style={th}>Category</th>
            <th style={th}></th>
          </tr>
        </thead>
        <tbody>
          {pending.map((it) => (
            <tr key={it.id}>
              <td style={td}>{it.itemCode}</td>
              <td style={td}>{it.name}</td>
              <td style={td}>{it.itemType}</td>
              <td style={td}>{it.unit}</td>
              <td style={td}>{it.categoryName ?? '—'}</td>
              <td style={td}>
                <button onClick={() => approve(it)} style={{ color: '#1e7e34', marginRight: 8 }}>
                  Approve
                </button>
                <button onClick={() => reject(it)} style={{ color: '#c0392b' }}>
                  Reject
                </button>
              </td>
            </tr>
          ))}
          {pending.length === 0 && (
            <tr>
              <td style={td} colSpan={6}>
                Nothing pending approval.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

// ---- BOM -------------------------------------------------------------------

function BomTab() {
  const [items, setItems] = useState<StoreItem[]>([]);
  const [outputItemId, setOutputItemId] = useState('');
  const [boms, setBoms] = useState<Bom[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  // New BOM form
  const [colour, setColour] = useState('');
  const [size, setSize] = useState('');
  const [batchQty, setBatchQty] = useState('1');
  const [components, setComponents] = useState<{ componentItemId: string; quantity: string }[]>([
    { componentItemId: '', quantity: '' },
  ]);

  useEffect(() => {
    listItems().then(setItems).catch(() => setError('Could not load items.'));
  }, []);

  function refresh() {
    if (!outputItemId) {
      setBoms([]);
      return;
    }
    listBoms(Number(outputItemId)).then(setBoms).catch(() => setError('Could not load BOMs.'));
  }
  useEffect(refresh, [outputItemId]);

  const outputCandidates = useMemo(() => items.filter((i) => i.itemType !== 'RM'), [items]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    const comps = components
      .filter((c) => c.componentItemId && c.quantity)
      .map((c) => ({ componentItemId: Number(c.componentItemId), quantity: Number(c.quantity) }));
    if (comps.length === 0) {
      setError('Add at least one component.');
      return;
    }
    try {
      await createBom({
        outputItemId: Number(outputItemId),
        colour: colour || undefined,
        size: size || undefined,
        batchQty: batchQty ? Number(batchQty) : undefined,
        components: comps,
      });
      setMessage('BOM created.');
      setColour('');
      setSize('');
      setBatchQty('1');
      setComponents([{ componentItemId: '', quantity: '' }]);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not create BOM.'));
    }
  }

  async function remove(b: Bom) {
    setError(null);
    try {
      await deleteBom(b.id);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not delete BOM.'));
    }
  }

  return (
    <div>
      <label style={{ fontSize: 13 }}>
        Output item (SFG/FG)
        <br />
        <select value={outputItemId} onChange={(e) => setOutputItemId(e.target.value)} style={input}>
          <option value="">Select…</option>
          {outputCandidates.map((i) => (
            <option key={i.id} value={i.id}>
              {i.itemCode} — {i.name} ({i.itemType})
            </option>
          ))}
        </select>
      </label>

      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      {outputItemId && (
        <>
          {boms.map((b) => (
            <div key={b.id} style={{ border: '1px solid #eee', borderRadius: 6, padding: 12, marginTop: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong>
                  {b.colour || 'any colour'} / {b.size || 'any size'} — batch {num(b.batchQty)}
                </strong>
                <button onClick={() => remove(b)} style={{ color: '#c0392b' }}>
                  Delete
                </button>
              </div>
              <table style={{ borderCollapse: 'collapse', width: '100%', marginTop: 8 }}>
                <thead>
                  <tr>
                    <th style={th}>Component</th>
                    <th style={th}>Type</th>
                    <th style={{ ...th, textAlign: 'right' }}>Qty / batch</th>
                  </tr>
                </thead>
                <tbody>
                  {b.components.map((c) => (
                    <tr key={c.componentItemId}>
                      <td style={td}>
                        {c.itemCode} — {c.itemName}
                      </td>
                      <td style={td}>{c.itemType}</td>
                      <td style={tdNum}>{num(c.quantity)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ))}
          {boms.length === 0 && <p>No BOMs defined for this item yet.</p>}

          <form onSubmit={submit} style={{ border: '1px solid #ddd', borderRadius: 6, padding: 12, marginTop: 16 }}>
            <h3 style={{ margin: '0 0 8px' }}>New BOM</h3>
            <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
              <label style={{ fontSize: 13 }}>
                Colour
                <br />
                <input value={colour} onChange={(e) => setColour(e.target.value)} placeholder="any" style={input} />
              </label>
              <label style={{ fontSize: 13 }}>
                Size
                <br />
                <input value={size} onChange={(e) => setSize(e.target.value)} placeholder="any" style={input} />
              </label>
              <label style={{ fontSize: 13 }}>
                Batch qty
                <br />
                <input
                  type="number"
                  min="0.001"
                  step="0.001"
                  value={batchQty}
                  onChange={(e) => setBatchQty(e.target.value)}
                  style={{ ...input, width: 90 }}
                />
              </label>
            </div>
            <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4 }}>Components</div>
            {components.map((c, i) => (
              <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
                <select
                  value={c.componentItemId}
                  onChange={(e) =>
                    setComponents((prev) => prev.map((x, j) => (j === i ? { ...x, componentItemId: e.target.value } : x)))
                  }
                  style={{ ...input, flex: 1 }}
                >
                  <option value="">Select component…</option>
                  {items
                    .filter((it) => String(it.id) !== outputItemId)
                    .map((it) => (
                      <option key={it.id} value={it.id}>
                        {it.itemCode} — {it.name} ({it.itemType})
                      </option>
                    ))}
                </select>
                <input
                  type="number"
                  min="0.0001"
                  step="0.0001"
                  placeholder="qty"
                  value={c.quantity}
                  onChange={(e) =>
                    setComponents((prev) => prev.map((x, j) => (j === i ? { ...x, quantity: e.target.value } : x)))
                  }
                  style={{ ...input, width: 100 }}
                />
                <button type="button" onClick={() => setComponents((prev) => prev.filter((_, j) => j !== i))}>
                  ×
                </button>
              </div>
            ))}
            <button type="button" onClick={() => setComponents((prev) => [...prev, { componentItemId: '', quantity: '' }])}>
              + component
            </button>
            <div style={{ marginTop: 8 }}>
              <button type="submit">Create BOM</button>
            </div>
          </form>
        </>
      )}
    </div>
  );
}

// ---- MRP -------------------------------------------------------------------

function MrpTab() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [jobId, setJobId] = useState('');
  const [result, setResult] = useState<MrpResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    listJobs().then(setJobs).catch(() => setError('Could not load jobs.'));
  }, []);

  async function run() {
    if (!jobId) return;
    setError(null);
    setBusy(true);
    try {
      setResult(await runMrp(Number(jobId)));
    } catch (err) {
      setError(extractError(err, 'Could not run MRP.'));
    } finally {
      setBusy(false);
    }
  }

  function statusColour(s: string): string {
    return s === 'OK' ? '#1e7e34' : s === 'PARTIAL' ? '#b8860b' : '#c0392b';
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end', marginBottom: 12 }}>
        <label style={{ fontSize: 13 }}>
          Job
          <br />
          <select value={jobId} onChange={(e) => setJobId(e.target.value)} style={input}>
            <option value="">Select…</option>
            {jobs.map((j) => (
              <option key={j.id} value={j.id}>
                {j.jobDisplayId} — {j.styleCode} {j.modelNo}
              </option>
            ))}
          </select>
        </label>
        <button onClick={run} disabled={!jobId || busy}>
          {busy ? 'Running…' : 'Run MRP'}
        </button>
      </div>
      {error && <p style={{ color: '#c0392b' }}>{error}</p>}

      {result && (
        <div>
          {result.warnings.length > 0 && (
            <div style={{ background: '#fff8e1', border: '1px solid #ffe082', padding: 8, borderRadius: 4, marginBottom: 12 }}>
              {result.warnings.map((w, i) => (
                <div key={i} style={{ fontSize: 13 }}>
                  ⚠ {w}
                </div>
              ))}
            </div>
          )}
          <table style={{ borderCollapse: 'collapse', width: '100%' }}>
            <thead>
              <tr>
                <th style={th}>Item</th>
                <th style={th}>Type</th>
                <th style={th}>Unit</th>
                <th style={{ ...th, textAlign: 'right' }}>Required</th>
                <th style={{ ...th, textAlign: 'right' }}>Available</th>
                <th style={{ ...th, textAlign: 'right' }}>Shortfall</th>
                <th style={th}>Status</th>
              </tr>
            </thead>
            <tbody>
              {result.lines.map((l) => (
                <tr key={l.itemId}>
                  <td style={td}>
                    {l.itemCode} — {l.itemName}
                  </td>
                  <td style={td}>{l.itemType}</td>
                  <td style={td}>{l.unit}</td>
                  <td style={tdNum}>{num(l.required)}</td>
                  <td style={tdNum}>{num(l.available)}</td>
                  <td style={tdNum}>{num(l.shortfall)}</td>
                  <td style={{ ...td, color: statusColour(l.status), fontWeight: 700 }}>{l.status}</td>
                </tr>
              ))}
              {result.lines.length === 0 && (
                <tr>
                  <td style={td} colSpan={7}>
                    No requirements (no BOM resolved).
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

// ---- Purchase Orders -------------------------------------------------------

function PurchaseOrdersTab() {
  const [items, setItems] = useState<StoreItem[]>([]);
  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [supplierName, setSupplierName] = useState('');
  const [notes, setNotes] = useState('');
  const [lines, setLines] = useState<{ itemId: string; quantity: string; rate: string }[]>([
    { itemId: '', quantity: '', rate: '' },
  ]);

  useEffect(() => {
    listItems().then(setItems).catch(() => undefined);
  }, []);

  function refresh() {
    listPurchaseOrders().then(setOrders).catch(() => setError('Could not load purchase orders.'));
  }
  useEffect(refresh, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    const l = lines
      .filter((x) => x.itemId && x.quantity)
      .map((x) => ({ itemId: Number(x.itemId), quantity: Number(x.quantity), rate: x.rate ? Number(x.rate) : undefined }));
    if (l.length === 0) {
      setError('Add at least one line.');
      return;
    }
    try {
      const po = await createPurchaseOrder({ supplierName: supplierName || undefined, notes: notes || undefined, lines: l });
      setMessage(`PO ${po.poNumber} created.`);
      setSupplierName('');
      setNotes('');
      setLines([{ itemId: '', quantity: '', rate: '' }]);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not create PO.'));
    }
  }

  async function order(po: PurchaseOrder) {
    setError(null);
    try {
      await markPoOrdered(po.id);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not mark ordered.'));
    }
  }

  async function receive(po: PurchaseOrder) {
    setError(null);
    setMessage(null);
    // Receive outstanding quantity on every line.
    const receipts = po.items
      .map((it) => ({ poItemId: it.id, quantity: it.orderedQty - it.receivedQty }))
      .filter((r) => r.quantity > 0);
    if (receipts.length === 0) {
      setMessage('Nothing outstanding to receive.');
      return;
    }
    try {
      await receivePo(po.id, receipts);
      setMessage(`PO ${po.poNumber} received into stock.`);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not receive PO.'));
    }
  }

  return (
    <div>
      <form onSubmit={submit} style={{ border: '1px solid #ddd', borderRadius: 6, padding: 12, marginBottom: 16 }}>
        <h3 style={{ margin: '0 0 8px' }}>New purchase order</h3>
        <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
          <label style={{ fontSize: 13, flex: 1 }}>
            Supplier
            <br />
            <input value={supplierName} onChange={(e) => setSupplierName(e.target.value)} style={{ ...input, width: '100%' }} />
          </label>
          <label style={{ fontSize: 13, flex: 2 }}>
            Notes
            <br />
            <input value={notes} onChange={(e) => setNotes(e.target.value)} style={{ ...input, width: '100%' }} />
          </label>
        </div>
        {lines.map((ln, i) => (
          <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
            <select
              value={ln.itemId}
              onChange={(e) => setLines((prev) => prev.map((x, j) => (j === i ? { ...x, itemId: e.target.value } : x)))}
              style={{ ...input, flex: 1 }}
            >
              <option value="">Select item…</option>
              {items.map((it) => (
                <option key={it.id} value={it.id}>
                  {it.itemCode} — {it.name}
                </option>
              ))}
            </select>
            <input
              type="number"
              min="0.001"
              step="0.001"
              placeholder="qty"
              value={ln.quantity}
              onChange={(e) => setLines((prev) => prev.map((x, j) => (j === i ? { ...x, quantity: e.target.value } : x)))}
              style={{ ...input, width: 90 }}
            />
            <input
              type="number"
              min="0"
              step="0.001"
              placeholder="rate"
              value={ln.rate}
              onChange={(e) => setLines((prev) => prev.map((x, j) => (j === i ? { ...x, rate: e.target.value } : x)))}
              style={{ ...input, width: 90 }}
            />
            <button type="button" onClick={() => setLines((prev) => prev.filter((_, j) => j !== i))}>
              ×
            </button>
          </div>
        ))}
        <button type="button" onClick={() => setLines((prev) => [...prev, { itemId: '', quantity: '', rate: '' }])}>
          + line
        </button>
        <div style={{ marginTop: 8 }}>
          <button type="submit">Create PO</button>
        </div>
      </form>

      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      {orders.map((po) => (
        <div key={po.id} style={{ border: '1px solid #eee', borderRadius: 6, padding: 12, marginBottom: 12 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <strong>
              {po.poNumber} — {po.supplierName ?? 'no supplier'} <span style={{ fontWeight: 400 }}>[{po.status}]</span>
            </strong>
            <div style={{ display: 'flex', gap: 8 }}>
              {po.status === 'PENDING' && <button onClick={() => order(po)}>Mark ordered</button>}
              {po.status !== 'RECEIVED' && po.status !== 'CANCELLED' && (
                <button onClick={() => receive(po)} style={{ color: '#1e7e34' }}>
                  Receive all
                </button>
              )}
            </div>
          </div>
          <table style={{ borderCollapse: 'collapse', width: '100%', marginTop: 8 }}>
            <thead>
              <tr>
                <th style={th}>Item</th>
                <th style={{ ...th, textAlign: 'right' }}>Ordered</th>
                <th style={{ ...th, textAlign: 'right' }}>Received</th>
                <th style={{ ...th, textAlign: 'right' }}>Rate</th>
              </tr>
            </thead>
            <tbody>
              {po.items.map((it) => (
                <tr key={it.id}>
                  <td style={td}>
                    {it.itemCode} — {it.itemName}
                  </td>
                  <td style={tdNum}>{num(it.orderedQty)}</td>
                  <td style={tdNum}>{num(it.receivedQty)}</td>
                  <td style={tdNum}>{num(it.rate)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
      {orders.length === 0 && <p>No purchase orders yet.</p>}
    </div>
  );
}

// ---- Issuance --------------------------------------------------------------

function IssuanceTab() {
  const [items, setItems] = useState<StoreItem[]>([]);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [issuances, setIssuances] = useState<Issuance[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [jobId, setJobId] = useState('');
  const [workstationId, setWorkstationId] = useState('');
  const [notes, setNotes] = useState('');
  const [lines, setLines] = useState<{ itemId: string; variantId: string; quantity: string }[]>([
    { itemId: '', variantId: '', quantity: '' },
  ]);

  useEffect(() => {
    listItems().then(setItems).catch(() => undefined);
    listJobs().then(setJobs).catch(() => undefined);
    listWorkstations().then(setWorkstations).catch(() => undefined);
  }, []);

  function refresh() {
    listIssuances().then(setIssuances).catch(() => setError('Could not load issuances.'));
  }
  useEffect(refresh, []);

  function variantsFor(itemId: string) {
    return items.find((i) => String(i.id) === itemId)?.variants ?? [];
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (!workstationId) {
      setError('Select a destination workstation.');
      return;
    }
    const l = lines
      .filter((x) => x.itemId && x.quantity)
      .map((x) => ({ itemId: Number(x.itemId), variantId: x.variantId ? Number(x.variantId) : undefined, quantity: Number(x.quantity) }));
    if (l.length === 0) {
      setError('Add at least one line.');
      return;
    }
    try {
      const iss = await createIssuance({
        jobId: jobId ? Number(jobId) : undefined,
        workstationId: Number(workstationId),
        notes: notes || undefined,
        lines: l,
      });
      setMessage(`Issuance ${iss.issNumber} created.`);
      setNotes('');
      setLines([{ itemId: '', variantId: '', quantity: '' }]);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not create issuance.'));
    }
  }

  return (
    <div>
      <form onSubmit={submit} style={{ border: '1px solid #ddd', borderRadius: 6, padding: 12, marginBottom: 16 }}>
        <h3 style={{ margin: '0 0 8px' }}>New issuance challan</h3>
        <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
          <label style={{ fontSize: 13 }}>
            Job (optional)
            <br />
            <select value={jobId} onChange={(e) => setJobId(e.target.value)} style={input}>
              <option value="">—</option>
              {jobs.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.jobDisplayId} — {j.styleCode}
                </option>
              ))}
            </select>
          </label>
          <label style={{ fontSize: 13 }}>
            To workstation
            <br />
            <select value={workstationId} onChange={(e) => setWorkstationId(e.target.value)} required style={input}>
              <option value="">Select…</option>
              {workstations.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name}
                </option>
              ))}
            </select>
          </label>
          <label style={{ fontSize: 13, flex: 1 }}>
            Notes
            <br />
            <input value={notes} onChange={(e) => setNotes(e.target.value)} style={{ ...input, width: '100%' }} />
          </label>
        </div>
        {lines.map((ln, i) => (
          <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
            <select
              value={ln.itemId}
              onChange={(e) =>
                setLines((prev) => prev.map((x, j) => (j === i ? { ...x, itemId: e.target.value, variantId: '' } : x)))
              }
              style={{ ...input, flex: 1 }}
            >
              <option value="">Select item…</option>
              {items.map((it) => (
                <option key={it.id} value={it.id}>
                  {it.itemCode} — {it.name}
                </option>
              ))}
            </select>
            {variantsFor(ln.itemId).length > 0 && (
              <select
                value={ln.variantId}
                onChange={(e) => setLines((prev) => prev.map((x, j) => (j === i ? { ...x, variantId: e.target.value } : x)))}
                style={input}
              >
                <option value="">variant…</option>
                {variantsFor(ln.itemId).map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.colour}/{v.size}
                  </option>
                ))}
              </select>
            )}
            <input
              type="number"
              min="0.001"
              step="0.001"
              placeholder="qty"
              value={ln.quantity}
              onChange={(e) => setLines((prev) => prev.map((x, j) => (j === i ? { ...x, quantity: e.target.value } : x)))}
              style={{ ...input, width: 90 }}
            />
            <button type="button" onClick={() => setLines((prev) => prev.filter((_, j) => j !== i))}>
              ×
            </button>
          </div>
        ))}
        <button type="button" onClick={() => setLines((prev) => [...prev, { itemId: '', variantId: '', quantity: '' }])}>
          + line
        </button>
        <div style={{ marginTop: 8 }}>
          <button type="submit">Create issuance</button>
        </div>
      </form>

      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      {issuances.map((iss) => (
        <div key={iss.id} style={{ border: '1px solid #eee', borderRadius: 6, padding: 12, marginBottom: 12 }}>
          <strong>
            {iss.issNumber} → {iss.toWorkstationName}
            {iss.jobDisplayId ? ` · ${iss.jobDisplayId}` : ''}
          </strong>
          <table style={{ borderCollapse: 'collapse', width: '100%', marginTop: 8 }}>
            <thead>
              <tr>
                <th style={th}>Item</th>
                <th style={th}>Variant</th>
                <th style={{ ...th, textAlign: 'right' }}>Qty</th>
              </tr>
            </thead>
            <tbody>
              {iss.items.map((it, i) => (
                <tr key={i}>
                  <td style={td}>
                    {it.itemCode} — {it.itemName}
                  </td>
                  <td style={td}>{it.variantLabel ?? '—'}</td>
                  <td style={tdNum}>{num(it.quantity)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
      {issuances.length === 0 && <p>No issuance challans yet.</p>}
    </div>
  );
}

// ---- Requisitions ----------------------------------------------------------

function RequisitionsTab() {
  const [items, setItems] = useState<StoreItem[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [requisitions, setRequisitions] = useState<Requisition[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const [workstationId, setWorkstationId] = useState('');
  const [notes, setNotes] = useState('');
  const [lines, setLines] = useState<{ itemId: string; quantity: string }[]>([{ itemId: '', quantity: '' }]);

  useEffect(() => {
    listItems().then(setItems).catch(() => undefined);
    listWorkstations().then(setWorkstations).catch(() => undefined);
  }, []);

  function refresh() {
    listRequisitions().then(setRequisitions).catch(() => setError('Could not load requisitions.'));
  }
  useEffect(refresh, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (!workstationId) {
      setError('Select the requesting workstation.');
      return;
    }
    const l = lines
      .filter((x) => x.itemId && x.quantity)
      .map((x) => ({ itemId: Number(x.itemId), quantity: Number(x.quantity) }));
    if (l.length === 0) {
      setError('Add at least one line.');
      return;
    }
    try {
      const mr = await createRequisition({ workstationId: Number(workstationId), notes: notes || undefined, lines: l });
      setMessage(`Requisition ${mr.mrNumber} created.`);
      setNotes('');
      setLines([{ itemId: '', quantity: '' }]);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not create requisition.'));
    }
  }

  async function fulfil(mr: Requisition) {
    setError(null);
    setMessage(null);
    // Fulfil each line's outstanding quantity.
    const fulfilments = mr.items
      .map((it) => ({ reqItemId: it.id, quantity: it.requestedQty - it.fulfilledQty }))
      .filter((f) => f.quantity > 0);
    if (fulfilments.length === 0) {
      setMessage('Nothing outstanding.');
      return;
    }
    try {
      await fulfilRequisition(mr.id, fulfilments);
      setMessage(`Requisition ${mr.mrNumber} fulfilled from stock.`);
      refresh();
    } catch (err) {
      setError(extractError(err, 'Could not fulfil requisition.'));
    }
  }

  return (
    <div>
      <form onSubmit={submit} style={{ border: '1px solid #ddd', borderRadius: 6, padding: 12, marginBottom: 16 }}>
        <h3 style={{ margin: '0 0 8px' }}>New material requisition</h3>
        <div style={{ display: 'flex', gap: 8, marginBottom: 8, flexWrap: 'wrap' }}>
          <label style={{ fontSize: 13 }}>
            From workstation
            <br />
            <select value={workstationId} onChange={(e) => setWorkstationId(e.target.value)} required style={input}>
              <option value="">Select…</option>
              {workstations.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name}
                </option>
              ))}
            </select>
          </label>
          <label style={{ fontSize: 13, flex: 1 }}>
            Notes
            <br />
            <input value={notes} onChange={(e) => setNotes(e.target.value)} style={{ ...input, width: '100%' }} />
          </label>
        </div>
        {lines.map((ln, i) => (
          <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
            <select
              value={ln.itemId}
              onChange={(e) => setLines((prev) => prev.map((x, j) => (j === i ? { ...x, itemId: e.target.value } : x)))}
              style={{ ...input, flex: 1 }}
            >
              <option value="">Select item…</option>
              {items.map((it) => (
                <option key={it.id} value={it.id}>
                  {it.itemCode} — {it.name}
                </option>
              ))}
            </select>
            <input
              type="number"
              min="0.001"
              step="0.001"
              placeholder="qty"
              value={ln.quantity}
              onChange={(e) => setLines((prev) => prev.map((x, j) => (j === i ? { ...x, quantity: e.target.value } : x)))}
              style={{ ...input, width: 90 }}
            />
            <button type="button" onClick={() => setLines((prev) => prev.filter((_, j) => j !== i))}>
              ×
            </button>
          </div>
        ))}
        <button type="button" onClick={() => setLines((prev) => [...prev, { itemId: '', quantity: '' }])}>
          + line
        </button>
        <div style={{ marginTop: 8 }}>
          <button type="submit">Create requisition</button>
        </div>
      </form>

      {error && <p style={{ color: '#c0392b' }}>{error}</p>}
      {message && <p style={{ color: '#1e7e34' }}>{message}</p>}

      {requisitions.map((mr) => (
        <div key={mr.id} style={{ border: '1px solid #eee', borderRadius: 6, padding: 12, marginBottom: 12 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <strong>
              {mr.mrNumber} — {mr.fromWorkstationName} <span style={{ fontWeight: 400 }}>[{mr.status}]</span>
            </strong>
            {mr.status !== 'FULFILLED' && (
              <button onClick={() => fulfil(mr)} style={{ color: '#1e7e34' }}>
                Fulfil outstanding
              </button>
            )}
          </div>
          <table style={{ borderCollapse: 'collapse', width: '100%', marginTop: 8 }}>
            <thead>
              <tr>
                <th style={th}>Item</th>
                <th style={{ ...th, textAlign: 'right' }}>Requested</th>
                <th style={{ ...th, textAlign: 'right' }}>Fulfilled</th>
              </tr>
            </thead>
            <tbody>
              {mr.items.map((it) => (
                <tr key={it.id}>
                  <td style={td}>
                    {it.itemCode} — {it.itemName}
                  </td>
                  <td style={tdNum}>{num(it.requestedQty)}</td>
                  <td style={tdNum}>{num(it.fulfilledQty)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}
      {requisitions.length === 0 && <p>No requisitions yet.</p>}
    </div>
  );
}
