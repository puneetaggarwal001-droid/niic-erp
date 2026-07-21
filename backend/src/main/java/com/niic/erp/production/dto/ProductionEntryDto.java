package com.niic.erp.production.dto;

import com.niic.erp.production.ProductionEntry;
import com.niic.erp.production.Side;
import com.niic.erp.production.Unit;
import java.time.LocalDate;
import java.util.List;

public record ProductionEntryDto(Long id, LocalDate date, Long employeeId, String employeeName, Long jobId,
                                  String jobDisplayId, Long colourId, Long sizeId, Side side, Unit unit,
                                  List<ProductionEntryOpDto> operations, boolean approvedEdit) {
    public static ProductionEntryDto from(ProductionEntry entry) {
        return new ProductionEntryDto(entry.getId(), entry.getDate(), entry.getEmployee().getId(),
                entry.getEmployee().getName(), entry.getJob().getId(), entry.getJob().getJobDisplayId(),
                entry.getColour() != null ? entry.getColour().getId() : null,
                entry.getSize() != null ? entry.getSize().getId() : null, entry.getSide(), entry.getUnit(),
                entry.getOperations().stream().map(ProductionEntryOpDto::from).toList(), entry.isApprovedEdit());
    }
}
