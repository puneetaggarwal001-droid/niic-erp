package com.niic.erp.production;

import com.niic.erp.attendance.Employee;
import com.niic.erp.common.BaseEntity;
import com.niic.erp.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_entries")
public class ProductionEntry extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

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

    @ManyToOne
    @JoinColumn(name = "entered_by_id")
    private User enteredBy;

    @Column(nullable = false)
    private Instant enteredAt;

    @ManyToOne
    @JoinColumn(name = "last_edited_by_id")
    private User lastEditedBy;

    private Instant lastEditedAt;

    @Column(nullable = false)
    private boolean isApprovedEdit = false;

    @ManyToOne
    @JoinColumn(name = "approved_edit_request_id")
    private ProductionEditRequest approvedEditRequest;

    @OneToMany(mappedBy = "productionEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionEntryOp> operations = new ArrayList<>();

    protected ProductionEntry() {
    }

    public ProductionEntry(LocalDate date, Employee employee, Job job, JobColour colour, JobSize size, Side side,
                            Unit unit, User enteredBy) {
        this.date = date;
        this.employee = employee;
        this.job = job;
        this.colour = colour;
        this.size = size;
        this.side = side;
        this.unit = unit;
        this.enteredBy = enteredBy;
        this.enteredAt = Instant.now();
    }

    public LocalDate getDate() {
        return date;
    }

    public Employee getEmployee() {
        return employee;
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

    public User getEnteredBy() {
        return enteredBy;
    }

    public Instant getEnteredAt() {
        return enteredAt;
    }

    public User getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(User lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public Instant getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(Instant lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public boolean isApprovedEdit() {
        return isApprovedEdit;
    }

    public void setApprovedEdit(boolean approvedEdit) {
        isApprovedEdit = approvedEdit;
    }

    public ProductionEditRequest getApprovedEditRequest() {
        return approvedEditRequest;
    }

    public void setApprovedEditRequest(ProductionEditRequest approvedEditRequest) {
        this.approvedEditRequest = approvedEditRequest;
    }

    public List<ProductionEntryOp> getOperations() {
        return operations;
    }
}
