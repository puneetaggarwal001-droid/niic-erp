package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "issuance_challan_items")
public class IssuanceChallanItem extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "challan_id", nullable = false)
    private IssuanceChallan challan;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private StoreItem item;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private StoreVariant variant;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal quantity;

    protected IssuanceChallanItem() {
    }

    public IssuanceChallanItem(IssuanceChallan challan, StoreItem item, StoreVariant variant, BigDecimal quantity) {
        this.challan = challan;
        this.item = item;
        this.variant = variant;
        this.quantity = quantity;
    }

    public IssuanceChallan getChallan() {
        return challan;
    }

    public StoreItem getItem() {
        return item;
    }

    public StoreVariant getVariant() {
        return variant;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
