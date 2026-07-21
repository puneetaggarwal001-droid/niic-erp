package com.niic.erp.sampling.dto;

import com.niic.erp.sampling.SamplePpmRow;

public record PpmRowDto(Long id, String section, String text1, String text2, String remark) {

    public static PpmRowDto from(SamplePpmRow r) {
        return new PpmRowDto(r.getId(), r.getSection().name(), r.getText1(), r.getText2(), r.getRemark());
    }
}
