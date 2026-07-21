package com.niic.erp.sampling.dto;

import com.niic.erp.sampling.SampleRequest;
import java.time.Instant;

public record SampleRequestDto(
        Long id,
        String reqType,
        String title,
        String description,
        String priority,
        Long refSampleId,
        String refSampleNo,
        String status,
        Long completedSampleId,
        String completedSampleNo,
        String requestedBy,
        Instant createdAt,
        Instant adminResolvedAt,
        String adminResolvedBy,
        String adminResolvedRemark) {

    public static SampleRequestDto from(SampleRequest r, String refSampleNo, String completedSampleNo) {
        return new SampleRequestDto(
                r.getId(), r.getReqType().name(), r.getTitle(), r.getDescription(), r.getPriority(),
                r.getRefSampleId(), refSampleNo, r.getStatus().name(),
                r.getCompletedSampleId(), completedSampleNo, r.getRequestedBy(), r.getCreatedAt(),
                r.getAdminResolvedAt(), r.getAdminResolvedBy(),
                r.getAdminResolvedRemark() != null ? r.getAdminResolvedRemark().name() : null);
    }
}
