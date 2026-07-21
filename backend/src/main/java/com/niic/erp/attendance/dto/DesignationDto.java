package com.niic.erp.attendance.dto;

import com.niic.erp.attendance.Designation;
import jakarta.validation.constraints.NotBlank;

public record DesignationDto(Long id, @NotBlank String name) {
    public static DesignationDto from(Designation designation) {
        return new DesignationDto(designation.getId(), designation.getName());
    }
}
