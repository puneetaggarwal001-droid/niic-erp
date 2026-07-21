package com.niic.erp.production.dto;

import com.niic.erp.production.QcEntry;
import com.niic.erp.production.QcStatus;
import com.niic.erp.production.Side;
import java.time.LocalDate;

public record QcEntryDto(Long id, LocalDate date, Long jobId, String jobDisplayId, Long colourId, Long sizeId,
                          Side side, String opRef, Long workstationId, String workstationName, int totalChecked,
                          int passQty, int alterQty, int rejectQty, QcStatus status, boolean fromQcWorkstation) {
    public static QcEntryDto from(QcEntry entry) {
        return new QcEntryDto(entry.getId(), entry.getDate(), entry.getJob().getId(), entry.getJob().getJobDisplayId(),
                entry.getColour() != null ? entry.getColour().getId() : null,
                entry.getSize() != null ? entry.getSize().getId() : null, entry.getSide(), entry.getOpRef(),
                entry.getWorkstation() != null ? entry.getWorkstation().getId() : null,
                entry.getWorkstation() != null ? entry.getWorkstation().getName() : null, entry.getTotalChecked(),
                entry.getPassQty(), entry.getAlterQty(), entry.getRejectQty(), entry.getStatus(),
                entry.isFromQcWorkstation());
    }
}
