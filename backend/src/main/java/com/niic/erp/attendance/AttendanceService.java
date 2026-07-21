package com.niic.erp.attendance;

import com.niic.erp.common.NotFoundException;
import com.niic.erp.security.CurrentUserProvider;
import com.niic.erp.user.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserProvider currentUserProvider;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository,
                              EmployeeRepository employeeRepository,
                              CurrentUserProvider currentUserProvider) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<AttendanceRecord> findByDate(LocalDate date) {
        return attendanceRecordRepository.findByDate(date);
    }

    /** Creates today's record for the employee, or updates entry/exit times if one already exists. */
    @Transactional
    public AttendanceRecord upsert(LocalDate date, Long employeeId, LocalTime entryTime, LocalTime exitTime) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee " + employeeId + " not found."));
        User currentUser = currentUserProvider.require();

        AttendanceRecord record = attendanceRecordRepository.findByDateAndEmployee_Id(date, employeeId)
                .orElseGet(() -> new AttendanceRecord(date, employee, employee.getDesignation(), currentUser));
        boolean isNew = record.getId() == null;
        record.setEntryTime(entryTime);
        record.setExitTime(exitTime);
        if (!isNew) {
            record.markEdited(currentUser);
        }
        return attendanceRecordRepository.save(record);
    }
}
