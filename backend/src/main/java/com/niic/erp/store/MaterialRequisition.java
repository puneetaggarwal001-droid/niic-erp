package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.production.Workstation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** A workstation→store material request (MR-####), fulfilled partially or fully. */
@Entity
@Table(name = "material_requisitions")
public class MaterialRequisition extends BaseEntity {

    @Column(unique = true, length = 20)
    private String mrNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_workstation_id", nullable = false)
    private Workstation fromWorkstation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private RequisitionStatus status = RequisitionStatus.PENDING;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialRequisitionItem> items = new ArrayList<>();

    protected MaterialRequisition() {
    }

    public MaterialRequisition(Workstation fromWorkstation, String notes) {
        this.fromWorkstation = fromWorkstation;
        this.notes = notes;
    }

    public String getMrNumber() {
        return mrNumber;
    }

    public void setMrNumber(String mrNumber) {
        this.mrNumber = mrNumber;
    }

    public Workstation getFromWorkstation() {
        return fromWorkstation;
    }

    public RequisitionStatus getStatus() {
        return status;
    }

    public void setStatus(RequisitionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public List<MaterialRequisitionItem> getItems() {
        return items;
    }
}
