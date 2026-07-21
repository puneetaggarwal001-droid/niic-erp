package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One stock movement. {@code quantity} is the SIGNED delta already applied to
 * on-hand (INWARD/RETURN positive, ISSUE/REJECT negative, ADJUST either), so
 * current stock is simply the sum of quantities for an item/variant.
 */
@Entity
@Table(name = "stock_transactions")
public class StockTransaction extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private StoreItem item;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private StoreVariant variant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockTxnType txnType;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false)
    private LocalDate txnDate;

    private String reference;

    @Column(length = 1000)
    private String note;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    protected StockTransaction() {
    }

    public StockTransaction(StoreItem item, StoreVariant variant, StockTxnType txnType, BigDecimal quantity,
                            LocalDate txnDate, String reference, String note, User createdBy) {
        this.item = item;
        this.variant = variant;
        this.txnType = txnType;
        this.quantity = quantity;
        this.txnDate = txnDate;
        this.reference = reference;
        this.note = note;
        this.createdBy = createdBy;
    }

    public StoreItem getItem() {
        return item;
    }

    public StoreVariant getVariant() {
        return variant;
    }

    public StockTxnType getTxnType() {
        return txnType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public LocalDate getTxnDate() {
        return txnDate;
    }

    public String getReference() {
        return reference;
    }

    public String getNote() {
        return note;
    }

    public User getCreatedBy() {
        return createdBy;
    }
}
