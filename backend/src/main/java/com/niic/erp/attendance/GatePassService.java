package com.niic.erp.attendance;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.security.CurrentUserProvider;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GatePassService {

    private final GatePassRepository gatePassRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final CurrentUserProvider currentUserProvider;
    private final int monthlyLimit;

    public GatePassService(GatePassRepository gatePassRepository, EmployeeRepository employeeRepository,
                           AttendanceRecordRepository attendanceRepository, CurrentUserProvider currentUserProvider,
                           @Value("${erp.gatepass.monthly-limit:3}") int monthlyLimit) {
        this.gatePassRepository = gatePassRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.currentUserProvider = currentUserProvider;
        this.monthlyLimit = monthlyLimit;
    }

    @Transactional
    public GatePass issue(Long employeeId, LocalDate date, String purpose) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee " + employeeId + " not found."));
        // Attendance on the date is a prerequisite — you can't leave on a pass if you
        // were never marked in.
        if (attendanceRepository.findByDateAndEmployee_Id(date, employeeId).isEmpty()) {
            throw new BadRequestException("Employee has no attendance on " + date + "; mark attendance first.");
        }
        if (purpose == null || purpose.isBlank()) {
            throw new BadRequestException("Purpose is required.");
        }

        YearMonth ym = YearMonth.from(date);
        int priorThisMonth = gatePassRepository.countByEmployeeIdAndDateBetween(
                employeeId, ym.atDay(1), ym.atEndOfMonth());

        GatePass pass = new GatePass(employee, date, purpose.trim(), currentUserProvider.require());
        // This pass is the (priorThisMonth + 1)th of the month; over the limit ⇒ penalty.
        pass.setPenalty(priorThisMonth + 1 > monthlyLimit);
        return gatePassRepository.save(pass);
    }

    public List<GatePass> listByDate(LocalDate date) {
        return gatePassRepository.findByDateOrderByCreatedAtDesc(date);
    }

    public int monthlyLimit() {
        return monthlyLimit;
    }

    public int usedThisMonth(Long employeeId, LocalDate date) {
        YearMonth ym = YearMonth.from(date);
        return gatePassRepository.countByEmployeeIdAndDateBetween(employeeId, ym.atDay(1), ym.atEndOfMonth());
    }
}
