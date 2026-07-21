package com.niic.erp.store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record RequisitionRequest(
        @NotNull Long workstationId,
        String notes,
        @NotEmpty List<Line> lines) {

    public record Line(@NotNull Long itemId, @NotNull BigDecimal quantity) {
    }
}
