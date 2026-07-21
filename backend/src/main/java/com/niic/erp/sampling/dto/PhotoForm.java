package com.niic.erp.sampling.dto;

import jakarta.validation.constraints.NotBlank;

/** Add-photo body: a section tag and an inline base64 data URL. */
public record PhotoForm(
        @NotBlank String section,
        @NotBlank String dataUrl,
        String caption) {
}
