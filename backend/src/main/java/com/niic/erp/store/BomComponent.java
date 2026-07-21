package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** A single input line of a {@link Bom} — quantity of a component item per batch. */
@Entity
@Table(name = "store_bom_components")
public class BomComponent extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "bom_id", nullable = false)
    private Bom bom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "component_item_id", nullable = false)
    private StoreItem componentItem;

    @Column(nullable = false, precision = 16, scale = 4)
    private BigDecimal quantity;

    protected BomComponent() {
    }

    public BomComponent(Bom bom, StoreItem componentItem, BigDecimal quantity) {
        this.bom = bom;
        this.componentItem = componentItem;
        this.quantity = quantity;
    }

    public Bom getBom() {
        return bom;
    }

    public StoreItem getComponentItem() {
        return componentItem;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
