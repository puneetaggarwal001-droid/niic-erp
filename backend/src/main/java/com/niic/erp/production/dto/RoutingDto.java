package com.niic.erp.production.dto;

import com.niic.erp.production.Routing;

public record RoutingDto(Long id, Long jobId, String jobDisplayId, java.util.List<RoutingWorkstationDto> workstations) {
    public static RoutingDto from(Routing routing) {
        return new RoutingDto(routing.getId(), routing.getJob().getId(), routing.getJob().getJobDisplayId(),
                routing.getWorkstations().stream().map(RoutingWorkstationDto::from).toList());
    }
}
