package com.niic.erp.sampling;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** A raw-material, semi-finished, or operation line captured on a sample. */
@Entity
@Table(name = "sample_lines")
public class SampleLine extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false, length = 5)
    private SampleLineType lineType;

    @Column(name = "item_id")
    private Long itemId;

    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String colour;

    private BigDecimal qty;

    @Column(length = 30)
    private String unit;

    @Column(nullable = false)
    private int seq;

    protected SampleLine() {
    }

    public SampleLine(Sample sample, SampleLineType lineType, Long itemId, String name, String description,
                      String colour, BigDecimal qty, String unit, int seq) {
        this.sample = sample;
        this.lineType = lineType;
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.colour = colour;
        this.qty = qty;
        this.unit = unit;
        this.seq = seq;
    }

    public Sample getSample() {
        return sample;
    }

    public SampleLineType getLineType() {
        return lineType;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getColour() {
        return colour;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public String getUnit() {
        return unit;
    }

    public int getSeq() {
        return seq;
    }
}
