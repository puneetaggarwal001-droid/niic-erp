package com.niic.erp.production;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "workstations")
public class Workstation extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String code;

    @Column(nullable = false)
    private boolean active = true;

    protected Workstation() {
    }

    public Workstation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
