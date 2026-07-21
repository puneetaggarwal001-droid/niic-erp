package com.niic.erp.payroll;

import com.niic.erp.attendance.AttendanceRecord;
import com.niic.erp.attendance.AttendanceRecordRepository;
import com.niic.erp.attendance.Employee;
import com.niic.erp.attendance.EmployeeRepository;
import com.niic.erp.attendance.SalaryType;
import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.ProductionEntry;
import com.niic.erp.production.ProductionEntryOp;
import com.niic.erp.production.ProductionEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wage computation ported from the legacy attendance module (line ~10741):
 *  - Salaried: per-day = salary / 26; regular = presentDays x per-day. Overtime =
 *    sum of max(0, workedMinutes - 480) across the month; per-minute = salary /
 *    (26 x 480); overtimePay = overtimeMinutes x per-minute. gross = regular + OT.
 *  - PC-rate: gross = (sum of production pieces in month) x employee pcRate.
 *  - net = max(0, gross - pending advances for the month).
 */
@Service
public class PayrollService {

    static final int STD_WORK_MIN = 480;
    static final BigDecimal STD_WORK_DAYS = BigDecimal.valueOf(26);
    static final BigDecimal STD_MONTH_MINUTES = BigDecimal.valueOf(26L * STD_WORK_MIN);
    // Scale for intermediate per-day / per-minute rates before the final 2dp rounding.
    private static final int CALC_SCALE = 6;

    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final ProductionEntryRepository productionEntryRepository;
    private final PayrollAdvanceRepository advanceRepository;
    private final PayrollRunRepository runRepository;
    private final ContractorRepository contractorRepository;
    private final ContractorBillRepository contractorBillRepository;

    public PayrollService(EmployeeRepository employeeRepository, AttendanceRecordRepository attendanceRepository,
                          ProductionEntryRepository productionEntryRepository,
                          PayrollAdvanceRepository advanceRepository, PayrollRunRepository runRepository,
                          ContractorRepository contractorRepository,
                          ContractorBillRepository contractorBillRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.productionEntryRepository = productionEntryRepository;
        this.advanceRepository = advanceRepository;
        this.runRepository = runRepository;
        this.contractorRepository = contractorRepository;
        this.contractorBillRepository = contractorBillRepository;
    }

    // ---- Advances --------------------------------------------------------

    @Transactional
    public PayrollAdvance addAdvance(Long employeeId, String periodMonth, BigDecimal amount, String reason) {
        requireMonth(periodMonth);
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("Advance amount must be positive.");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee " + employeeId + " not found."));
        return advanceRepository.save(new PayrollAdvance(employee, periodMonth, amount, reason));
    }

    public List<PayrollAdvance> listAdvances(String periodMonth) {
        return advanceRepository.findByPeriodMonthOrderByCreatedAtDesc(periodMonth);
    }

    // ---- Payroll run -----------------------------------------------------

    @Transactional
    public PayrollRun generateDraft(String periodMonth) {
        requireMonth(periodMonth);
        if (runRepository.existsByPeriodMonthAndStatus(periodMonth, PayrollRunStatus.FINALIZED)) {
            throw new BadRequestException("Payroll for " + periodMonth + " is already finalized.");
        }
        // A prior draft for the same month is superseded — remove it and recompute.
        runRepository.findByPeriodMonthAndStatus(periodMonth, PayrollRunStatus.DRAFT)
                .ifPresent(runRepository::delete);

        YearMonth ym = YearMonth.parse(periodMonth);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        PayrollRun run = new PayrollRun(periodMonth);
        for (Employee emp : employeeRepository.findByActiveTrue()) {
            // Contractor-type staff are billed via contractor bills, not the run.
            if (emp.getSalaryType() == SalaryType.CONTRACTOR) {
                continue;
            }
            run.getLines().add(computeLine(run, emp, periodMonth, from, to));
        }
        return runRepository.save(run);
    }

