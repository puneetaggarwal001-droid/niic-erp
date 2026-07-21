package com.niic.erp.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByDate(LocalDate date);
    Optional<AttendanceRecord> findByDateAndEmployee_Id(LocalDate date, Long employeeId);
    List<AttendanceRecord> findByEmployee_IdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);
}
