package com.niic.erp.attendance;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A gate pass lets an employee leave the premises during a shift. Requires
 * attendance on the date; the (monthly-limit)th+1 pass in a calendar month is
 * flagged with {@code penalty = true} (ported from legacy gatePassConfig, default
 * limit 3).
 */
@Entity
@Table(name = "gate_passes")
public class GatePass extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime outTime;

    private LocalTime inTime;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Column(nullable = false)
    private boolean penalty = false;

    @ManyToOne
    @JoinColumn(name = "issued_by_id")
    private User issuedBy;

    protected GatePass() {
    }

    public GatePass(Employee employee, LocalDate date, String purpose, User issuedBy) {
        this.employee = employee;
        this.date = date;
        this.purpose = purpose;
        this.issuedBy = issuedBy;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getOutTime() {
        return outTime;
    }

    public void setOutTime(LocalTime outTime) {
        this.outTime = outTime;
    }

    public LocalTime getInTime() {
        return inTime;
    }

    public void setInTime(LocalTime inTime) {
        this.inTime = inTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public boolean isPenalty() {
        return penalty;
    }

    public void setPenalty(boolean penalty) {
        this.penalty = penalty;
    }

    public User getIssuedBy() {
        return issuedBy;
    }
}
