package com.niic.erp.sampling.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Create/update body for a sample's header and lines. */
public record SampleForm(
        LocalDate date,
        @NotBlank String name,
        String style,
        String category,
        String designer,
        String reference,
        String notes,
        List<LineForm> rawMaterials,
        List<LineForm> sfgItems,
        List<LineForm> operations) {

    public record LineForm(
            Long itemId,
            String name,
            String description,
            String colour,
            BigDecimal qty,
            String unit) {
    }
}
