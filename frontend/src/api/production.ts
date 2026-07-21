import { apiClient } from './client';
import type {
  ChallanStatus,
  Job,
  JobCreateRequest,
  JobRequest,
  Operation,
  OperationClosure,
  OperationClosureRequest,
  PcRate,
  PcRateRequest,
  ProductionEditRequest,
  ProductionEditRequestSubmitRequest,
  ProductionEntry,
  ProductionEntryRequest,
  QcEntry,
  QcEntryCreateRequest,
  QcEntryFillRequest,
  QcRework,
  Routing,
  RoutingChangeRequest,
  RoutingSaveRequest,
  RoutingSaveResult,
  Style,
  TransferChallan,
  TransferChallanCreateRequest,
  Workstation,
} from './productionTypes';

// Styles -------------------------------------------------------------------
export async function listStyles(): Promise<Style[]> {
  const { data } = await apiClient.get<Style[]>('/production/styles');
  return data;
}

// Workstations --------------------------------------------------------------
export async function listWorkstations(): Promise<Workstation[]> {
  const { data } = await apiClient.get<Workstation[]>('/production/workstations');
  return data;
}

export async function createWorkstation(name: string, code?: string): Promise<Workstation> {
  const { data } = await apiClient.post<Workstation>('/production/workstations', { name, code, active: true });
  return data;
}

export async function updateWorkstation(id: number, request: { name: string; code?: string; active: boolean }): Promise<Workstation> {
  const { data } = await apiClient.put<Workstation>(`/production/workstations/${id}`, request);
  return data;
}

// Operations ------------------------------------------------------------
export async function listOperations(workstationId?: number): Promise<Operation[]> {
  const { data } = await apiClient.get<Operation[]>('/production/operations', { params: { workstationId } });
  return data;
}

export async function createOperation(workstationId: number, name: string): Promise<Operation> {
  const { data } = await apiClient.post<Operation>('/production/operations', { workstationId, name, active: true });
  return data;
}

// PC rates -------------------------------------------------------------
export async function listPcRates(workstationId: number, styleCode: string, modelNo: string): Promise<PcRate[]> {
  const { data } = await apiClient.get<PcRate[]>('/production/pc-rates', { params: { workstationId, styleCode, modelNo } });
  return data;
}

export async function createPcRate(request: PcRateRequest): Promise<PcRate> {
  const { data } = await apiClient.post<PcRate>('/production/pc-rates', request);
  return data;
}

// Jobs -------------------------------------------------------------------
export async function listJobs(activeOnly = true): Promise<Job[]> {
  const { data } = await apiClient.get<Job[]>('/production/jobs', { params: { activeOnly } });
  return data;
}

export async function getJob(id: number): Promise<Job> {
  const { data } = await apiClient.get<Job>(`/production/jobs/${id}`);
  return data;
}

export async function createJob(request: JobCreateRequest): Promise<Job> {
  const { data } = await apiClient.post<Job>('/production/jobs', request);
  return data;
}

export async function setJobActive(id: number, active: boolean): Promise<Job> {
  const { data } = await apiClient.put<Job>(`/production/jobs/${id}/active`, null, { params: { active } });
  return data;
}

// Job requests -----------------------------------------------------------
export async function submitJobRequest(request: JobCreateRequest): Promise<JobRequest> {
  const { data } = await apiClient.post<JobRequest>('/production/job-requests', request);
  return data;
}

export async function listJobRequests(mine = false): Promise<JobRequest[]> {
  const { data } = await apiClient.get<JobRequest[]>('/production/job-requests', { params: { mine } });
  return data;
}

export async function approveJobRequest(id: number, adminRemark?: string): Promise<JobRequest> {
  const { data } = await apiClient.post<JobRequest>(`/production/job-requests/${id}/approve`, { adminRemark });
  return data;
}

export async function rejectJobRequest(id: number, adminRemark?: string): Promise<JobRequest> {
  const { data } = await apiClient.post<JobRequest>(`/production/job-requests/${id}/reject`, { adminRemark });
  return data;
}

// Routing ------------------------------------------------------------------
export async function getRoutingForJob(jobId: number): Promise<Routing> {
  const { data } = await apiClient.get<Routing>(`/production/routing/${jobId}`);
  return data;
}

export async function findRoutingTemplate(style: string, modelNo: string): Promise<Routing | null> {
  const { data } = await apiClient.get<Routing | ''>('/production/routing/template', { params: { style, modelNo } });
  return data || null;
}

export async function saveRouting(request: RoutingSaveRequest, reason?: string): Promise<RoutingSaveResult> {
  const { data } = await apiClient.post<RoutingSaveResult>('/production/routing', request, { params: { reason } });
  return data;
}

// Routing change requests ----------------------------------------------
export async function listRoutingChangeRequests(): Promise<RoutingChangeRequest[]> {
  const { data } = await apiClient.get<RoutingChangeRequest[]>('/production/routing-change-requests');
  return data;
}

