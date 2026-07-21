package com.niic.erp.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceRequest(
        @NotNull LocalDate date,
        @NotNull Long employeeId,
        LocalTime entryTime,
        LocalTime exitTime) {
}
