package com.niic.erp.production;

import com.niic.erp.attendance.Employee;
import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "operation_closures")
public class OperationClosure extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "colour_id")
    private JobColour colour;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private JobSize size;

    @ManyToOne
    @JoinColumn(name = "workstation_id", nullable = false)
    private Workstation workstation;

    @ManyToOne
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    @Column(nullable = false)
    private int doneQtyAtClosure;

    @Column(nullable = false)
    private int plannedQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClosureReason reason;

    private Integer reworkQty;

    private Integer rejectionQty;

    @Column(length = 2000)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "closed_by_id", nullable = false)
    private Employee closedBy;

    @Column(nullable = false)
    private LocalDate date;

    protected OperationClosure() {
    }

    public OperationClosure(Job job, JobColour colour, JobSize size, Workstation workstation, Operation operation,
                             int doneQtyAtClosure, int plannedQty, ClosureReason reason, Integer reworkQty,
                             Integer rejectionQty, String notes, Employee closedBy, LocalDate date) {
        this.job = job;
        this.colour = colour;
        this.size = size;
        this.workstation = workstation;
        this.operation = operation;
        this.doneQtyAtClosure = doneQtyAtClosure;
        this.plannedQty = plannedQty;
        this.reason = reason;
        this.reworkQty = reworkQty;
        this.rejectionQty = rejectionQty;
        this.notes = notes;
        this.closedBy = closedBy;
        this.date = date;
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

    public Workstation getWorkstation() {
        return workstation;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getDoneQtyAtClosure() {
        return doneQtyAtClosure;
    }

    public int getPlannedQty() {
        return plannedQty;
    }

    public ClosureReason getReason() {
        return reason;
    }

    public Integer getReworkQty() {
        return reworkQty;
    }

    public Integer getRejectionQty() {
        return rejectionQty;
    }

    public String getNotes() {
        return notes;
    }

    public Employee getClosedBy() {
        return closedBy;
    }

    public LocalDate getDate() {
        return date;
    }
}
