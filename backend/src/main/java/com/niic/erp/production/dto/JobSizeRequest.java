package com.niic.erp.production.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JobSizeRequest(@NotBlank String size, @Min(1) int plannedQty, Long variantId) {
}
