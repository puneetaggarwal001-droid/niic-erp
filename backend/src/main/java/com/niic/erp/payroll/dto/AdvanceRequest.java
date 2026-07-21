package com.niic.erp.payroll.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AdvanceRequest(
        @NotNull Long employeeId,
        @NotNull @Pattern(regexp = "\\d{4}-\\d{2}", message = "periodMonth must be YYYY-MM") String periodMonth,
        @NotNull @Positive BigDecimal amount,
        String reason) {
}
