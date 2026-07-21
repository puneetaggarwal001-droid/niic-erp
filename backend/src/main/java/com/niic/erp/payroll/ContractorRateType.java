package com.niic.erp.payroll;

public enum ContractorRateType {
    /** Bill = sum of (rate x quantity) across operation lines. */
    PER_OPERATION,
    /** Bill = finished pieces x rate. */
    FINISHED_PIECES
}
