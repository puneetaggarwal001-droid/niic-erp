package com.niic.erp.sampling;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A base64 image attached to a sample section, kept inline (as in the legacy app). */
@Entity
@Table(name = "sample_photos")
public class SamplePhoto extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PhotoSection section;

    @Column(name = "data_url", nullable = false, columnDefinition = "text")
    private String dataUrl;

    @Column(length = 500)
    private String caption;

    @Column(nullable = false)
    private int seq;

    protected SamplePhoto() {
    }

    public SamplePhoto(Sample sample, PhotoSection section, String dataUrl, String caption, int seq) {
        this.sample = sample;
        this.section = section;
        this.dataUrl = dataUrl;
        this.caption = caption;
        this.seq = seq;
    }

    public Sample getSample() {
        return sample;
    }

    public PhotoSection getSection() {
        return section;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getSeq() {
        return seq;
    }
}
