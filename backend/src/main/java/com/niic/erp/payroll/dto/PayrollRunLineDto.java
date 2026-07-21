package com.niic.erp.payroll.dto;

import com.niic.erp.payroll.PayrollRunLine;
import java.math.BigDecimal;

public record PayrollRunLineDto(
        Long employeeId,
        String empId,
        String name,
        String salaryType,
        int presentDays,
        int overtimeMinutes,
        int totalPieces,
        BigDecimal monthlySalary,
        BigDecimal pcRate,
        BigDecimal grossPay,
        BigDecimal advancesDeducted,
        BigDecimal netPay) {

    public static PayrollRunLineDto from(PayrollRunLine l) {
        return new PayrollRunLineDto(
                l.getEmployee().getId(), l.getEmployee().getEmpId(), l.getEmployee().getName(),
                l.getSalaryType().name(), l.getPresentDays(), l.getOvertimeMinutes(), l.getTotalPieces(),
                l.getMonthlySalary(), l.getPcRate(), l.getGrossPay(), l.getAdvancesDeducted(), l.getNetPay());
    }
}
