export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  rights: string[];
}

export interface Designation {
  id: number;
  name: string;
}

export type SalaryType = 'SALARIED' | 'PC_RATE' | 'CONTRACTOR';
export type EmployeeType = 'REGULAR' | 'VISITOR';

export interface Employee {
  id: number;
  empId: string;
  name: string;
  address: string | null;
  aadhar: string;
  phone: string;
  designationId: number;
  designationName: string;
  dateOfJoining: string;
  salaryType: SalaryType;
  salary: number | null;
  pcRate: number | null;
  contractorName: string | null;
  empType: EmployeeType;
  validTill: string | null;
  department: string | null;
  notes: string | null;
  authorizedWorkstations: string[];
  photoUrl: string | null;
  active: boolean;
}

export interface EmployeeRequest {
  name: string;
  address?: string;
  aadhar: string;
  phone: string;
  designationId: number;
  dateOfJoining: string;
  salaryType: SalaryType;
  salary?: number;
  pcRate?: number;
  contractorName?: string;
  empType?: EmployeeType;
  validTill?: string;
  department?: string;
  notes?: string;
  authorizedWorkstations?: string[];
  photoUrl?: string;
}

export interface AttendanceRecord {
  id: number;
  date: string;
  employeeId: number;
  empId: string;
  employeeName: string;
  designationId: number;
  designationName: string;
  entryTime: string | null;
  exitTime: string | null;
  enteredByUsername: string | null;
  enteredAt: string;
  lastEditedByUsername: string | null;
  lastEditedAt: string | null;
}
