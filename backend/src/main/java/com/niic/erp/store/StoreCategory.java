package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Two-level category tree: a top-level category has {@code parent == null}; a
 * sub-category points at its parent. {@code code} feeds SFG item-code generation.
 */
@Entity
@Table(name = "store_categories")
public class StoreCategory extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private StoreCategory parent;

    protected StoreCategory() {
    }

    public StoreCategory(String name, String code, StoreCategory parent) {
        this.name = name;
        this.code = code;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public StoreCategory getParent() {
        return parent;
    }

    public void setParent(StoreCategory parent) {
        this.parent = parent;
    }
}
