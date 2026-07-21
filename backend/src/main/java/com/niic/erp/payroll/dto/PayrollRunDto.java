package com.niic.erp.payroll.dto;

import com.niic.erp.payroll.PayrollRun;
import com.niic.erp.payroll.PayrollRunLine;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Full payroll run with its computed lines and rolled-up totals. Used for both the
 * run detail view and (with lines) the finalized payslip listing.
 */
public record PayrollRunDto(
        Long id,
        String periodMonth,
        String status,
        Instant finalizedAt,
        BigDecimal totalGross,
        BigDecimal totalAdvances,
        BigDecimal totalNet,
        List<PayrollRunLineDto> lines) {

    public static PayrollRunDto from(PayrollRun run) {
        List<PayrollRunLineDto> lines = run.getLines().stream().map(PayrollRunLineDto::from).toList();
        BigDecimal gross = run.getLines().stream().map(PayrollRunLine::getGrossPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal advances = run.getLines().stream().map(PayrollRunLine::getAdvancesDeducted)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = run.getLines().stream().map(PayrollRunLine::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PayrollRunDto(run.getId(), run.getPeriodMonth(), run.getStatus().name(),
                run.getFinalizedAt(), gross, advances, net, lines);
    }
}
