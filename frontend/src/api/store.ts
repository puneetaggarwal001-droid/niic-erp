import { apiClient } from './client';

export type ItemType = 'RM' | 'SFG' | 'FG';
export type StockTxnType = 'INWARD' | 'ISSUE' | 'RETURN' | 'REJECT' | 'ADJUST';

export interface StoreCategory {
  id: number;
  name: string;
  code: string;
  parentId: number | null;
  parentName: string | null;
}

export interface StoreVariant {
  id: number;
  colour: string;
  size: string;
}

export interface StoreItem {
  id: number;
  itemCode: string;
  name: string;
  itemType: ItemType;
  unit: string;
  categoryId: number | null;
  categoryName: string | null;
  reorderLevel: number;
  onHand: number;
  belowReorder: boolean;
  approvalStatus: 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED';
  rejectionReason: string | null;
  variants: StoreVariant[];
}

export interface StockTxn {
  id: number;
  itemId: number;
  itemCode: string;
  itemName: string;
  variantId: number | null;
  variantLabel: string | null;
  txnType: StockTxnType;
  quantity: number;
  txnDate: string;
  reference: string | null;
  note: string | null;
  createdByUsername: string | null;
}

export interface BomComponentDto {
  componentItemId: number;
  itemCode: string;
  itemName: string;
  itemType: ItemType;
  quantity: number;
}

export interface Bom {
  id: number;
  outputItemId: number;
  outputItemCode: string;
  outputItemName: string;
  outputItemType: ItemType;
  colour: string | null;
  size: string | null;
  batchQty: number;
  active: boolean;
  components: BomComponentDto[];
}

export interface MrpLine {
  itemId: number;
  itemCode: string;
  itemName: string;
  itemType: ItemType;
  unit: string;
  required: number;
  available: number;
  shortfall: number;
  status: 'OK' | 'PARTIAL' | 'SHORT';
}

export interface MrpResponse {
  jobId: number;
  jobDisplayId: string;
  lines: MrpLine[];
  warnings: string[];
}

export interface PoLine {
  id: number;
  itemId: number;
  itemCode: string;
  itemName: string;
  orderedQty: number;
  receivedQty: number;
  rate: number | null;
}

export interface PurchaseOrder {
  id: number;
  poNumber: string;
  supplierName: string | null;
  status: 'PENDING' | 'ORDERED' | 'RECEIVED' | 'CANCELLED';
  notes: string | null;
  items: PoLine[];
}

export interface Booking {
  id: number;
  itemId: number;
  itemCode: string;
  itemName: string;
  jobId: number;
  jobDisplayId: string;
  bookedQty: number;
  issuedQty: number;
  outstanding: number;
  status: 'OPEN' | 'CLOSED';
}

export interface Issuance {
  id: number;
  issNumber: string;
  jobId: number | null;
  jobDisplayId: string | null;
  toWorkstationId: number;
  toWorkstationName: string;
  notes: string | null;
  items: { itemId: number; itemCode: string; itemName: string; variantLabel: string | null; quantity: number }[];
}

export interface Requisition {
  id: number;
  mrNumber: string;
  fromWorkstationId: number;
  fromWorkstationName: string;
  status: 'PENDING' | 'PARTIAL' | 'FULFILLED';
  notes: string | null;
  items: { id: number; itemId: number; itemCode: string; itemName: string; requestedQty: number; fulfilledQty: number }[];
}

// ---- Categories ----
export const listCategories = () => apiClient.get<StoreCategory[]>('/store/categories').then((r) => r.data);
export const addCategory = (name: string, code: string, parentId?: number) =>
  apiClient.post<StoreCategory>('/store/categories', { name, code, parentId }).then((r) => r.data);

// ---- Items ----
export const listItems = (type?: ItemType) =>
  apiClient.get<StoreItem[]>('/store/items', { params: type ? { type } : {} }).then((r) => r.data);
