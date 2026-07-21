package com.niic.erp.store.dto;

import com.niic.erp.store.IssuanceChallan;
import java.math.BigDecimal;
import java.util.List;

public record IssuanceDto(
        Long id,
        String issNumber,
        Long jobId,
        String jobDisplayId,
        Long toWorkstationId,
        String toWorkstationName,
        String notes,
        List<Line> items) {

    public record Line(Long itemId, String itemCode, String itemName, String variantLabel, BigDecimal quantity) {
    }

    public static IssuanceDto from(IssuanceChallan c) {
        List<Line> lines = c.getItems().stream()
                .map(i -> new Line(i.getItem().getId(), i.getItem().getItemCode(), i.getItem().getName(),
                        i.getVariant() != null ? i.getVariant().getColour() + "/" + i.getVariant().getSize() : null,
                        i.getQuantity()))
                .toList();
        return new IssuanceDto(
                c.getId(), c.getIssNumber(),
                c.getJob() != null ? c.getJob().getId() : null,
                c.getJob() != null ? c.getJob().getJobDisplayId() : null,
                c.getToWorkstation().getId(), c.getToWorkstation().getName(), c.getNotes(), lines);
    }
}
