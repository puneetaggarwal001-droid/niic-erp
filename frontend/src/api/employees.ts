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

export async function updateEmployee(id: number, request: EmployeeRequest): Promise<Employee> {
  const { data } = await apiClient.put<Employee>(`/employees/${id}`, request);
  return data;
}

// Soft delete — the backend deactivates the employee (returns 204) rather than
// hard-deleting, so they drop off the active list but history is preserved.
export async function deactivateEmployee(id: number): Promise<void> {
  await apiClient.delete(`/employees/${id}`);
}

export async function listDesignations(): Promise<Designation[]> {
  const { data } = await apiClient.get<Designation[]>('/designations');
  return data;
}

export async function createDesignation(name: string): Promise<Designation> {
  const { data } = await apiClient.post<Designation>('/designations', { name });
  return data;
}
