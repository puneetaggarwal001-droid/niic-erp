package com.niic.erp.production.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferChallanItemRequest(Long itemId, String itemCode, @NotBlank String itemName,
                                          @NotBlank String itemUnit, @NotNull @DecimalMin("0.01") BigDecimal qty) {
}
