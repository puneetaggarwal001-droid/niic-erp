package com.niic.erp.production.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RoutingWorkstationRequest(@NotNull Long workstationId,
                                         @NotEmpty List<@Valid RoutingOperationRequest> operations) {
}
