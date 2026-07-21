package com.niic.erp.sampling.dto;

import com.niic.erp.sampling.SamplePhoto;

public record PhotoDto(Long id, String section, String dataUrl, String caption) {

    public static PhotoDto from(SamplePhoto p) {
        return new PhotoDto(p.getId(), p.getSection().name(), p.getDataUrl(), p.getCaption());
    }
}
