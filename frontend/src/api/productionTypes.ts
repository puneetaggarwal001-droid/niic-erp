export type Unit = 'PCS' | 'PAIR';
export type Side = 'LEFT' | 'RIGHT' | 'PAIR';
export type JobSource = 'ADMIN_DIRECT' | 'APPROVED_REQUEST';
export type RequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type ClosureReason = 'RAW_MATERIAL' | 'REWORK' | 'REJECTION' | 'COMPONENT_LOSS' | 'OTHER';
export type QcStatus = 'PENDING_DETAILS' | 'COMPLETED';
export type ReworkStatus = 'PENDING' | 'COMPLETED';
export type ChallanStatus = 'PENDING' | 'RECEIVED' | 'REJECTED' | 'CANCELLED';

export interface Style {
  id: number;
  code: string;
  label: string;
  active: boolean;
}

export interface Workstation {
  id: number;
  name: string;
  code: string | null;
  active: boolean;
}

export interface Operation {
  id: number;
  workstationId: number;
  workstationName: string;
  name: string;
  active: boolean;
}

export interface PcRate {
  id: number;
  workstationId: number;
  workstationName: string;
  styleCode: string;
  modelNo: string;
  operationId: number;
  operationName: string;
  rate: number;
  effectiveDate: string;
  active: boolean;
}

export interface PcRateRequest {
  workstationId: number;
  styleCode: string;
  modelNo: string;
  operationId: number;
  rate: number;
  effectiveDate: string;
}

export interface JobSize {
  id: number;
  size: string;
  plannedQty: number;
  variantId: number | null;
}

export interface JobColour {
  id: number;
  name: string;
  sizes: JobSize[];
}

export interface Job {
  id: number;
  jobDisplayId: string;
  styleCode: string;
  styleLabel: string;
  modelNo: string;
  fgItemId: number | null;
  unit: Unit;
  totalPlannedQty: number;
  active: boolean;
  source: JobSource;
  routingAssigned: boolean;
  colours: JobColour[];
}

export interface JobSizeRequest {
  size: string;
  plannedQty: number;
  variantId?: number;
}

export interface JobColourRequest {
  name: string;
  sizes: JobSizeRequest[];
}

export interface JobCreateRequest {
  styleCode: string;
  modelNo: string;
  fgItemId?: number;
  unit: Unit;
  colours: JobColourRequest[];
}

export interface JobRequest {
  id: number;
  styleCode: string;
  styleLabel: string;
  modelNo: string;
  fgItemId: number | null;
  unit: Unit;
  colours: JobColourRequest[];
  status: RequestStatus;
  requestedByUsername: string;
  reviewedByUsername: string | null;
  reviewedAt: string | null;
  adminRemark: string | null;
}

export interface RoutingOperationInfo {
  id: number;
  operationId: number;
  operationName: string;
  dependsOnOperationIds: number[];
}

export interface RoutingWorkstationInfo {
  id: number;
  workstationId: number;
  workstationName: string;
  operations: RoutingOperationInfo[];
}

export interface Routing {
  id: number;
  jobId: number;
  jobDisplayId: string;
  workstations: RoutingWorkstationInfo[];
}

export interface RoutingOperationRequest {
  operationId: number;
  dependsOnOperationIds: number[];
}

export interface RoutingWorkstationRequest {
  workstationId: number;
  operations: RoutingOperationRequest[];
}

export interface RoutingSaveRequest {
  jobId: number;
  workstations: RoutingWorkstationRequest[];
}

export interface RoutingChangeRequest {
  id: number;
  jobId: number;
  jobDisplayId: string;
  proposedWorkstations: RoutingWorkstationRequest[];
  reason: string;
  status: RequestStatus;
  requestedByUsername: string;
  reviewedByUsername: string | null;
  reviewedAt: string | null;
  adminRemark: string | null;
}

export interface RoutingSaveResult {
  routing: Routing | null;
  pendingRequest: RoutingChangeRequest | null;
}

