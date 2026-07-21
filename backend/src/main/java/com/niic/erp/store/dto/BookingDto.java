package com.niic.erp.store.dto;

import com.niic.erp.store.StockBooking;
import java.math.BigDecimal;

public record BookingDto(
        Long id,
        Long itemId,
        String itemCode,
        String itemName,
        Long jobId,
        String jobDisplayId,
        BigDecimal bookedQty,
        BigDecimal issuedQty,
        BigDecimal outstanding,
        String status) {

    public static BookingDto from(StockBooking b) {
        return new BookingDto(
                b.getId(), b.getItem().getId(), b.getItem().getItemCode(), b.getItem().getName(),
                b.getJob().getId(), b.getJob().getJobDisplayId(),
                b.getBookedQty(), b.getIssuedQty(), b.outstanding(), b.getStatus().name());
    }
}
