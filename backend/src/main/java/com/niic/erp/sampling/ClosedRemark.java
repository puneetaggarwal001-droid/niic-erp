package com.niic.erp.sampling;

/** Why a sample was closed (drives the "old jobs" resolution history). */
public enum ClosedRemark {
    CHANGE_REQUESTED,
    SELECTED,
    NEW_REVISION,
    REJECTED
}
