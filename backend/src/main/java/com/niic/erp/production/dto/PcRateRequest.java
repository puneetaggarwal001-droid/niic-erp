package com.niic.erp.production.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PcRateRequest(@NotNull Long workstationId, @NotBlank String styleCode, @NotBlank String modelNo,
                             @NotNull Long operationId, @NotNull @DecimalMin("0.01") BigDecimal rate,
                             @NotNull LocalDate effectiveDate) {
}
