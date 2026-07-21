package com.niic.erp.sampling.dto;

import com.niic.erp.sampling.SampleLine;
import java.math.BigDecimal;

public record SampleLineDto(
        Long id,
        String lineType,
        Long itemId,
        String name,
        String description,
        String colour,
        BigDecimal qty,
        String unit) {

    public static SampleLineDto from(SampleLine l) {
        return new SampleLineDto(l.getId(), l.getLineType().name(), l.getItemId(), l.getName(),
                l.getDescription(), l.getColour(), l.getQty(), l.getUnit());
    }
}
