package com.niic.erp.production.dto;

import com.niic.erp.production.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OperationDto(Long id, @NotNull Long workstationId, String workstationName, @NotBlank String name,
                            boolean active) {
    public static OperationDto from(Operation operation) {
        return new OperationDto(operation.getId(), operation.getWorkstation().getId(),
                operation.getWorkstation().getName(), operation.getName(), operation.isActive());
    }
}
