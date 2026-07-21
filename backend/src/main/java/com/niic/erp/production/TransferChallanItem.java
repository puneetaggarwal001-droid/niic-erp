package com.niic.erp.production;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "transfer_challan_items")
public class TransferChallanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transfer_challan_id", nullable = false)
    private TransferChallan transferChallan;

    // FK into the not-yet-built Store module's item master; left unenforced for now.
    private Long itemId;

    private String itemCode;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String itemUnit;

    @Column(nullable = false)
    private BigDecimal qty;

    protected TransferChallanItem() {
    }

    public TransferChallanItem(TransferChallan transferChallan, Long itemId, String itemCode, String itemName,
                                String itemUnit, BigDecimal qty) {
        this.transferChallan = transferChallan;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.itemUnit = itemUnit;
        this.qty = qty;
    }

    public Long getId() {
        return id;
    }

    public TransferChallan getTransferChallan() {
        return transferChallan;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemUnit() {
        return itemUnit;
    }

    public BigDecimal getQty() {
        return qty;
    }
}
