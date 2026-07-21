package com.niic.erp.payroll.dto;

import com.niic.erp.payroll.ContractorRateType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ContractorBillRequest(
        @NotNull Long contractorId,
        @NotNull @Pattern(regexp = "\\d{4}-\\d{2}", message = "periodMonth must be YYYY-MM") String periodMonth,
        @NotNull ContractorRateType rateType,
        @NotNull @PositiveOrZero BigDecimal quantity,
        @NotNull @PositiveOrZero BigDecimal rate,
        @PositiveOrZero BigDecimal advances,
        String notes) {
}
