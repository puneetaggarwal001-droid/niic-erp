package com.niic.erp.production.dto;

import com.niic.erp.production.RoutingOperation;

public record RoutingOperationDto(Long id, Long operationId, String operationName,
                                   java.util.List<Long> dependsOnOperationIds) {
    public static RoutingOperationDto from(RoutingOperation op) {
        return new RoutingOperationDto(op.getId(), op.getOperation().getId(), op.getOperation().getName(),
                op.getDependsOn().stream().map(dep -> dep.getOperation().getId()).toList());
    }
}
