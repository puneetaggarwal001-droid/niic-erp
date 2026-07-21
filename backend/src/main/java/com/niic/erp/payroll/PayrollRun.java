package com.niic.erp.payroll;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A monthly payroll run. Generated as a DRAFT (recomputable from attendance and
 * production), then FINALIZED — which freezes the numbers and marks the included
 * advances as deducted.
 */
@Entity
@Table(name = "payroll_runs")
public class PayrollRun extends BaseEntity {

    @Column(nullable = false, length = 7)
    private String periodMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollRunStatus status = PayrollRunStatus.DRAFT;

    private Instant finalizedAt;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollRunLine> lines = new ArrayList<>();

    protected PayrollRun() {
    }

    public PayrollRun(String periodMonth) {
        this.periodMonth = periodMonth;
    }

    public String getPeriodMonth() {
        return periodMonth;
    }

    public PayrollRunStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollRunStatus status) {
        this.status = status;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public List<PayrollRunLine> getLines() {
        return lines;
    }
}
