package com.niic.erp.reports;

import java.util.List;

/**
 * Work-in-progress report. For every active job it compares, per operation
 * (workstation + operation), the pieces logged in production entries against
 * the pieces accounted for by operation closures. The difference is the WIP
 * still open on the floor for that operation.
 *
 * <p>Produced is the sum of {@code ProductionEntryOp.quantity}; closed is the
 * sum of {@code OperationClosure.doneQtyAtClosure}. Open = produced − closed
 * (never shown below zero — an over-closure reads as fully cleared).
 */
public record WipResponse(int totalOpen, List<JobWip> jobs) {

    public record JobWip(
            Long jobId,
            String jobDisplayId,
            String modelNo,
            int plannedQty,
            int produced,
            int closed,
            int open,
            List<OpWip> operations) {
    }

    public record OpWip(
            String workstation,
            String operation,
            int produced,
            int closed,
            int open) {
    }
}
