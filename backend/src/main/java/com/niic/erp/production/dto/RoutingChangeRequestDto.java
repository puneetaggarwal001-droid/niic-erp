package com.niic.erp.production.dto;

import com.niic.erp.common.RequestStatus;
import com.niic.erp.production.RoutingChangeRequest;
import java.time.Instant;
import java.util.List;

public record RoutingChangeRequestDto(Long id, Long jobId, String jobDisplayId,
                                       List<RoutingWorkstationRequest> proposedWorkstations, String reason,
                                       RequestStatus status, String requestedByUsername, String reviewedByUsername,
                                       Instant reviewedAt, String adminRemark) {
    public static RoutingChangeRequestDto from(RoutingChangeRequest request, List<RoutingWorkstationRequest> proposed) {
        return new RoutingChangeRequestDto(request.getId(), request.getJob().getId(),
                request.getJob().getJobDisplayId(), proposed, request.getReason(), request.getStatus(),
                request.getRequestedBy().getUsername(),
                request.getReviewedBy() != null ? request.getReviewedBy().getUsername() : null,
                request.getReviewedAt(), request.getAdminRemark());
    }
}
