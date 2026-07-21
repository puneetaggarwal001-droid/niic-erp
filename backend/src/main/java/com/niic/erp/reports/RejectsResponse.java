package com.niic.erp.reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Rejects &amp; rework report over a date range. Aggregates QC entries per job
 * (and per operation within a job): pieces checked, passed, sent to alter and
 * outright rejected, plus the rejects that came back out of rework. Reject %
 * is rejected / checked for the job.
 */
public record RejectsResponse(
        LocalDate from,
        LocalDate to,
        int totalChecked,
        int totalPassed,
        int totalAlter,
        int totalRejected,
        int totalReworkReject,
        List<JobRejects> jobs) {

    public record JobRejects(
            Long jobId,
            String jobDisplayId,
            String modelNo,
            int checked,
            int passed,
            int alter,
            int rejected,
            int reworkReject,
            BigDecimal rejectPct,
            List<OpRejects> operations) {
    }

    public record OpRejects(
            String workstation,
            String operation,
            int checked,
            int passed,
            int alter,
            int rejected) {
    }
}
