import { apiClient } from './client';

export type SampleStatus =
  | 'DRAFT'
  | 'IN_REVIEW'
  | 'SELECTED'
  | 'PPS_DONE'
  | 'PPM_DONE'
  | 'REJECTED'
  | 'CLOSED';

export type LineType = 'RM' | 'SFG' | 'OP';
export type PpmSectionKey = 'SOP' | 'TRIM' | 'MARK' | 'CHECK' | 'PACK';
export type PhotoSectionKey = 'FORM' | 'PPS' | PpmSectionKey;

export interface SampleSummary {
  id: number;
  sampleNo: string;
  revBase: string;
  revNum: number;
  name: string;
  style: string | null;
  category: string | null;
  designer: string | null;
  date: string | null;
  status: SampleStatus;
  closedRemark: string | null;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface SampleLine {
  id: number;
  lineType: LineType;
  itemId: number | null;
  name: string | null;
  description: string | null;
  colour: string | null;
  qty: number | null;
  unit: string | null;
}

export interface PpmRow {
  id: number;
  section: PpmSectionKey;
  text1: string | null;
  text2: string | null;
  remark: string | null;
}

export interface SamplePhoto {
  id: number;
  section: PhotoSectionKey;
  dataUrl: string;
  caption: string | null;
}

export interface Pps {
  fabricDetails: string | null;
  designCount: number | null;
  specialInstructions: string | null;
  colours: string[];
  sizes: string[];
  savedAt: string | null;
  approvedAt: string | null;
  approvedBy: string | null;
}

export interface Ppm {
  sop: PpmRow[];
  trim: PpmRow[];
  mark: PpmRow[];
  check: PpmRow[];
  pack: PpmRow[];
}

export interface SampleDetail {
  id: number;
  sampleNo: string;
  revBase: string;
  revNum: number;
  date: string | null;
  name: string;
  style: string | null;
  category: string | null;
  designer: string | null;
  reference: string | null;
  notes: string | null;
  status: SampleStatus;
  closedRemark: string | null;
  closedAt: string | null;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
  rawMaterials: SampleLine[];
  sfgItems: SampleLine[];
  operations: SampleLine[];
  pps: Pps;
  ppm: Ppm;
  photos: SamplePhoto[];
}

export interface LineForm {
  itemId?: number;
  name?: string;
  description?: string;
  colour?: string;
  qty?: number;
  unit?: string;
}

export interface SampleForm {
  date?: string;
  name: string;
  style?: string;
  category?: string;
  designer?: string;
  reference?: string;
  notes?: string;
  rawMaterials?: LineForm[];
  sfgItems?: LineForm[];
  operations?: LineForm[];
}

export interface PpsForm {
  fabricDetails?: string;
  designCount?: number;
  specialInstructions?: string;
  colours: string[];
  sizes: string[];
}

export type RequestType = 'NEW' | 'CHANGE';
export type RequestStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface SampleRequest {
  id: number;
  reqType: RequestType;
  title: string;
  description: string | null;
  priority: string;
  refSampleId: number | null;
  refSampleNo: string | null;
  status: RequestStatus;
  completedSampleId: number | null;
  completedSampleNo: string | null;
  requestedBy: string | null;
  createdAt: string;
  adminResolvedAt: string | null;
  adminResolvedBy: string | null;
  adminResolvedRemark: string | null;
}

export interface RequestForm {
  reqType: RequestType;
  title: string;
  description?: string;
  priority?: string;
  refSampleId?: number;
}

// ---- Samples ----
export const listSamples = (status?: SampleStatus, includeClosed = false) =>
  apiClient
    .get<SampleSummary[]>('/sampling/samples', { params: { status, includeClosed } })
    .then((r) => r.data);
export const getSample = (id: number) =>
  apiClient.get<SampleDetail>(`/sampling/samples/${id}`).then((r) => r.data);
export const createSample = (form: SampleForm, submit = false) =>
  apiClient.post<SampleDetail>('/sampling/samples', form, { params: { submit } }).then((r) => r.data);
export const updateSample = (id: number, form: SampleForm, submit = false) =>
  apiClient.put<SampleDetail>(`/sampling/samples/${id}`, form, { params: { submit } }).then((r) => r.data);
export const deleteSample = (id: number) => apiClient.delete(`/sampling/samples/${id}`).then((r) => r.data);
export const submitSample = (id: number) =>
  apiClient.post<SampleDetail>(`/sampling/samples/${id}/submit`).then((r) => r.data);
export const selectSample = (id: number) =>
  apiClient.post<SampleDetail>(`/sampling/samples/${id}/select`).then((r) => r.data);
export const rejectSample = (id: number) =>
  apiClient.post<SampleDetail>(`/sampling/samples/${id}/reject`).then((r) => r.data);
export const closeSample = (id: number, remark?: string) =>
  apiClient.post<SampleDetail>(`/sampling/samples/${id}/close`, { remark }).then((r) => r.data);
export const reviseSample = (id: number) =>
  apiClient.post<SampleDetail>(`/sampling/samples/${id}/revise`).then((r) => r.data);
export const savePps = (id: number, form: PpsForm, approve = false) =>
  apiClient.post<SampleDetail>(`/sampling/samples/${id}/pps`, form, { params: { approve } }).then((r) => r.data);
export const savePpm = (id: number, section: PpmSectionKey, rows: { text1?: string; text2?: string; remark?: string }[]) =>
  apiClient.post<SampleDetail>(`/sampling/samples/${id}/ppm`, { section, rows }).then((r) => r.data);
export const completePpm = (id: number) =>
  apiClient.post<SampleDetail>(`/sampling/samples/${id}/ppm/complete`).then((r) => r.data);
export const addPhoto = (id: number, section: PhotoSectionKey, dataUrl: string, caption?: string) =>
  apiClient.post<SamplePhoto>(`/sampling/samples/${id}/photos`, { section, dataUrl, caption }).then((r) => r.data);
export const deletePhoto = (id: number, photoId: number) =>
  apiClient.delete(`/sampling/samples/${id}/photos/${photoId}`).then((r) => r.data);

// ---- Requests ----
export const listRequests = () => apiClient.get<SampleRequest[]>('/sampling/requests').then((r) => r.data);
export const createRequest = (form: RequestForm) =>
  apiClient.post<SampleRequest>('/sampling/requests', form).then((r) => r.data);
export const updateRequest = (id: number, form: RequestForm) =>
  apiClient.put<SampleRequest>(`/sampling/requests/${id}`, form).then((r) => r.data);
export const startRequest = (id: number) =>
  apiClient.post<SampleRequest>(`/sampling/requests/${id}/start`).then((r) => r.data);
export const cancelRequest = (id: number) =>
  apiClient.post<SampleRequest>(`/sampling/requests/${id}/cancel`).then((r) => r.data);
export const completeRequest = (id: number, completedSampleId: number) =>
  apiClient.post<SampleRequest>(`/sampling/requests/${id}/complete`, { completedSampleId }).then((r) => r.data);

export const STATUS_LABEL: Record<SampleStatus, string> = {
  DRAFT: 'Draft',
  IN_REVIEW: 'In Review',
  SELECTED: 'Selected',
  PPS_DONE: 'PPS Approved',
  PPM_DONE: 'PPM Complete',
  REJECTED: 'Rejected',
  CLOSED: 'Closed',
};

export const STATUS_COLOUR: Record<SampleStatus, string> = {
  DRAFT: '#9ca3af',
  IN_REVIEW: '#f59e0b',
  SELECTED: '#3b82f6',
  PPS_DONE: '#8b5cf6',
  PPM_DONE: '#10b981',
  REJECTED: '#ef4444',
  CLOSED: '#6b7280',
};
