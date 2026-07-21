package com.niic.erp.payroll.dto;

import com.niic.erp.payroll.Contractor;

public record ContractorDto(Long id, String name, String phone, boolean active) {

    public static ContractorDto from(Contractor c) {
        return new ContractorDto(c.getId(), c.getName(), c.getPhone(), c.isActive());
    }
}
