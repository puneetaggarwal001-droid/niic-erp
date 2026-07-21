package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.production.Job;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * A reservation of an item's stock against a job. Outstanding reservation =
 * bookedQty - issuedQty; available stock = on-hand minus outstanding reservations.
 * Closed (or fully issued) bookings no longer reserve anything.
 */
@Entity
@Table(name = "stock_bookings")
public class StockBooking extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private StoreItem item;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private StoreVariant variant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal bookedQty;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal issuedQty = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BookingStatus status = BookingStatus.OPEN;

    protected StockBooking() {
    }

    public StockBooking(StoreItem item, StoreVariant variant, Job job, BigDecimal bookedQty) {
        this.item = item;
        this.variant = variant;
        this.job = job;
        this.bookedQty = bookedQty;
    }

    public StoreItem getItem() {
        return item;
    }

    public StoreVariant getVariant() {
        return variant;
    }

    public Job getJob() {
        return job;
    }

    public BigDecimal getBookedQty() {
        return bookedQty;
    }

    public BigDecimal getIssuedQty() {
        return issuedQty;
    }

    public void setIssuedQty(BigDecimal issuedQty) {
        this.issuedQty = issuedQty;
    }

    public BigDecimal outstanding() {
        return bookedQty.subtract(issuedQty);
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
