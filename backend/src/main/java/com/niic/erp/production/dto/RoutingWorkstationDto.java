package com.niic.erp.production.dto;

import com.niic.erp.production.RoutingWorkstation;

public record RoutingWorkstationDto(Long id, Long workstationId, String workstationName,
                                     java.util.List<RoutingOperationDto> operations) {
    public static RoutingWorkstationDto from(RoutingWorkstation ws) {
        return new RoutingWorkstationDto(ws.getId(), ws.getWorkstation().getId(), ws.getWorkstation().getName(),
                ws.getOperations().stream().map(RoutingOperationDto::from).toList());
    }
}
