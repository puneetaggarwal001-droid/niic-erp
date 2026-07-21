package com.niic.erp.production.dto;

import com.niic.erp.production.JobSize;

public record JobSizeDto(Long id, String size, int plannedQty, Long variantId) {
    public static JobSizeDto from(JobSize size) {
        return new JobSizeDto(size.getId(), size.getSize(), size.getPlannedQty(), size.getVariantId());
    }
}
