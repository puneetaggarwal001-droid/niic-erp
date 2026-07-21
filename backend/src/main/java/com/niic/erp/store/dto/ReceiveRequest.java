package com.niic.erp.store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ReceiveRequest(@NotEmpty List<Receipt> receipts) {

    public record Receipt(@NotNull Long poItemId, @NotNull BigDecimal quantity) {
    }
}
