package com.niic.erp.production;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_sizes")
public class JobSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_colour_id", nullable = false)
    private JobColour jobColour;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private int plannedQty;

    // FK into the not-yet-built Store module's fg item variants; left unenforced for now.
    private Long variantId;

    @Column(nullable = false)
    private int seq;

    protected JobSize() {
    }

    public JobSize(JobColour jobColour, String size, int plannedQty, Long variantId, int seq) {
        this.jobColour = jobColour;
        this.size = size;
        this.plannedQty = plannedQty;
        this.variantId = variantId;
        this.seq = seq;
    }

    public Long getId() {
        return id;
    }

    public JobColour getJobColour() {
        return jobColour;
    }

    public String getSize() {
        return size;
    }

    public int getPlannedQty() {
        return plannedQty;
    }

    public Long getVariantId() {
        return variantId;
    }

    public int getSeq() {
        return seq;
    }
}
