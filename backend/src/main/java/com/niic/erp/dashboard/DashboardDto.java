package com.niic.erp.dashboard;

import java.util.Map;

/** Cross-module KPI snapshot for the management dashboard. */
public record DashboardDto(Hr hr, Production production, Store store, Sampling sampling, Payroll payroll) {

    public record Hr(long totalEmployees, long activeEmployees, long presentToday, long gatePassesToday) {
    }

    public record Production(
            long activeJobs,
            long unitsToday,
            long pendingJobRequests,
            long pendingEditRequests,
            long pendingRoutingRequests,
            long pendingQc,
            long pendingTransfers) {
    }

    public record Store(
            long totalItems,
            long pendingItemApprovals,
            long belowReorder,
            long openPurchaseOrders,
            long pendingRequisitions) {
    }

    public record Sampling(Map<String, Long> pipeline, long activeSamples, long pendingRequests) {
    }

    public record Payroll(String lastRunMonth, String lastRunStatus) {
    }
}
