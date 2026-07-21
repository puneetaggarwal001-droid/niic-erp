package com.niic.erp.production;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "operations")
public class Operation extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "workstation_id", nullable = false)
    private Workstation workstation;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    protected Operation() {
    }

    public Operation(Workstation workstation, String name) {
        this.workstation = workstation;
        this.name = name;
    }

    public Workstation getWorkstation() {
        return workstation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
