package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bill of materials for one output item.
 *  - FG BOM: components (RM and/or SFG) needed to make ONE finished unit. May be
 *    variant-specific via colour/size; a null colour+size row is the generic default.
 *  - SFG BOM: RM needed to make a BATCH of {@code batchQty} SFG units (batch-based),
 *    so per-unit RM = component.qty / batchQty.
 */
@Entity
@Table(name = "store_boms")
public class Bom extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "output_item_id", nullable = false)
    private StoreItem outputItem;

    private String colour;

    private String size;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal batchQty = BigDecimal.ONE;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BomComponent> components = new ArrayList<>();

    protected Bom() {
    }

    public Bom(StoreItem outputItem, String colour, String size, BigDecimal batchQty) {
        this.outputItem = outputItem;
        this.colour = colour;
        this.size = size;
        this.batchQty = batchQty != null && batchQty.signum() > 0 ? batchQty : BigDecimal.ONE;
    }

    public StoreItem getOutputItem() {
        return outputItem;
    }

    public String getColour() {
        return colour;
    }

    public String getSize() {
        return size;
    }

    public BigDecimal getBatchQty() {
        return batchQty;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<BomComponent> getComponents() {
        return components;
    }
}
