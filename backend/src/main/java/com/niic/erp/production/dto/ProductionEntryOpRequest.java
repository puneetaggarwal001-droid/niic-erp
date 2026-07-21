package com.niic.erp.production.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductionEntryOpRequest(@NotNull Long workstationId, @NotNull Long operationId, @Min(1) int quantity) {
}
