package com.niic.erp.production.dto;

import com.niic.erp.production.Side;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record QcEntryCreateRequest(@NotNull LocalDate date, @NotNull Long jobId, Long colourId, Long sizeId,
                                    Side side, String opRef, Long workstationId, @Min(1) int totalChecked,
                                    @Min(0) int passQty, @Min(0) int alterQty, @Min(0) int rejectQty,
                                    boolean skipDetails) {
}
