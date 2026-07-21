package com.niic.erp.attendance.dto;

import com.niic.erp.attendance.Employee;
import com.niic.erp.attendance.EmployeeType;
import com.niic.erp.attendance.SalaryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record EmployeeDto(
        Long id,
        String empId,
        String name,
        String address,
        String aadhar,
        String phone,
        Long designationId,
        String designationName,
        LocalDate dateOfJoining,
        SalaryType salaryType,
        BigDecimal salary,
        BigDecimal pcRate,
        String contractorName,
        EmployeeType empType,
        LocalDate validTill,
        String department,
        String notes,
        Set<String> authorizedWorkstations,
        String photoUrl,
        boolean active) {

    public static EmployeeDto from(Employee e) {
        return new EmployeeDto(
                e.getId(), e.getEmpId(), e.getName(), e.getAddress(), e.getAadhar(), e.getPhone(),
                e.getDesignation().getId(), e.getDesignation().getName(), e.getDateOfJoining(),
                e.getSalaryType(), e.getSalary(), e.getPcRate(), e.getContractorName(), e.getEmpType(),
                e.getValidTill(), e.getDepartment(), e.getNotes(), e.getAuthorizedWorkstations(),
                e.getPhotoUrl(), e.isActive());
    }
}
