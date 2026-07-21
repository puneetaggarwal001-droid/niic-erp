package com.niic.erp.production.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RoutingOperationRequest(@NotNull Long operationId, List<Long> dependsOnOperationIds) {
}
