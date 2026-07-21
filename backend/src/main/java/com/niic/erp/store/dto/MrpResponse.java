package com.niic.erp.store.dto;

import java.math.BigDecimal;
import java.util.List;

public record MrpResponse(
        Long jobId,
        String jobDisplayId,
        List<Line> lines,
        List<String> warnings) {

    public record Line(
            Long itemId,
            String itemCode,
            String itemName,
            String itemType,
            String unit,
            BigDecimal required,
            BigDecimal available,
            BigDecimal shortfall,
            String status) {
    }
}
