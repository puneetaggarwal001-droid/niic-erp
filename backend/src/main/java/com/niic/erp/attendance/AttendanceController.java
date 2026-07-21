package com.niic.erp.attendance;

import com.niic.erp.attendance.dto.AttendanceRecordDto;
import com.niic.erp.attendance.dto.AttendanceRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public List<AttendanceRecordDto> byDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.findByDate(date).stream().map(AttendanceRecordDto::from).toList();
    }

    @PostMapping
    public AttendanceRecordDto upsert(@Valid @RequestBody AttendanceRequest request) {
        AttendanceRecord record = attendanceService.upsert(
                request.date(), request.employeeId(), request.entryTime(), request.exitTime());
        return AttendanceRecordDto.from(record);
    }
}
