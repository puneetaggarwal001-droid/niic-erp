import { apiClient } from './client';

export interface Dashboard {
  hr: {
    totalEmployees: number;
    activeEmployees: number;
    presentToday: number;
    gatePassesToday: number;
  };
  production: {
    activeJobs: number;
    unitsToday: number;
    pendingJobRequests: number;
    pendingEditRequests: number;
    pendingRoutingRequests: number;
    pendingQc: number;
    pendingTransfers: number;
  };
  store: {
    totalItems: number;
    pendingItemApprovals: number;
    belowReorder: number;
    openPurchaseOrders: number;
    pendingRequisitions: number;
  };
  sampling: {
    pipeline: Record<string, number>;
    activeSamples: number;
    pendingRequests: number;
  };
  payroll: {
    lastRunMonth: string | null;
    lastRunStatus: string | null;
  };
}

export const getDashboard = () => apiClient.get<Dashboard>('/dashboard').then((r) => r.data);

export const exportBackup = () =>
  apiClient.get('/backup/export', { responseType: 'blob' }).then((r) => r.data as Blob);

export const restoreBackup = (snapshot: unknown) =>
  apiClient.post<{ restoredTables: number; restoredRows: number }>('/backup/restore', snapshot).then((r) => r.data);
