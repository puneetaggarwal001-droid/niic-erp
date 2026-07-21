package com.niic.erp.production;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pc_rates")
public class PcRate extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "workstation_id", nullable = false)
    private Workstation workstation;

    @ManyToOne
    @JoinColumn(name = "style", referencedColumnName = "code", nullable = false)
    private Style style;

    @Column(nullable = false)
    private String modelNo;

    @ManyToOne
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    protected PcRate() {
    }

    public PcRate(Workstation workstation, Style style, String modelNo, Operation operation, BigDecimal rate,
                  LocalDate effectiveDate, User createdBy) {
        this.workstation = workstation;
        this.style = style;
        this.modelNo = modelNo;
        this.operation = operation;
        this.rate = rate;
        this.effectiveDate = effectiveDate;
        this.createdBy = createdBy;
    }

    public Workstation getWorkstation() {
        return workstation;
    }

    public Style getStyle() {
        return style;
    }

    public String getModelNo() {
        return modelNo;
    }

    public Operation getOperation() {
        return operation;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getCreatedBy() {
        return createdBy;
    }
}
