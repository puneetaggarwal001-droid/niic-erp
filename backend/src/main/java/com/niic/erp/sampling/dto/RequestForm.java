package com.niic.erp.sampling.dto;

import jakarta.validation.constraints.NotBlank;

/** Create/update body for an admin sample request. */
public record RequestForm(
        @NotBlank String reqType,
        @NotBlank String title,
        String description,
        String priority,
        Long refSampleId) {
}
