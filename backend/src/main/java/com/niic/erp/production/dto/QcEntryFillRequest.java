package com.niic.erp.production.dto;

import jakarta.validation.constraints.Min;

public record QcEntryFillRequest(@Min(0) int passQty, @Min(0) int alterQty, @Min(0) int rejectQty) {
}
