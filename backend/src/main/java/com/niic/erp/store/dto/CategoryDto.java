package com.niic.erp.store.dto;

import com.niic.erp.store.StoreCategory;

public record CategoryDto(Long id, String name, String code, Long parentId, String parentName) {

    public static CategoryDto from(StoreCategory c) {
        return new CategoryDto(c.getId(), c.getName(), c.getCode(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getParent() != null ? c.getParent().getName() : null);
    }
}
