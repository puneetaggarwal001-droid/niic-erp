import { apiClient } from './client';

export interface GatePass {
  id: number;
  employeeId: number;
  empId: string;
  employeeName: string;
  designationName: string | null;
  date: string;
  purpose: string;
  penalty: boolean;
  issuedByUsername: string | null;
}

export async function listGatePasses(date: string): Promise<GatePass[]> {
  const { data } = await apiClient.get<GatePass[]>('/gate-passes', { params: { date } });
  return data;
}

export async function getGatePassConfig(): Promise<{ monthlyLimit: number }> {
  const { data } = await apiClient.get<{ monthlyLimit: number }>('/gate-passes/config');
  return data;
}

export async function issueGatePass(req: {
  employeeId: number;
  date: string;
  purpose: string;
}): Promise<GatePass> {
  const { data } = await apiClient.post<GatePass>('/gate-passes', req);
  return data;
}
