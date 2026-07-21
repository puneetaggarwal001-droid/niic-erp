package com.niic.erp.store;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.production.Job;
import com.niic.erp.production.Workstation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** A store→workstation issue of stock (ISS-####). Posts ISSUE stock movements. */
@Entity
@Table(name = "issuance_challans")
public class IssuanceChallan extends BaseEntity {

    @Column(unique = true, length = 20)
    private String issNumber;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_workstation_id", nullable = false)
    private Workstation toWorkstation;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "challan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IssuanceChallanItem> items = new ArrayList<>();

    protected IssuanceChallan() {
    }

    public IssuanceChallan(Job job, Workstation toWorkstation, String notes) {
        this.job = job;
        this.toWorkstation = toWorkstation;
        this.notes = notes;
    }

    public String getIssNumber() {
        return issNumber;
    }

    public void setIssNumber(String issNumber) {
        this.issNumber = issNumber;
    }

    public Job getJob() {
        return job;
    }

    public Workstation getToWorkstation() {
        return toWorkstation;
    }

    public String getNotes() {
        return notes;
    }

    public List<IssuanceChallanItem> getItems() {
        return items;
    }
}
