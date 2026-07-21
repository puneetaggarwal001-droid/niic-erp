package com.niic.erp.payroll.dto;

import com.niic.erp.payroll.ContractorBill;
import java.math.BigDecimal;
import java.time.Instant;

public record ContractorBillDto(
        Long id,
        Long contractorId,
        String contractorName,
        String periodMonth,
        String rateType,
        BigDecimal quantity,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal advancesDeducted,
        BigDecimal netPayable,
        String notes,
        String status,
        Instant finalizedAt) {

    public static ContractorBillDto from(ContractorBill b) {
        return new ContractorBillDto(
                b.getId(), b.getContractor().getId(), b.getContractor().getName(), b.getPeriodMonth(),
                b.getRateType().name(), b.getQuantity(), b.getRate(), b.getAmount(), b.getAdvancesDeducted(),
                b.getNetPayable(), b.getNotes(), b.getStatus().name(), b.getFinalizedAt());
    }
}
