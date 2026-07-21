package com.niic.erp.production.dto;

import com.niic.erp.production.Workstation;
import jakarta.validation.constraints.NotBlank;

public record WorkstationDto(Long id, @NotBlank String name, String code, boolean active) {
    public static WorkstationDto from(Workstation workstation) {
        return new WorkstationDto(workstation.getId(), workstation.getName(), workstation.getCode(),
                workstation.isActive());
    }
}
