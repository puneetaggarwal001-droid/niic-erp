package com.niic.erp.production.dto;

import com.niic.erp.production.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record JobCreateRequest(@NotBlank String styleCode, @NotBlank String modelNo, Long fgItemId,
                                @NotNull Unit unit, @NotEmpty List<@Valid JobColourRequest> colours) {
}
