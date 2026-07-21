package com.niic.erp.attendance;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "designations")
public class Designation extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    protected Designation() {
    }

    public Designation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
