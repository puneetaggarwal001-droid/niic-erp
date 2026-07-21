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
@Table(name = "qc_rework")
public class QcRework extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "qc_entry_id", nullable = false)
    private QcEntry qcEntry;

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

    @Column(nullable = false)
    private int alterQty;

    @Column(nullable = false)
    private int reworkDone = 0;

    @Column(nullable = false)
    private int reworkReject = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReworkStatus status = ReworkStatus.PENDING;

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

    protected QcRework() {
    }

    public QcRework(QcEntry qcEntry, LocalDate date, Job job, JobColour colour, JobSize size, Side side,
                     int alterQty, User enteredBy) {
        this.qcEntry = qcEntry;
        this.date = date;
        this.job = job;
        this.colour = colour;
        this.size = size;
        this.side = side;
        this.alterQty = alterQty;
        this.enteredBy = enteredBy;
        this.enteredAt = Instant.now();
    }

    public QcEntry getQcEntry() {
        return qcEntry;
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

    public int getAlterQty() {
        return alterQty;
    }

    public int getReworkDone() {
        return reworkDone;
    }

    public void setReworkDone(int reworkDone) {
        this.reworkDone = reworkDone;
    }

    public int getReworkReject() {
        return reworkReject;
    }

    public void setReworkReject(int reworkReject) {
        this.reworkReject = reworkReject;
    }

    public ReworkStatus getStatus() {
        return status;
    }

    public void setStatus(ReworkStatus status) {
        this.status = status;
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
