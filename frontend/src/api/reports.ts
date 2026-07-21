import { apiClient } from './client';

export interface EprOpLine {
  workstation: string;
  operation: string;
  pieces: number;
  rate: number;
  income: number;
}

export interface EprDay {
  date: string;
  pieces: number;
  income: number;
}

export interface EprEmployeeReport {
  employeeId: number;
  empId: string;
  name: string;
  totalPieces: number;
  totalIncome: number;
  days: EprDay[];
  operations: EprOpLine[];
}

export interface EprResponse {
  from: string;
  to: string;
  grandTotalIncome: number;
  grandTotalPieces: number;
  employees: EprEmployeeReport[];
}

export async function getEpr(from: string, to: string, employeeId?: number): Promise<EprResponse> {
  const { data } = await apiClient.get<EprResponse>('/reports/epr', {
    params: { from, to, ...(employeeId ? { employeeId } : {}) },
  });
  return data;
}

// ---- Work in progress ----

export interface OpWip {
  workstation: string;
  operation: string;
  produced: number;
  closed: number;
  open: number;
}

export interface JobWip {
  jobId: number;
  jobDisplayId: string;
  modelNo: string;
  plannedQty: number;
  produced: number;
  closed: number;
  open: number;
  operations: OpWip[];
}

export interface WipResponse {
  totalOpen: number;
  jobs: JobWip[];
}

export const getWip = () => apiClient.get<WipResponse>('/reports/wip').then((r) => r.data);

// ---- Rejects & rework ----

export interface OpRejects {
  workstation: string;
  operation: string;
  checked: number;
  passed: number;
  alter: number;
  rejected: number;
}

export interface JobRejects {
  jobId: number;
  jobDisplayId: string;
  modelNo: string;
  checked: number;
  passed: number;
  alter: number;
  rejected: number;
  reworkReject: number;
  rejectPct: number;
  operations: OpRejects[];
}

export interface RejectsResponse {
  from: string;
  to: string;
  totalChecked: number;
  totalPassed: number;
  totalAlter: number;
  totalRejected: number;
  totalReworkReject: number;
  jobs: JobRejects[];
}

export const getRejects = (from: string, to: string) =>
  apiClient.get<RejectsResponse>('/reports/rejects', { params: { from, to } }).then((r) => r.data);
