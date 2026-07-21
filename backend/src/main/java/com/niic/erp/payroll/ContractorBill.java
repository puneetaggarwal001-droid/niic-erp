package com.niic.erp.payroll;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A monthly bill for a contractor. Amount = quantity x rate (interpretation of
 * quantity depends on {@link ContractorRateType}); netPayable = max(0, amount -
 * advancesDeducted). DRAFT until finalized.
 */
@Entity
@Table(name = "contractor_bills")
public class ContractorBill extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;

    @Column(nullable = false, length = 7)
    private String periodMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractorRateType rateType;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal advancesDeducted;

    @Column(nullable = false)
    private BigDecimal netPayable;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollRunStatus status = PayrollRunStatus.DRAFT;

    private Instant finalizedAt;

    protected ContractorBill() {
    }

    public ContractorBill(Contractor contractor, String periodMonth, ContractorRateType rateType,
                          BigDecimal quantity, BigDecimal rate, BigDecimal advancesDeducted, String notes) {
        this.contractor = contractor;
        this.periodMonth = periodMonth;
        this.rateType = rateType;
        this.quantity = quantity;
        this.rate = rate;
        this.advancesDeducted = advancesDeducted;
        this.notes = notes;
        recompute();
    }

    /** amount = quantity x rate; netPayable = max(0, amount - advances). */
    public final void recompute() {
        this.amount = quantity.multiply(rate);
        BigDecimal net = amount.subtract(advancesDeducted);
        this.netPayable = net.signum() < 0 ? BigDecimal.ZERO : net;
    }

    public Contractor getContractor() {
        return contractor;
    }

    public String getPeriodMonth() {
        return periodMonth;
    }

    public ContractorRateType getRateType() {
        return rateType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getAdvancesDeducted() {
        return advancesDeducted;
    }

    public BigDecimal getNetPayable() {
        return netPayable;
    }

    public String getNotes() {
        return notes;
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
}
