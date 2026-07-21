package com.niic.erp.store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record IssueRequest(
        Long jobId,
        @NotNull Long workstationId,
        String notes,
        @NotEmpty List<Line> lines) {

    public record Line(@NotNull Long itemId, Long variantId, @NotNull BigDecimal quantity) {
    }
}
