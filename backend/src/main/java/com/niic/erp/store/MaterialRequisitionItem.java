package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "material_requisition_items")
public class MaterialRequisitionItem extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "requisition_id", nullable = false)
    private MaterialRequisition requisition;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private StoreItem item;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal requestedQty;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal fulfilledQty = BigDecimal.ZERO;

    protected MaterialRequisitionItem() {
    }

    public MaterialRequisitionItem(MaterialRequisition requisition, StoreItem item, BigDecimal requestedQty) {
        this.requisition = requisition;
        this.item = item;
        this.requestedQty = requestedQty;
    }

    public MaterialRequisition getRequisition() {
        return requisition;
    }

    public StoreItem getItem() {
        return item;
    }

    public BigDecimal getRequestedQty() {
        return requestedQty;
    }

    public BigDecimal getFulfilledQty() {
        return fulfilledQty;
    }

    public void setFulfilledQty(BigDecimal fulfilledQty) {
        this.fulfilledQty = fulfilledQty;
    }

    public BigDecimal outstanding() {
        return requestedQty.subtract(fulfilledQty);
    }
}
