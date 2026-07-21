import { apiClient } from './client';
import type { AppUser, CreateUserRequest, UpdateUserRequest } from './types';

// The grantable rights from the legacy RIGHTS registry. Admins implicitly hold
// all of them, so these only ever apply to ENTRY_USER / STORE_ADMIN accounts.
export const RIGHTS: { key: string; label: string }[] = [
  { key: 'create_job', label: 'Create jobs' },
  { key: 'manage_employees', label: 'Manage employees' },
  { key: 'create_item', label: 'Create store items' },
  { key: 'approve_item', label: 'Approve store items' },
  { key: 'store_entry', label: 'Store entry' },
  { key: 'sampling_access', label: 'Sampling access' },
];

export async function listUsers(): Promise<AppUser[]> {
  const { data } = await apiClient.get<AppUser[]>('/users');
  return data;
}

export async function createUser(request: CreateUserRequest): Promise<AppUser> {
  const { data } = await apiClient.post<AppUser>('/users', request);
  return data;
}

export async function updateUser(id: number, request: UpdateUserRequest): Promise<AppUser> {
  const { data } = await apiClient.put<AppUser>(`/users/${id}`, request);
  return data;
}
