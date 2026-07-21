package com.niic.erp.store.dto;

import com.niic.erp.store.PurchaseOrder;
import java.math.BigDecimal;
import java.util.List;

public record PurchaseOrderDto(
        Long id,
        String poNumber,
        String supplierName,
        String status,
        String notes,
        List<Line> items) {

    public record Line(
            Long id,
            Long itemId,
            String itemCode,
            String itemName,
            BigDecimal orderedQty,
            BigDecimal receivedQty,
            BigDecimal rate) {
    }

    public static PurchaseOrderDto from(PurchaseOrder po) {
        List<Line> lines = po.getItems().stream()
                .map(i -> new Line(i.getId(), i.getItem().getId(), i.getItem().getItemCode(),
                        i.getItem().getName(), i.getOrderedQty(), i.getReceivedQty(), i.getRate()))
                .toList();
        return new PurchaseOrderDto(po.getId(), po.getPoNumber(), po.getSupplierName(),
                po.getStatus().name(), po.getNotes(), lines);
    }
}