    private PayrollRunLine computeLine(PayrollRun run, Employee emp, String periodMonth, LocalDate from, LocalDate to) {
        PayrollRunLine line = new PayrollRunLine(run, emp, emp.getSalaryType());

        List<AttendanceRecord> attendance =
                attendanceRepository.findByEmployee_IdAndDateBetween(emp.getId(), from, to);
        int presentDays = attendance.size();
        int overtimeMinutes = attendance.stream().mapToInt(PayrollService::overtimeMinutes).sum();
        line.setPresentDays(presentDays);
        line.setOvertimeMinutes(overtimeMinutes);

        BigDecimal gross;
        if (emp.getSalaryType() == SalaryType.SALARIED) {
            BigDecimal salary = emp.getSalary() != null ? emp.getSalary() : BigDecimal.ZERO;
            line.setMonthlySalary(salary);
            BigDecimal perDay = salary.divide(STD_WORK_DAYS, CALC_SCALE, RoundingMode.HALF_UP);
            BigDecimal regular = perDay.multiply(BigDecimal.valueOf(presentDays));
            BigDecimal perMin = salary.divide(STD_MONTH_MINUTES, CALC_SCALE, RoundingMode.HALF_UP);
            BigDecimal overtimePay = perMin.multiply(BigDecimal.valueOf(overtimeMinutes));
            gross = regular.add(overtimePay);
        } else { // PC_RATE
            BigDecimal pcRate = emp.getPcRate() != null ? emp.getPcRate() : BigDecimal.ZERO;
            line.setPcRate(pcRate);
            int pieces = sumPieces(emp.getId(), from, to);
            line.setTotalPieces(pieces);
            gross = pcRate.multiply(BigDecimal.valueOf(pieces));
        }

        BigDecimal advances = advanceRepository
                .findByEmployeeIdAndPeriodMonthAndDeductedFalse(emp.getId(), periodMonth)
                .stream().map(PayrollAdvance::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossRounded = money(gross);
        BigDecimal advancesRounded = money(advances);
        BigDecimal net = grossRounded.subtract(advancesRounded);
        if (net.signum() < 0) {
            net = BigDecimal.ZERO;
        }
        line.setGrossPay(grossRounded);
        line.setAdvancesDeducted(advancesRounded);
        line.setNetPay(net);
        return line;
    }

    private int sumPieces(Long employeeId, LocalDate from, LocalDate to) {
        int total = 0;
        for (ProductionEntry entry : productionEntryRepository.findByEmployeeIdAndDateBetween(employeeId, from, to)) {
            for (ProductionEntryOp op : entry.getOperations()) {
                total += op.getQuantity();
            }
        }
        return total;
    }

    private static int overtimeMinutes(AttendanceRecord record) {
        LocalTime entry = record.getEntryTime();
        LocalTime exit = record.getExitTime();
        if (entry == null || exit == null || !exit.isAfter(entry)) {
            return 0;
        }
        long worked = Duration.between(entry, exit).toMinutes();
        return (int) Math.max(0, worked - STD_WORK_MIN);
    }

    public List<PayrollRun> listRuns() {
        return runRepository.findByOrderByPeriodMonthDesc();
    }

    public PayrollRun getRun(Long id) {
        return runRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payroll run " + id + " not found."));
    }

    @Transactional
    public PayrollRun finalizeRun(Long id) {
        PayrollRun run = getRun(id);
        if (run.getStatus() == PayrollRunStatus.FINALIZED) {
            throw new BadRequestException("This payroll run is already finalized.");
        }
        run.setStatus(PayrollRunStatus.FINALIZED);
        run.setFinalizedAt(Instant.now());
        // Mark the advances that fed into these lines as deducted so they aren't
        // taken again in a future month.
        for (PayrollRunLine line : run.getLines()) {
            advanceRepository
                    .findByEmployeeIdAndPeriodMonthAndDeductedFalse(line.getEmployee().getId(), run.getPeriodMonth())
                    .forEach(a -> a.setDeducted(true));
        }
        return run;
    }

    // ---- Contractors & bills --------------------------------------------

    @Transactional
    public Contractor addContractor(String name, String phone) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Contractor name is required.");
        }
        return contractorRepository.save(new Contractor(name.trim(), phone));
    }

    public List<Contractor> listContractors() {
        return contractorRepository.findByActiveTrueOrderByName();
    }

    @Transactional
    public void deactivateContractor(Long id) {
        Contractor c = contractorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contractor " + id + " not found."));
        c.setActive(false);
    }

    @Transactional
    public ContractorBill createBill(Long contractorId, String periodMonth, ContractorRateType rateType,
                                     BigDecimal quantity, BigDecimal rate, BigDecimal advances, String notes) {
        requireMonth(periodMonth);
        Contractor contractor = contractorRepository.findById(contractorId)
                .orElseThrow(() -> new NotFoundException("Contractor " + contractorId + " not found."));
        if (quantity == null || quantity.signum() < 0 || rate == null || rate.signum() < 0) {
            throw new BadRequestException("Quantity and rate must be non-negative.");
        }
        BigDecimal adv = advances != null ? advances : BigDecimal.ZERO;
        return contractorBillRepository.save(
                new ContractorBill(contractor, periodMonth, rateType, quantity, rate, adv, notes));
    }

    public List<ContractorBill> listBills(String periodMonth) {
        return contractorBillRepository.findByPeriodMonthOrderByCreatedAtDesc(periodMonth);
    }

    @Transactional
    public ContractorBill finalizeBill(Long id) {
        ContractorBill bill = contractorBillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contractor bill " + id + " not found."));
        if (bill.getStatus() == PayrollRunStatus.FINALIZED) {
            throw new BadRequestException("This bill is already finalized.");
        }
        bill.setStatus(PayrollRunStatus.FINALIZED);
        bill.setFinalizedAt(Instant.now());
        return bill;
    }

    private static BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static void requireMonth(String periodMonth) {
        if (periodMonth == null) {
            throw new BadRequestException("periodMonth (YYYY-MM) is required.");
        }
        try {
            YearMonth.parse(periodMonth);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("periodMonth must be in YYYY-MM format.");
        }
    }
}
