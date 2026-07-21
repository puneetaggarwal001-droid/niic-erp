package com.niic.erp.store;

/**
 * Direction of a stock ledger movement. INWARD/RETURN increase on-hand;
 * ISSUE/REJECT decrease it; ADJUST is a signed correction (can be either).
 */
public enum StockTxnType {
    INWARD,
    ISSUE,
    RETURN,
    REJECT,
    ADJUST
}
