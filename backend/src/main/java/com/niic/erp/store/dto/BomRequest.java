package com.niic.erp.store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record BomRequest(
        @NotNull Long outputItemId,
        String colour,
        String size,
        BigDecimal batchQty,
        @NotEmpty List<ComponentLine> components) {

    public record ComponentLine(@NotNull Long componentItemId, @NotNull BigDecimal quantity) {
    }
}
