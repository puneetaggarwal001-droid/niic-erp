package com.niic.erp.store.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name, @NotBlank String code, Long parentId) {
}