export async function approveRoutingChangeRequest(id: number, adminRemark?: string): Promise<RoutingChangeRequest> {
  const { data } = await apiClient.post<RoutingChangeRequest>(`/production/routing-change-requests/${id}/approve`, { adminRemark });
  return data;
}

export async function rejectRoutingChangeRequest(id: number, adminRemark?: string): Promise<RoutingChangeRequest> {
  const { data } = await apiClient.post<RoutingChangeRequest>(`/production/routing-change-requests/${id}/reject`, { adminRemark });
  return data;
}

// Production entries -----------------------------------------------------
export async function listProductionEntriesForDate(date: string): Promise<ProductionEntry[]> {
  const { data } = await apiClient.get<ProductionEntry[]>('/production/entries', { params: { date } });
  return data;
}

export async function listProductionEntriesForJob(jobId: number): Promise<ProductionEntry[]> {
  const { data } = await apiClient.get<ProductionEntry[]>('/production/entries', { params: { jobId } });
  return data;
}

export async function saveProductionEntry(request: ProductionEntryRequest): Promise<ProductionEntry> {
  const { data } = await apiClient.post<ProductionEntry>('/production/entries', request);
  return data;
}

// Production edit requests -----------------------------------------------
export async function submitProductionEditRequest(request: ProductionEditRequestSubmitRequest): Promise<ProductionEditRequest> {
  const { data } = await apiClient.post<ProductionEditRequest>('/production/edit-requests', request);
  return data;
}

export async function listProductionEditRequests(mine = false): Promise<ProductionEditRequest[]> {
  const { data } = await apiClient.get<ProductionEditRequest[]>('/production/edit-requests', { params: { mine } });
  return data;
}

export async function approveProductionEditRequest(id: number): Promise<ProductionEditRequest> {
  const { data } = await apiClient.post<ProductionEditRequest>(`/production/edit-requests/${id}/approve`);
  return data;
}

export async function rejectProductionEditRequest(id: number): Promise<ProductionEditRequest> {
  const { data } = await apiClient.post<ProductionEditRequest>(`/production/edit-requests/${id}/reject`);
  return data;
}

export async function consolidateProductionEditRequest(id: number, replacement: ProductionEntryRequest): Promise<ProductionEntry> {
  const { data } = await apiClient.post<ProductionEntry>(`/production/edit-requests/${id}/consolidate`, replacement);
  return data;
}

// Operation closures -----------------------------------------------------
export async function listOperationClosures(jobId: number): Promise<OperationClosure[]> {
  const { data } = await apiClient.get<OperationClosure[]>('/production/operation-closures', { params: { jobId } });
  return data;
}

export async function closeOperation(request: OperationClosureRequest): Promise<OperationClosure> {
  const { data } = await apiClient.post<OperationClosure>('/production/operation-closures', request);
  return data;
}

export async function reopenOperationClosure(id: number): Promise<void> {
  await apiClient.delete(`/production/operation-closures/${id}`);
}

// QC entries ---------------------------------------------------------------
export async function listQcEntries(jobId?: number, pendingOnly = false): Promise<QcEntry[]> {
  const { data } = await apiClient.get<QcEntry[]>('/production/qc-entries', { params: { jobId, pendingOnly } });
  return data;
}

export async function createQcEntry(request: QcEntryCreateRequest): Promise<QcEntry> {
  const { data } = await apiClient.post<QcEntry>('/production/qc-entries', request);
  return data;
}

export async function fillQcEntryDetails(id: number, request: QcEntryFillRequest): Promise<QcEntry> {
  const { data } = await apiClient.put<QcEntry>(`/production/qc-entries/${id}/fill-details`, request);
  return data;
}

// QC rework ------------------------------------------------------------
export async function listQcRework(jobId?: number, pendingOnly = false): Promise<QcRework[]> {
  const { data } = await apiClient.get<QcRework[]>('/production/qc-rework', { params: { jobId, pendingOnly } });
  return data;
}

export async function recordQcReworkResult(id: number, done: number, reject: number): Promise<QcRework> {
  const { data } = await apiClient.post<QcRework>(`/production/qc-rework/${id}/result`, { done, reject });
  return data;
}

// Transfer challans ------------------------------------------------------
export async function listTransferChallans(status: ChallanStatus = 'PENDING', pendingForWorkstationId?: number): Promise<TransferChallan[]> {
  const { data } = await apiClient.get<TransferChallan[]>('/production/transfer-challans', {
    params: { status, pendingForWorkstationId },
  });
  return data;
}

export async function createTransferChallan(request: TransferChallanCreateRequest): Promise<TransferChallan> {
  const { data } = await apiClient.post<TransferChallan>('/production/transfer-challans', request);
  return data;
}

export async function receiveTransferChallan(id: number): Promise<TransferChallan> {
  const { data } = await apiClient.post<TransferChallan>(`/production/transfer-challans/${id}/receive`);
  return data;
}

export async function rejectTransferChallan(id: number, reason: string): Promise<TransferChallan> {
  const { data } = await apiClient.post<TransferChallan>(`/production/transfer-challans/${id}/reject`, { reason });
  return data;
}