export interface ProductionEntryOp {
  id: number;
  workstationId: number;
  workstationName: string;
  operationId: number;
  operationName: string;
  quantity: number;
  unit: Unit;
}

export interface ProductionEntry {
  id: number;
  date: string;
  employeeId: number;
  employeeName: string;
  jobId: number;
  jobDisplayId: string;
  colourId: number | null;
  sizeId: number | null;
  side: Side | null;
  unit: Unit;
  operations: ProductionEntryOp[];
  approvedEdit: boolean;
}

export interface ProductionEntryOpRequest {
  workstationId: number;
  operationId: number;
  quantity: number;
}

export interface ProductionEntryRequest {
  date: string;
  employeeId: number;
  jobId: number;
  colourId?: number;
  sizeId?: number;
  side?: Side;
  operations: ProductionEntryOpRequest[];
}

export interface ProductionEditRequest {
  id: number;
  jobId: number;
  jobDisplayId: string;
  colourId: number | null;
  sizeId: number | null;
  requestedByUsername: string;
  reason: string;
  status: RequestStatus;
  used: boolean;
  resolvedByUsername: string | null;
  resolution: string | null;
}

export interface ProductionEditRequestSubmitRequest {
  jobId: number;
  colourId?: number;
  sizeId?: number;
  reason: string;
}

export interface OperationClosure {
  id: number;
  jobId: number;
  colourId: number | null;
  sizeId: number | null;
  workstationId: number;
  workstationName: string;
  operationId: number;
  operationName: string;
  doneQtyAtClosure: number;
  plannedQty: number;
  reason: ClosureReason;
  reworkQty: number | null;
  rejectionQty: number | null;
  notes: string | null;
  closedByEmployeeId: number;
  closedByEmployeeName: string;
  date: string;
}

export interface OperationClosureRequest {
  jobId: number;
  colourId?: number;
  sizeId?: number;
  workstationId: number;
  operationId: number;
  doneQtyAtClosure: number;
  plannedQty: number;
  reason: ClosureReason;
  reworkQty?: number;
  rejectionQty?: number;
  notes?: string;
  closedByEmployeeId: number;
  date: string;
}

export interface QcEntry {
  id: number;
  date: string;
  jobId: number;
  jobDisplayId: string;
  colourId: number | null;
  sizeId: number | null;
  side: Side | null;
  opRef: string | null;
  workstationId: number | null;
  workstationName: string | null;
  totalChecked: number;
  passQty: number;
  alterQty: number;
  rejectQty: number;
  status: QcStatus;
  fromQcWorkstation: boolean;
}

export interface QcEntryCreateRequest {
  date: string;
  jobId: number;
  colourId?: number;
  sizeId?: number;
  side?: Side;
  opRef?: string;
  workstationId?: number;
  totalChecked: number;
  passQty: number;
  alterQty: number;
  rejectQty: number;
  skipDetails: boolean;
}

export interface QcEntryFillRequest {
  passQty: number;
  alterQty: number;
  rejectQty: number;
}

export interface QcRework {
  id: number;
  qcEntryId: number;
  jobId: number;
  jobDisplayId: string;
  alterQty: number;
  reworkDone: number;
  reworkReject: number;
  status: ReworkStatus;
}

export interface TransferChallanItem {
  id: number;
  itemId: number | null;
  itemCode: string | null;
  itemName: string;
  itemUnit: string;
  qty: number;
}

export interface TransferChallan {
  id: number;
  challanNo: string;
  jobId: number;
  jobDisplayId: string;
  fromWorkstationId: number;
  fromWorkstationName: string;
  toWorkstationId: number;
  toWorkstationName: string;
  remarks: string | null;
  status: ChallanStatus;
  createdByUsername: string;
  items: TransferChallanItem[];
}

export interface TransferChallanItemRequest {
  itemId?: number;
  itemCode?: string;
  itemName: string;
  itemUnit: string;
  qty: number;
}

export interface TransferChallanCreateRequest {
  jobId: number;
  fromWorkstationId: number;
  toWorkstationId: number;
  remarks?: string;
  items: TransferChallanItemRequest[];
}
