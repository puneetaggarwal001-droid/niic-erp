package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private StoreItem item;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal orderedQty;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal receivedQty = BigDecimal.ZERO;

    private BigDecimal rate;

    protected PurchaseOrderItem() {
    }

    public PurchaseOrderItem(PurchaseOrder purchaseOrder, StoreItem item, BigDecimal orderedQty, BigDecimal rate) {
        this.purchaseOrder = purchaseOrder;
        this.item = item;
        this.orderedQty = orderedQty;
        this.rate = rate;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public StoreItem getItem() {
        return item;
    }

    public BigDecimal getOrderedQty() {
        return orderedQty;
    }

    public BigDecimal getReceivedQty() {
        return receivedQty;
    }

    public void setReceivedQty(BigDecimal receivedQty) {
        this.receivedQty = receivedQty;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal outstanding() {
        return orderedQty.subtract(receivedQty);
    }
}
