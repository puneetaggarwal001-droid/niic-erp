package com.niic.erp.store.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record FulfilRequest(@NotEmpty List<Fulfilment> fulfilments) {

    public record Fulfilment(@NotNull Long reqItemId, @NotNull BigDecimal quantity) {
    }
}
