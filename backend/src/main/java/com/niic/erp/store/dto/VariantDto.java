package com.niic.erp.store.dto;

import com.niic.erp.store.StoreVariant;

public record VariantDto(Long id, String colour, String size) {

    public static VariantDto from(StoreVariant v) {
        return new VariantDto(v.getId(), v.getColour(), v.getSize());
    }
}
