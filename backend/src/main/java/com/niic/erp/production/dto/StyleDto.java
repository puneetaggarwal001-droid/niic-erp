package com.niic.erp.production.dto;

import com.niic.erp.production.Style;

public record StyleDto(Long id, String code, String label, boolean active) {
    public static StyleDto from(Style style) {
        return new StyleDto(style.getId(), style.getCode(), style.getLabel(), style.isActive());
    }
}
