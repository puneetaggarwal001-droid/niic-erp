package com.niic.erp.payroll.dto;

import jakarta.validation.constraints.NotBlank;

public record ContractorRequest(@NotBlank String name, String phone) {
}
