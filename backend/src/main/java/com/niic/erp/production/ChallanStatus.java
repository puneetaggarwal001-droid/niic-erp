package com.niic.erp.production;

public enum ChallanStatus {
    PENDING, RECEIVED, REJECTED,
    // Reserved for parity with the legacy status label set — no code path sets
    // this yet (see production package-info research notes).
    CANCELLED
}
