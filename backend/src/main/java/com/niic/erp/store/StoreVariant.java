package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A colour/size variant of an FG item. */
@Entity
@Table(name = "store_variants")
public class StoreVariant extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private StoreItem item;

    @Column(nullable = false)
    private String colour;

    @Column(nullable = false)
    private String size;

    protected StoreVariant() {
    }

    public StoreVariant(StoreItem item, String colour, String size) {
        this.item = item;
        this.colour = colour;
        this.size = size;
    }

    public StoreItem getItem() {
        return item;
    }

    public String getColour() {
        return colour;
    }

    public String getSize() {
        return size;
    }
}
