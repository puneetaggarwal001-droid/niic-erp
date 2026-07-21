package com.niic.erp.production.dto;

import com.niic.erp.production.ProductionEntryOp;
import com.niic.erp.production.Unit;

public record ProductionEntryOpDto(Long id, Long workstationId, String workstationName, Long operationId,
                                    String operationName, int quantity, Unit unit) {
    public static ProductionEntryOpDto from(ProductionEntryOp op) {
        return new ProductionEntryOpDto(op.getId(), op.getWorkstation().getId(), op.getWorkstation().getName(),
                op.getOperation().getId(), op.getOperation().getName(), op.getQuantity(), op.getUnit());
    }
}
