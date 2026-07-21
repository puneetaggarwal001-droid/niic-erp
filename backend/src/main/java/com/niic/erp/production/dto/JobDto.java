package com.niic.erp.production.dto;

import com.niic.erp.production.Job;
import com.niic.erp.production.JobSource;
import com.niic.erp.production.Unit;
import java.util.List;

public record JobDto(Long id, String jobDisplayId, String styleCode, String styleLabel, String modelNo,
                      Long fgItemId, Unit unit, int totalPlannedQty, boolean active, JobSource source,
                      boolean routingAssigned, List<JobColourDto> colours) {
    public static JobDto from(Job job) {
        return new JobDto(job.getId(), job.getJobDisplayId(), job.getStyle().getCode(), job.getStyle().getLabel(),
                job.getModelNo(), job.getFgItemId(), job.getUnit(), job.getTotalPlannedQty(), job.isActive(),
                job.getSource(), job.isRoutingAssigned(),
                job.getColours().stream().map(JobColourDto::from).toList());
    }
}
