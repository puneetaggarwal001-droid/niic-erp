package com.niic.erp.production.dto;

import com.niic.erp.production.PcRate;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PcRateDto(Long id, Long workstationId, String workstationName, String styleCode, String modelNo,
                         Long operationId, String operationName, BigDecimal rate, LocalDate effectiveDate,
                         boolean active) {
    public static PcRateDto from(PcRate rate) {
        return new PcRateDto(rate.getId(), rate.getWorkstation().getId(), rate.getWorkstation().getName(),
                rate.getStyle().getCode(), rate.getModelNo(), rate.getOperation().getId(),
                rate.getOperation().getName(), rate.getRate(), rate.getEffectiveDate(), rate.isActive());
    }
}
