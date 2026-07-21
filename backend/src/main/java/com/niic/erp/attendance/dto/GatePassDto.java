package com.niic.erp.attendance.dto;

import com.niic.erp.attendance.GatePass;
import java.time.LocalDate;

public record GatePassDto(
        Long id,
        Long employeeId,
        String empId,
        String employeeName,
        String designationName,
        LocalDate date,
        String purpose,
        boolean penalty,
        String issuedByUsername) {

    public static GatePassDto from(GatePass p) {
        return new GatePassDto(
                p.getId(),
                p.getEmployee().getId(),
                p.getEmployee().getEmpId(),
                p.getEmployee().getName(),
                p.getEmployee().getDesignation() != null ? p.getEmployee().getDesignation().getName() : null,
                p.getDate(),
                p.getPurpose(),
                p.isPenalty(),
                p.getIssuedBy() != null ? p.getIssuedBy().getUsername() : null);
    }
}
