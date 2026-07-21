package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A store item: raw material, semi-finished, or finished goods. New items raised
 * by store users start PENDING_APPROVAL and only count as usable stock once
 * approved (mirrors the legacy item-approval workflow). FG items carry colour/size
 * variants.
 */
@Entity
@Table(name = "store_items")
public class StoreItem extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String itemCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ItemType itemType;

    @Column(nullable = false)
    private String unit;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private StoreCategory category;

    @Column(nullable = false)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemApprovalStatus approvalStatus = ItemApprovalStatus.PENDING_APPROVAL;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreVariant> variants = new ArrayList<>();

    protected StoreItem() {
    }

    public StoreItem(String itemCode, String name, ItemType itemType, String unit, StoreCategory category,
                     BigDecimal reorderLevel) {
        this.itemCode = itemCode;
        this.name = name;
        this.itemType = itemType;
        this.unit = unit;
        this.category = category;
        this.reorderLevel = reorderLevel != null ? reorderLevel : BigDecimal.ZERO;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public StoreCategory getCategory() {
        return category;
    }

    public void setCategory(StoreCategory category) {
        this.category = category;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(BigDecimal reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public ItemApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ItemApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<StoreVariant> getVariants() {
        return variants;
    }
}
