package com.niic.erp.production;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "qc_entries")
public class QcEntry extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "colour_id")
    private JobColour colour;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private JobSize size;

    @Enumerated(EnumType.STRING)
    private Side side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    // Operation label as free text, not a strict FK — see production package-info.
    private String opRef;

    @ManyToOne
    @JoinColumn(name = "workstation_id")
    private Workstation workstation;

    @Column(nullable = false)
    private int totalChecked;

    @Column(nullable = false)
    private int passQty;

    @Column(nullable = false)
    private int alterQty;

    @Column(nullable = false)
    private int rejectQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QcStatus status;

    @Column(nullable = false)
    private boolean fromQcWorkstation;

    @Column(length = 2000)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "entered_by_id")
    private User enteredBy;

    @Column(nullable = false)
    private Instant enteredAt;

    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    @Column(name = "updated_at_business")
    private Instant updatedAtBusiness;

    protected QcEntry() {
    }

    public QcEntry(LocalDate date, Job job, JobColour colour, JobSize size, Side side, Unit unit, String opRef,
                   Workstation workstation, int totalChecked, QcStatus status, boolean fromQcWorkstation,
                   User enteredBy) {
        this.date = date;
        this.job = job;
        this.colour = colour;
        this.size = size;
        this.side = side;
        this.unit = unit;
        this.opRef = opRef;
        this.workstation = workstation;
        this.totalChecked = totalChecked;
        this.status = status;
        this.fromQcWorkstation = fromQcWorkstation;
        this.enteredBy = enteredBy;
        this.enteredAt = Instant.now();
    }

    public LocalDate getDate() {
        return date;
    }

    public Job getJob() {
        return job;
    }

    public JobColour getColour() {
        return colour;
    }

    public JobSize getSize() {
        return size;
    }

    public Side getSide() {
        return side;
    }

    public Unit getUnit() {
        return unit;
    }

    public String getOpRef() {
        return opRef;
    }

    public Workstation getWorkstation() {
        return workstation;
    }

    public int getTotalChecked() {
        return totalChecked;
    }

    public void setTotalChecked(int totalChecked) {
        this.totalChecked = totalChecked;
    }

    public int getPassQty() {
        return passQty;
    }

    public void setPassQty(int passQty) {
        this.passQty = passQty;
    }

    public int getAlterQty() {
        return alterQty;
    }

    public void setAlterQty(int alterQty) {
        this.alterQty = alterQty;
    }

    public int getRejectQty() {
        return rejectQty;
    }

    public void setRejectQty(int rejectQty) {
        this.rejectQty = rejectQty;
    }

    public QcStatus getStatus() {
        return status;
    }

    public void setStatus(QcStatus status) {
        this.status = status;
    }

    public boolean isFromQcWorkstation() {
        return fromQcWorkstation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getEnteredBy() {
        return enteredBy;
    }

    public Instant getEnteredAt() {
        return enteredAt;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getUpdatedAtBusiness() {
        return updatedAtBusiness;
    }

    public void setUpdatedAtBusiness(Instant updatedAtBusiness) {
        this.updatedAtBusiness = updatedAtBusiness;
    }
}
