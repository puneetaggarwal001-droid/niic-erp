package com.niic.erp.sampling.dto;

import jakarta.validation.constraints.NotNull;

/** Body used by the sampler to complete a request, linking the produced sample. */
public record CompleteRequestForm(@NotNull Long completedSampleId) {
}
