package com.niic.erp.store.dto;

import com.niic.erp.store.MaterialRequisition;
import java.math.BigDecimal;
import java.util.List;

public record RequisitionDto(
        Long id,
        String mrNumber,
        Long fromWorkstationId,
        String fromWorkstationName,
        String status,
        String notes,
        List<Line> items) {

    public record Line(
            Long id,
            Long itemId,
            String itemCode,
            String itemName,
            BigDecimal requestedQty,
            BigDecimal fulfilledQty) {
    }

    public static RequisitionDto from(MaterialRequisition r) {
        List<Line> lines = r.getItems().stream()
                .map(i -> new Line(i.getId(), i.getItem().getId(), i.getItem().getItemCode(),
                        i.getItem().getName(), i.getRequestedQty(), i.getFulfilledQty()))
                .toList();
        return new RequisitionDto(r.getId(), r.getMrNumber(), r.getFromWorkstation().getId(),
                r.getFromWorkstation().getName(), r.getStatus().name(), r.getNotes(), lines);
    }
}
