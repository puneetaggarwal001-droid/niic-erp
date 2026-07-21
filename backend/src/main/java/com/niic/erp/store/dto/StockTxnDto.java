package com.niic.erp.store.dto;

import com.niic.erp.store.StockTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;

public record StockTxnDto(
        Long id,
        Long itemId,
        String itemCode,
        String itemName,
        Long variantId,
        String variantLabel,
        String txnType,
        BigDecimal quantity,
        LocalDate txnDate,
        String reference,
        String note,
        String createdByUsername) {

    public static StockTxnDto from(StockTransaction t) {
        return new StockTxnDto(
                t.getId(), t.getItem().getId(), t.getItem().getItemCode(), t.getItem().getName(),
                t.getVariant() != null ? t.getVariant().getId() : null,
                t.getVariant() != null ? t.getVariant().getColour() + "/" + t.getVariant().getSize() : null,
                t.getTxnType().name(), t.getQuantity(), t.getTxnDate(), t.getReference(), t.getNote(),
                t.getCreatedBy() != null ? t.getCreatedBy().getUsername() : null);
    }
}
