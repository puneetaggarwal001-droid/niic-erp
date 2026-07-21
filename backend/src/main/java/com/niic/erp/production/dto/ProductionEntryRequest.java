package com.niic.erp.production.dto;

import com.niic.erp.production.Side;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ProductionEntryRequest(@NotNull LocalDate date, @NotNull Long employeeId, @NotNull Long jobId,
                                      Long colourId, Long sizeId, Side side,
                                      @NotEmpty List<@Valid ProductionEntryOpRequest> operations) {
}
