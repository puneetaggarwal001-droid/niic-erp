package com.niic.erp.attendance.dto;

import com.niic.erp.attendance.EmployeeType;
import com.niic.erp.attendance.SalaryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** Request body for both create and update — mirrors _collectEmpForm() in the legacy app. */
public record EmployeeRequest(
        @NotBlank String name,
        String address,
        @NotBlank @Pattern(regexp = "\\d{12}", message = "Aadhaar must be 12 digits") String aadhar,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits") String phone,
        @NotNull Long designationId,
        @NotNull LocalDate dateOfJoining,
        @NotNull SalaryType salaryType,
        BigDecimal salary,
        BigDecimal pcRate,
        String contractorName,
        EmployeeType empType,
        LocalDate validTill,
        String department,
        String notes,
        Set<String> authorizedWorkstations,
        String photoUrl) {
}
