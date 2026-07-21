package com.niic.erp.production.dto;

import com.niic.erp.production.ClosureReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record OperationClosureRequest(@NotNull Long jobId, Long colourId, Long sizeId,
                                       @NotNull Long workstationId, @NotNull Long operationId,
                                       @Min(0) int doneQtyAtClosure, @Min(0) int plannedQty,
                                       @NotNull ClosureReason reason, Integer reworkQty, Integer rejectionQty,
                                       String notes, @NotNull Long closedByEmployeeId, @NotNull LocalDate date) {
}
