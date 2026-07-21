package com.niic.erp.sampling.dto;

import java.util.List;

/** Save body for the PPS spec. */
public record PpsForm(
        String fabricDetails,
        Integer designCount,
        String specialInstructions,
        List<String> colours,
        List<String> sizes) {
}
