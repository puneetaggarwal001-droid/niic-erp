package com.niic.erp.production.dto;

import com.niic.erp.production.ChallanStatus;
import com.niic.erp.production.TransferChallan;
import java.util.List;

public record TransferChallanDto(Long id, String challanNo, Long jobId, String jobDisplayId, Long fromWorkstationId,
                                  String fromWorkstationName, Long toWorkstationId, String toWorkstationName,
                                  String remarks, ChallanStatus status, String createdByUsername,
                                  List<TransferChallanItemDto> items) {
    public static TransferChallanDto from(TransferChallan challan) {
        return new TransferChallanDto(challan.getId(), challan.getChallanNo(), challan.getJob().getId(),
                challan.getJob().getJobDisplayId(), challan.getFromWorkstation().getId(),
                challan.getFromWorkstation().getName(), challan.getToWorkstation().getId(),
                challan.getToWorkstation().getName(), challan.getRemarks(), challan.getStatus(),
                challan.getCreatedBy().getUsername(), challan.getItems().stream().map(TransferChallanItemDto::from).toList());
    }
}
