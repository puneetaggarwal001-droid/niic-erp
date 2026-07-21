package com.niic.erp.sampling.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Save body for one PPM section's rows (replaces all rows in that section). */
public record PpmForm(
        @NotNull String section,
        List<RowForm> rows) {

    public record RowForm(String text1, String text2, String remark) {
    }
}
