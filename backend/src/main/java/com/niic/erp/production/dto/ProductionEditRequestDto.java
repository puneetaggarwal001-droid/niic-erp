package com.niic.erp.production.dto;

import com.niic.erp.common.RequestStatus;
import com.niic.erp.production.ProductionEditRequest;

public record ProductionEditRequestDto(Long id, Long jobId, String jobDisplayId, Long colourId, Long sizeId,
                                        String requestedByUsername, String reason, RequestStatus status,
                                        boolean used, String resolvedByUsername, String resolution) {
    public static ProductionEditRequestDto from(ProductionEditRequest request) {
        return new ProductionEditRequestDto(request.getId(), request.getJob().getId(),
                request.getJob().getJobDisplayId(), request.getColour() != null ? request.getColour().getId() : null,
                request.getSize() != null ? request.getSize().getId() : null, request.getRequestedBy().getUsername(),
                request.getReason(), request.getStatus(), request.isUsed(),
                request.getResolvedBy() != null ? request.getResolvedBy().getUsername() : null,
                request.getResolution());
    }
}
