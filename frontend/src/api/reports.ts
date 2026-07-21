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
