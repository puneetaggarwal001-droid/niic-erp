package com.niic.erp.production.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TransferChallanCreateRequest(@NotNull Long jobId, @NotNull Long fromWorkstationId,
                                            @NotNull Long toWorkstationId, String remarks,
                                            @NotEmpty List<@Valid TransferChallanItemRequest> items) {
}
