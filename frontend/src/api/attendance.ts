import { apiClient } from './client';
import type { AttendanceRecord } from './types';

export async function listAttendanceForDate(date: string): Promise<AttendanceRecord[]> {
  const { data } = await apiClient.get<AttendanceRecord[]>('/attendance', { params: { date } });
  return data;
}

export async function upsertAttendance(
  date: string,
  employeeId: number,
  entryTime?: string,
  exitTime?: string,
): Promise<AttendanceRecord> {
  const { data } = await apiClient.post<AttendanceRecord>('/attendance', {
    date,
    employeeId,
    entryTime,
    exitTime,
  });
  return data;
}
