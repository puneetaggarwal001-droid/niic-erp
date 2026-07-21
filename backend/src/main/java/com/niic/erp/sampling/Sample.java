package com.niic.erp.sampling;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A footwear sample. Samples move through the lifecycle DRAFT -> IN_REVIEW ->
 * SELECTED -> PPS_DONE -> PPM_DONE, or terminate at REJECTED / CLOSED. Revisions
 * share a {@code revBase} (the original sample number) and are numbered
 * {@code <base>-R<n>}; selecting one closes the rest of its chain.
 */
@Entity
@Table(name = "samples")
public class Sample extends BaseEntity {

    @Column(name = "sample_no", nullable = false, unique = true, length = 30)
    private String sampleNo;

    @Column(name = "rev_base", nullable = false, length = 20)
    private String revBase;

    @Column(name = "sample_date")
    private LocalDate sampleDate;

    @Column(nullable = false)
    private String name;

    private String style;
    private String category;
    private String designer;

    @Column(name = "reference")
    private String reference;

    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SampleStatus status = SampleStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_remark", length = 20)
    private ClosedRemark closedRemark;

    @Column(name = "closed_at")
    private Instant closedAt;

    // ---- PPS (pre-production sample) spec --------------------------------
    @Column(name = "pps_fabric_details", length = 2000)
    private String ppsFabricDetails;

    @Column(name = "pps_design_count")
    private Integer ppsDesignCount;

    @Column(name = "pps_special_instructions", length = 2000)
    private String ppsSpecialInstructions;

    @Column(name = "pps_saved_at")
    private Instant ppsSavedAt;

    @Column(name = "pps_approved_at")
    private Instant ppsApprovedAt;

    @Column(name = "pps_approved_by", length = 100)
    private String ppsApprovedBy;

    @Column(name = "ppm_saved_at")
    private Instant ppmSavedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq asc")
    private List<SampleLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq asc")
    private List<SamplePpsOption> ppsOptions = new ArrayList<>();

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq asc")
    private List<SamplePpmRow> ppmRows = new ArrayList<>();

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq asc")
    private List<SamplePhoto> photos = new ArrayList<>();

    protected Sample() {
    }

    public Sample(String name, LocalDate sampleDate, String createdBy) {
        this.name = name;
        this.sampleDate = sampleDate;
        this.createdBy = createdBy;
    }

    public String getSampleNo() {
        return sampleNo;
    }

    public void setSampleNo(String sampleNo) {
        this.sampleNo = sampleNo;
    }

    public String getRevBase() {
        return revBase;
    }

    public void setRevBase(String revBase) {
        this.revBase = revBase;
    }

    public LocalDate getSampleDate() {
        return sampleDate;
    }

    public void setSampleDate(LocalDate sampleDate) {
        this.sampleDate = sampleDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDesigner() {
        return designer;
    }

    public void setDesigner(String designer) {
        this.designer = designer;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public SampleStatus getStatus() {
        return status;
    }

    public void setStatus(SampleStatus status) {
        this.status = status;
    }

    public ClosedRemark getClosedRemark() {
        return closedRemark;
    }

    public void setClosedRemark(ClosedRemark closedRemark) {
        this.closedRemark = closedRemark;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public String getPpsFabricDetails() {
        return ppsFabricDetails;
    }

    public void setPpsFabricDetails(String ppsFabricDetails) {
        this.ppsFabricDetails = ppsFabricDetails;
    }

    public Integer getPpsDesignCount() {
        return ppsDesignCount;
    }

    public void setPpsDesignCount(Integer ppsDesignCount) {
        this.ppsDesignCount = ppsDesignCount;
    }

    public String getPpsSpecialInstructions() {
        return ppsSpecialInstructions;
    }

    public void setPpsSpecialInstructions(String ppsSpecialInstructions) {
        this.ppsSpecialInstructions = ppsSpecialInstructions;
    }

    public Instant getPpsSavedAt() {
        return ppsSavedAt;
    }

    public void setPpsSavedAt(Instant ppsSavedAt) {
        this.ppsSavedAt = ppsSavedAt;
    }

    public Instant getPpsApprovedAt() {
        return ppsApprovedAt;
    }

    public void setPpsApprovedAt(Instant ppsApprovedAt) {
        this.ppsApprovedAt = ppsApprovedAt;
    }

    public String getPpsApprovedBy() {
        return ppsApprovedBy;
    }

    public void setPpsApprovedBy(String ppsApprovedBy) {
        this.ppsApprovedBy = ppsApprovedBy;
    }

    public Instant getPpmSavedAt() {
        return ppmSavedAt;
    }

    public void setPpmSavedAt(Instant ppmSavedAt) {
        this.ppmSavedAt = ppmSavedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<SampleLine> getLines() {
        return lines;
    }

    public List<SamplePpsOption> getPpsOptions() {
        return ppsOptions;
    }

    public List<SamplePpmRow> getPpmRows() {
        return ppmRows;
    }

    public List<SamplePhoto> getPhotos() {
        return photos;
    }
}
