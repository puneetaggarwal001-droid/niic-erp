package com.niic.erp.payroll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.niic.erp.attendance.AttendanceRecord;
import com.niic.erp.attendance.AttendanceRecordRepository;
import com.niic.erp.attendance.Employee;
import com.niic.erp.attendance.EmployeeRepository;
import com.niic.erp.attendance.SalaryType;
import com.niic.erp.production.ProductionEntry;
import com.niic.erp.production.ProductionEntryOp;
import com.niic.erp.production.ProductionEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the wage formulas, driven through {@link PayrollService#generateDraft}
 * with the data layer mocked so the arithmetic is exercised in isolation.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock AttendanceRecordRepository attendanceRepository;
    @Mock ProductionEntryRepository productionEntryRepository;
    @Mock PayrollAdvanceRepository advanceRepository;
    @Mock PayrollRunRepository runRepository;
    @Mock ContractorRepository contractorRepository;
    @Mock ContractorBillRepository contractorBillRepository;

    PayrollService service;

    @BeforeEach
    void setUp() {
        service = new PayrollService(employeeRepository, attendanceRepository, productionEntryRepository,
                advanceRepository, runRepository, contractorRepository, contractorBillRepository);
        when(runRepository.existsByPeriodMonthAndStatus(any(), eq(PayrollRunStatus.FINALIZED))).thenReturn(false);
        when(runRepository.findByPeriodMonthAndStatus(any(), eq(PayrollRunStatus.DRAFT)))
                .thenReturn(java.util.Optional.empty());
        // save() returns its argument so we can inspect the computed lines.
        when(runRepository.save(any(PayrollRun.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void salariedRegularPayPlusOvertime() {
        Employee emp = employee("EMP-001", SalaryType.SALARIED);
        emp.setSalary(new BigDecimal("12480")); // per-day = 480, per-minute = 1.0
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(emp));

        // Two present days, each 9h (540 min) worked -> 60 OT min/day -> 120 OT min total.
        when(attendanceRepository.findByEmployee_IdAndDateBetween(any(), any(), any()))
                .thenReturn(List.of(attendance(LocalTime.of(9, 0), LocalTime.of(18, 0)),
                        attendance(LocalTime.of(9, 0), LocalTime.of(18, 0))));
        when(advanceRepository.findByEmployeeIdAndPeriodMonthAndDeductedFalse(any(), any()))
                .thenReturn(List.of());

        PayrollRun run = service.generateDraft("2026-06");
        PayrollRunLine line = run.getLines().get(0);

        assertThat(line.getPresentDays()).isEqualTo(2);
        assertThat(line.getOvertimeMinutes()).isEqualTo(120);
        // regular = 2 * 480 = 960; OT = 120 min * 1.0/min = 120; gross = 1080.
        assertThat(line.getGrossPay()).isEqualByComparingTo("1080.00");
        assertThat(line.getNetPay()).isEqualByComparingTo("1080.00");
    }

    @Test
    void advancesReduceNetAndNeverGoNegative() {
        Employee emp = employee("EMP-002", SalaryType.SALARIED);
        emp.setSalary(new BigDecimal("12480"));
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(emp));
        when(attendanceRepository.findByEmployee_IdAndDateBetween(any(), any(), any()))
                .thenReturn(List.of(attendance(LocalTime.of(9, 0), LocalTime.of(17, 0)))); // exactly 480 -> no OT
        // Advance far larger than gross -> net floored at 0.
        when(advanceRepository.findByEmployeeIdAndPeriodMonthAndDeductedFalse(any(), any()))
                .thenReturn(List.of(new PayrollAdvance(emp, "2026-06", new BigDecimal("100000"), "loan")));

        PayrollRun run = service.generateDraft("2026-06");
        PayrollRunLine line = run.getLines().get(0);

        assertThat(line.getOvertimeMinutes()).isZero();
        assertThat(line.getGrossPay()).isEqualByComparingTo("480.00"); // 1 day * 480
        assertThat(line.getAdvancesDeducted()).isEqualByComparingTo("100000.00");
        assertThat(line.getNetPay()).isEqualByComparingTo("0.00");
    }

    @Test
    void pcRateGrossIsPiecesTimesRate() {
        Employee emp = employee("EMP-003", SalaryType.PC_RATE);
        emp.setPcRate(new BigDecimal("3.50"));
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(emp));
        when(attendanceRepository.findByEmployee_IdAndDateBetween(any(), any(), any())).thenReturn(List.of());
        when(advanceRepository.findByEmployeeIdAndPeriodMonthAndDeductedFalse(any(), any())).thenReturn(List.of());
        // 40 + 60 = 100 pieces across two entries.
        when(productionEntryRepository.findByEmployeeIdAndDateBetween(any(), any(), any()))
                .thenReturn(List.of(entryWithPieces(40), entryWithPieces(60)));

        PayrollRun run = service.generateDraft("2026-06");
        PayrollRunLine line = run.getLines().get(0);

        assertThat(line.getTotalPieces()).isEqualTo(100);
        assertThat(line.getGrossPay()).isEqualByComparingTo("350.00"); // 100 * 3.50
    }

    @Test
    void contractorEmployeesAreExcludedFromRun() {
        Employee contractor = employee("EMP-004", SalaryType.CONTRACTOR);
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(contractor));

        PayrollRun run = service.generateDraft("2026-06");

        assertThat(run.getLines()).isEmpty();
    }

    // ---- helpers ----

    private static Employee employee(String empId, SalaryType type) {
        return new Employee(empId, "Test " + empId, "111122223333", "9999999999", null, LocalDate.of(2025, 1, 1), type);
    }

    private static AttendanceRecord attendance(LocalTime in, LocalTime out) {
        AttendanceRecord r = new AttendanceRecord(LocalDate.of(2026, 6, 1), null, null, null);
        r.setEntryTime(in);
        r.setExitTime(out);
        return r;
    }

    private static ProductionEntry entryWithPieces(int pieces) {
        ProductionEntry entry = new ProductionEntry(LocalDate.of(2026, 6, 1), null, null, null, null, null, null, null);
        entry.getOperations().add(new ProductionEntryOp(entry, null, null, pieces, null));
        return entry;
    }
}
