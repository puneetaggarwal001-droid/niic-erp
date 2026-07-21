package com.niic.erp.store.dto;

import com.niic.erp.store.Bom;
import java.math.BigDecimal;
import java.util.List;

public record BomDto(
        Long id,
        Long outputItemId,
        String outputItemCode,
        String outputItemName,
        String outputItemType,
        String colour,
        String size,
        BigDecimal batchQty,
        boolean active,
        List<Component> components) {

    public record Component(
            Long componentItemId,
            String itemCode,
            String itemName,
            String itemType,
            BigDecimal quantity) {
    }

    public static BomDto from(Bom bom) {
        List<Component> comps = bom.getComponents().stream()
                .map(c -> new Component(
                        c.getComponentItem().getId(), c.getComponentItem().getItemCode(),
                        c.getComponentItem().getName(), c.getComponentItem().getItemType().name(), c.getQuantity()))
                .toList();
        return new BomDto(
                bom.getId(), bom.getOutputItem().getId(), bom.getOutputItem().getItemCode(),
                bom.getOutputItem().getName(), bom.getOutputItem().getItemType().name(),
                bom.getColour(), bom.getSize(), bom.getBatchQty(), bom.isActive(), comps);
    }
}
