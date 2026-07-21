package com.niic.erp.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductionEditRequestSubmitRequest(@NotNull Long jobId, Long colourId, Long sizeId,
                                                   @NotBlank String reason) {
}