export const getItem = (id: number) => apiClient.get<StoreItem>(`/store/items/${id}`).then((r) => r.data);
export const createItem = (req: {
  itemCode?: string;
  name: string;
  itemType: ItemType;
  unit: string;
  categoryId?: number;
  reorderLevel?: number;
  variants?: { colour: string; size: string }[];
}) => apiClient.post<StoreItem>('/store/items', req).then((r) => r.data);
export const listPendingItems = () => apiClient.get<StoreItem[]>('/store/items/pending-approval').then((r) => r.data);
export const approveItem = (id: number) => apiClient.post<StoreItem>(`/store/items/${id}/approve`).then((r) => r.data);
export const rejectItem = (id: number, reason: string) =>
  apiClient.post<StoreItem>(`/store/items/${id}/reject`, { reason }).then((r) => r.data);

// ---- Stock ----
export const listLedger = (itemId: number) =>
  apiClient.get<StockTxn[]>(`/store/items/${itemId}/ledger`).then((r) => r.data);
export const recordStock = (req: {
  itemId: number;
  variantId?: number;
  txnType: StockTxnType;
  quantity: number;
  date?: string;
  reference?: string;
  note?: string;
}) => apiClient.post<StockTxn>('/store/stock', req).then((r) => r.data);

// ---- BOM ----
export const listBoms = (outputItemId: number) =>
  apiClient.get<Bom[]>('/store/boms', { params: { outputItemId } }).then((r) => r.data);
export const createBom = (req: {
  outputItemId: number;
  colour?: string;
  size?: string;
  batchQty?: number;
  components: { componentItemId: number; quantity: number }[];
}) => apiClient.post<Bom>('/store/boms', req).then((r) => r.data);
export const deleteBom = (id: number) => apiClient.delete(`/store/boms/${id}`).then((r) => r.data);

// ---- MRP ----
export const runMrp = (jobId: number) =>
  apiClient.get<MrpResponse>('/store/mrp', { params: { jobId } }).then((r) => r.data);

// ---- Purchase orders ----
export const listPurchaseOrders = () => apiClient.get<PurchaseOrder[]>('/store/purchase-orders').then((r) => r.data);
export const createPurchaseOrder = (req: {
  supplierName?: string;
  notes?: string;
  lines: { itemId: number; quantity: number; rate?: number }[];
}) => apiClient.post<PurchaseOrder>('/store/purchase-orders', req).then((r) => r.data);
export const markPoOrdered = (id: number) =>
  apiClient.post<PurchaseOrder>(`/store/purchase-orders/${id}/order`).then((r) => r.data);
export const receivePo = (id: number, receipts: { poItemId: number; quantity: number }[]) =>
  apiClient.post<PurchaseOrder>(`/store/purchase-orders/${id}/receive`, { receipts }).then((r) => r.data);

// ---- Bookings ----
export const listBookings = (jobId: number) =>
  apiClient.get<Booking[]>('/store/bookings', { params: { jobId } }).then((r) => r.data);
export const bookForJob = (jobId: number) =>
  apiClient.post<Booking[]>('/store/bookings', null, { params: { jobId } }).then((r) => r.data);

// ---- Issuance ----
export const listIssuances = () => apiClient.get<Issuance[]>('/store/issuances').then((r) => r.data);
export const createIssuance = (req: {
  jobId?: number;
  workstationId: number;
  notes?: string;
  lines: { itemId: number; variantId?: number; quantity: number }[];
}) => apiClient.post<Issuance>('/store/issuances', req).then((r) => r.data);

// ---- Requisitions ----
export const listRequisitions = () => apiClient.get<Requisition[]>('/store/requisitions').then((r) => r.data);
export const createRequisition = (req: {
  workstationId: number;
  notes?: string;
  lines: { itemId: number; quantity: number }[];
}) => apiClient.post<Requisition>('/store/requisitions', req).then((r) => r.data);
export const fulfilRequisition = (id: number, fulfilments: { reqItemId: number; quantity: number }[]) =>
  apiClient.post<Requisition>(`/store/requisitions/${id}/fulfil`, { fulfilments }).then((r) => r.data);
