import { apiClient } from './client';

export interface Advance {
  id: number;
  employeeId: number;
  empId: string;
  employeeName: string;
  periodMonth: string;
  amount: number;
  reason: string | null;
  deducted: boolean;
}

export interface PayrollRunLine {
  employeeId: number;
  empId: string;
  name: string;
  salaryType: string;
  presentDays: number;
  overtimeMinutes: number;
  totalPieces: number;
  monthlySalary: number | null;
  pcRate: number | null;
  grossPay: number;
  advancesDeducted: number;
  netPay: number;
}

export interface PayrollRun {
  id: number;
  periodMonth: string;
  status: 'DRAFT' | 'FINALIZED';
  finalizedAt: string | null;
  totalGross: number;
  totalAdvances: number;
  totalNet: number;
  lines: PayrollRunLine[];
}

export interface Contractor {
  id: number;
  name: string;
  phone: string | null;
  active: boolean;
}

export type ContractorRateType = 'PER_OPERATION' | 'FINISHED_PIECES';

export interface ContractorBill {
  id: number;
  contractorId: number;
  contractorName: string;
  periodMonth: string;
  rateType: ContractorRateType;
  quantity: number;
  rate: number;
  amount: number;
  advancesDeducted: number;
  netPayable: number;
  notes: string | null;
  status: 'DRAFT' | 'FINALIZED';
  finalizedAt: string | null;
}

// ---- Advances ----
export async function listAdvances(month: string): Promise<Advance[]> {
  const { data } = await apiClient.get<Advance[]>('/payroll/advances', { params: { month } });
  return data;
}

export async function addAdvance(req: {
  employeeId: number;
  periodMonth: string;
  amount: number;
  reason?: string;
}): Promise<Advance> {
  const { data } = await apiClient.post<Advance>('/payroll/advances', req);
  return data;
}

// ---- Runs ----
export async function listRuns(): Promise<PayrollRun[]> {
  const { data } = await apiClient.get<PayrollRun[]>('/payroll/runs');
  return data;
}

export async function generateRun(month: string): Promise<PayrollRun> {
  const { data } = await apiClient.post<PayrollRun>('/payroll/runs', null, { params: { month } });
  return data;
}

export async function finalizeRun(id: number): Promise<PayrollRun> {
  const { data } = await apiClient.post<PayrollRun>(`/payroll/runs/${id}/finalize`);
  return data;
}

// ---- Contractors ----
export async function listContractors(): Promise<Contractor[]> {
  const { data } = await apiClient.get<Contractor[]>('/payroll/contractors');
  return data;
}

export async function addContractor(name: string, phone?: string): Promise<Contractor> {
  const { data } = await apiClient.post<Contractor>('/payroll/contractors', { name, phone });
  return data;
}

export async function deactivateContractor(id: number): Promise<void> {
  await apiClient.delete(`/payroll/contractors/${id}`);
}

// ---- Contractor bills ----
export async function listBills(month: string): Promise<ContractorBill[]> {
  const { data } = await apiClient.get<ContractorBill[]>('/payroll/bills', { params: { month } });
  return data;
}

export async function createBill(req: {
  contractorId: number;
  periodMonth: string;
  rateType: ContractorRateType;
  quantity: number;
  rate: number;
  advances?: number;
  notes?: string;
}): Promise<ContractorBill> {
  const { data } = await apiClient.post<ContractorBill>('/payroll/bills', req);
  return data;
}

export async function finalizeBill(id: number): Promise<ContractorBill> {
  const { data } = await apiClient.post<ContractorBill>(`/payroll/bills/${id}/finalize`);
  return data;
}
