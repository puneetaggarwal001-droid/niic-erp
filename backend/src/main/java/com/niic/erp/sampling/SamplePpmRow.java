package com.niic.erp.sampling;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A single PPM detail row. text1/text2 carry the section's two main columns
 * (for SOP: workstation / operation; for the others: a detail and its method),
 * with a free-form remark.
 */
@Entity
@Table(name = "sample_ppm_rows")
public class SamplePpmRow extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PpmSection section;

    @Column(nullable = false)
    private int seq;

    @Column(length = 500)
    private String text1;

    @Column(length = 500)
    private String text2;

    @Column(length = 1000)
    private String remark;

    protected SamplePpmRow() {
    }

    public SamplePpmRow(Sample sample, PpmSection section, int seq, String text1, String text2, String remark) {
        this.sample = sample;
        this.section = section;
        this.seq = seq;
        this.text1 = text1;
        this.text2 = text2;
        this.remark = remark;
    }

    public Sample getSample() {
        return sample;
    }

    public PpmSection getSection() {
        return section;
    }

    public int getSeq() {
        return seq;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public String getRemark() {
        return remark;
    }
}
