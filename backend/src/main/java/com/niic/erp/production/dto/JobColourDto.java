package com.niic.erp.production.dto;

import com.niic.erp.production.JobColour;

public record JobColourDto(Long id, String name, java.util.List<JobSizeDto> sizes) {
    public static JobColourDto from(JobColour colour) {
        return new JobColourDto(colour.getId(), colour.getName(),
                colour.getSizes().stream().map(JobSizeDto::from).toList());
    }
}
