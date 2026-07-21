package com.niic.erp.attendance.dto;

import com.niic.erp.attendance.AttendanceRecord;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceRecordDto(
        Long id,
        LocalDate date,
        Long employeeId,
        String empId,
        String employeeName,
        Long designationId,
        String designationName,
        LocalTime entryTime,
        LocalTime exitTime,
        String enteredByUsername,
        Instant enteredAt,
        String lastEditedByUsername,
        Instant lastEditedAt) {

    public static AttendanceRecordDto from(AttendanceRecord r) {
        return new AttendanceRecordDto(
                r.getId(),
                r.getDate(),
                r.getEmployee().getId(),
                r.getEmployee().getEmpId(),
                r.getEmployee().getName(),
                r.getDesignation().getId(),
                r.getDesignation().getName(),
                r.getEntryTime(),
                r.getExitTime(),
                r.getEnteredBy() != null ? r.getEnteredBy().getUsername() : null,
                r.getEnteredAt(),
                r.getLastEditedBy() != null ? r.getLastEditedBy().getUsername() : null,
                r.getLastEditedAt());
    }
}
