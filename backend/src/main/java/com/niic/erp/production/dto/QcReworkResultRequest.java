package com.niic.erp.production.dto;

import jakarta.validation.constraints.Min;

public record QcReworkResultRequest(@Min(0) int done, @Min(0) int reject) {
}
