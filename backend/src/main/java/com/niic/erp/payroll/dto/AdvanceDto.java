package com.niic.erp.payroll.dto;

import com.niic.erp.payroll.PayrollAdvance;
import java.math.BigDecimal;

public record AdvanceDto(
        Long id,
        Long employeeId,
        String empId,
        String employeeName,
        String periodMonth,
        BigDecimal amount,
        String reason,
        boolean deducted) {

    public static AdvanceDto from(PayrollAdvance a) {
        return new AdvanceDto(
                a.getId(), a.getEmployee().getId(), a.getEmployee().getEmpId(), a.getEmployee().getName(),
                a.getPeriodMonth(), a.getAmount(), a.getReason(), a.isDeducted());
    }
}
