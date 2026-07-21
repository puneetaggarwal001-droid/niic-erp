package com.niic.erp.store.dto;

import com.niic.erp.store.StockTxnType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record StockTxnRequest(
        @NotNull Long itemId,
        Long variantId,
        @NotNull StockTxnType txnType,
        @NotNull BigDecimal quantity,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        String reference,
        String note) {
}
