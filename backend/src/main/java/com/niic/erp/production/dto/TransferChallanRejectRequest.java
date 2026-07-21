package com.niic.erp.production.dto;

import jakarta.validation.constraints.NotBlank;

public record TransferChallanRejectRequest(@NotBlank String reason) {
}
