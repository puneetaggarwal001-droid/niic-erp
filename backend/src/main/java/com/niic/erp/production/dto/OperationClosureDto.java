package com.niic.erp.production.dto;

import com.niic.erp.production.ClosureReason;
import com.niic.erp.production.OperationClosure;
import java.time.LocalDate;

public record OperationClosureDto(Long id, Long jobId, Long colourId, Long sizeId, Long workstationId,
                                   String workstationName, Long operationId, String operationName,
                                   int doneQtyAtClosure, int plannedQty, ClosureReason reason, Integer reworkQty,
                                   Integer rejectionQty, String notes, Long closedByEmployeeId,
                                   String closedByEmployeeName, LocalDate date) {
    public static OperationClosureDto from(OperationClosure closure) {
        return new OperationClosureDto(closure.getId(), closure.getJob().getId(),
                closure.getColour() != null ? closure.getColour().getId() : null,
                closure.getSize() != null ? closure.getSize().getId() : null, closure.getWorkstation().getId(),
                closure.getWorkstation().getName(), closure.getOperation().getId(), closure.getOperation().getName(),
                closure.getDoneQtyAtClosure(), closure.getPlannedQty(), closure.getReason(), closure.getReworkQty(),
                closure.getRejectionQty(), closure.getNotes(), closure.getClosedBy().getId(),
                closure.getClosedBy().getName(), closure.getDate());
    }
}
