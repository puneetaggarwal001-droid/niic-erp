package com.niic.erp.sampling;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A PPS colourway or size option. */
@Entity
@Table(name = "sample_pps_options")
public class SamplePpsOption extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @Enumerated(EnumType.STRING)
    @Column(name = "opt_type", nullable = false, length = 10)
    private PpsOptionType optType;

    @Column(name = "option_value", nullable = false, length = 100)
    private String value;

    @Column(nullable = false)
    private int seq;

    protected SamplePpsOption() {
    }

    public SamplePpsOption(Sample sample, PpsOptionType optType, String value, int seq) {
        this.sample = sample;
        this.optType = optType;
        this.value = value;
        this.seq = seq;
    }

    public Sample getSample() {
        return sample;
    }

    public PpsOptionType getOptType() {
        return optType;
    }

    public String getValue() {
        return value;
    }

    public int getSeq() {
        return seq;
    }
}
