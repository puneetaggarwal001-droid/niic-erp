package com.niic.erp.production.dto;

import com.niic.erp.production.TransferChallanItem;
import java.math.BigDecimal;

public record TransferChallanItemDto(Long id, Long itemId, String itemCode, String itemName, String itemUnit,
                                      BigDecimal qty) {
    public static TransferChallanItemDto from(TransferChallanItem item) {
        return new TransferChallanItemDto(item.getId(), item.getItemId(), item.getItemCode(), item.getItemName(),
                item.getItemUnit(), item.getQty());
    }
}
