package com.niic.erp.reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Employee Production Report — piece-rate income earned by PC_RATE employees over
 * a date range. Income per operation = active PC rate(workstation, operation) x
 * pieces logged; aggregated per employee, per day and per operation.
 */
public record EprResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal grandTotalIncome,
        int grandTotalPieces,
        List<EmployeeReport> employees) {

    public record EmployeeReport(
            Long employeeId,
            String empId,
            String name,
            int totalPieces,
            BigDecimal totalIncome,
            List<Day> days,
            List<OpLine> operations) {
    }

    public record Day(LocalDate date, int pieces, BigDecimal income) {
    }

    public record OpLine(
            String workstation,
            String operation,
            int pieces,
            BigDecimal rate,
            BigDecimal income) {
    }
}
