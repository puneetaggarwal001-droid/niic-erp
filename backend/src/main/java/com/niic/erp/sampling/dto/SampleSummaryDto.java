package com.niic.erp.sampling.dto;

import com.niic.erp.sampling.Sample;
import java.time.Instant;
import java.time.LocalDate;

/** Lightweight sample row for the list view (no lazy collections touched). */
public record SampleSummaryDto(
        Long id,
        String sampleNo,
        String revBase,
        int revNum,
        String name,
        String style,
        String category,
        String designer,
        LocalDate date,
        String status,
        String closedRemark,
        String createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public static SampleSummaryDto from(Sample s) {
        return new SampleSummaryDto(
                s.getId(), s.getSampleNo(), s.getRevBase(), revNum(s.getSampleNo()), s.getName(), s.getStyle(),
                s.getCategory(), s.getDesigner(), s.getSampleDate(), s.getStatus().name(),
                s.getClosedRemark() != null ? s.getClosedRemark().name() : null,
                s.getCreatedBy(), s.getCreatedAt(), s.getUpdatedAt());
    }

    /** Revision sequence: original = 0, -R1 = 1, -R2 = 2 … */
    public static int revNum(String sampleNo) {
        if (sampleNo == null) {
            return 0;
        }
        int idx = sampleNo.lastIndexOf("-R");
        if (idx < 0) {
            return 0;
        }
        try {
            return Integer.parseInt(sampleNo.substring(idx + 2));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
