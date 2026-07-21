import { apiClient } from './client';
import type { Designation, Employee, EmployeeRequest } from './types';

export async function listEmployees(): Promise<Employee[]> {
  const { data } = await apiClient.get<Employee[]>('/employees');
  return data;
}

export async function createEmployee(request: EmployeeRequest): Promise<Employee> {
  const { data } = await apiClient.post<Employee>('/employees', request);
  return data;
}

export async function listDesignations(): Promise<Designation[]> {
  const { data } = await apiClient.get<Designation[]>('/designations');
  return data;
}

export async function createDesignation(name: string): Promise<Designation> {
  const { data } = await apiClient.post<Designation>('/designations', { name });
  return data;
}
