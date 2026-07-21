package com.niic.erp.production.dto;

import com.niic.erp.common.RequestStatus;
import com.niic.erp.production.JobRequest;
import com.niic.erp.production.Unit;
import java.time.Instant;
import java.util.List;

public record JobRequestDto(Long id, String styleCode, String styleLabel, String modelNo, Long fgItemId, Unit unit,
                             List<JobColourRequest> colours, RequestStatus status, String requestedByUsername,
                             String reviewedByUsername, Instant reviewedAt, String adminRemark) {
    public static JobRequestDto from(JobRequest request, List<JobColourRequest> colours) {
        return new JobRequestDto(request.getId(), request.getStyle().getCode(), request.getStyle().getLabel(),
                request.getModelNo(), request.getFgItemId(), request.getUnit(), colours, request.getStatus(),
                request.getRequestedBy().getUsername(),
                request.getReviewedBy() != null ? request.getReviewedBy().getUsername() : null,
                request.getReviewedAt(), request.getAdminRemark());
    }
}
