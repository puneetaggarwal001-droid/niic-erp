package com.niic.erp.production.dto;

import com.niic.erp.production.QcRework;
import com.niic.erp.production.ReworkStatus;

public record QcReworkDto(Long id, Long qcEntryId, Long jobId, String jobDisplayId, int alterQty, int reworkDone,
                           int reworkReject, ReworkStatus status) {
    public static QcReworkDto from(QcRework rework) {
        return new QcReworkDto(rework.getId(), rework.getQcEntry().getId(), rework.getJob().getId(),
                rework.getJob().getJobDisplayId(), rework.getAlterQty(), rework.getReworkDone(),
                rework.getReworkReject(), rework.getStatus());
    }
}
