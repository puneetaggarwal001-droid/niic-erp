package com.niic.erp.store.dto;

import com.niic.erp.store.StoreItem;
import java.math.BigDecimal;
import java.util.List;

public record ItemDto(
        Long id,
        String itemCode,
        String name,
        String itemType,
        String unit,
        Long categoryId,
        String categoryName,
        BigDecimal reorderLevel,
        BigDecimal onHand,
        boolean belowReorder,
        String approvalStatus,
        String rejectionReason,
        List<VariantDto> variants) {

    public static ItemDto from(StoreItem item, BigDecimal onHand) {
        BigDecimal stock = onHand != null ? onHand : BigDecimal.ZERO;
        return new ItemDto(
                item.getId(), item.getItemCode(), item.getName(), item.getItemType().name(), item.getUnit(),
                item.getCategory() != null ? item.getCategory().getId() : null,
                item.getCategory() != null ? item.getCategory().getName() : null,
                item.getReorderLevel(), stock,
                stock.compareTo(item.getReorderLevel()) < 0,
                item.getApprovalStatus().name(), item.getRejectionReason(),
                item.getVariants().stream().map(VariantDto::from).toList());
    }
}
