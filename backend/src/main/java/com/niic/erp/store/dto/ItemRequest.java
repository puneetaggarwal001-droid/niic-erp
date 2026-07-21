package com.niic.erp.store.dto;

import com.niic.erp.store.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ItemRequest(
        String itemCode,
        @NotBlank String name,
        @NotNull ItemType itemType,
        @NotBlank String unit,
        Long categoryId,
        BigDecimal reorderLevel,
        List<VariantSpecDto> variants) {

    public record VariantSpecDto(String colour, String size) {
    }
}
