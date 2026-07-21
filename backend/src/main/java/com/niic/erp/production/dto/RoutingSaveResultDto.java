package com.niic.erp.production.dto;

// Either `routing` (admin saved directly) or `pendingRequest` (non-admin
// submission awaiting approval) is populated, never both.
public record RoutingSaveResultDto(RoutingDto routing, RoutingChangeRequestDto pendingRequest) {
    public static RoutingSaveResultDto direct(RoutingDto routing) {
        return new RoutingSaveResultDto(routing, null);
    }

    public static RoutingSaveResultDto pending(RoutingChangeRequestDto pendingRequest) {
        return new RoutingSaveResultDto(null, pendingRequest);
    }
}
