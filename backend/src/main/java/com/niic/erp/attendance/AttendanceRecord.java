package com.niic.erp.attendance;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One row per employee per day. designation is captured at entry time (not looked
 * up live from Employee) so a later designation change doesn't rewrite history.
 */
@Entity
@Table(name = "attendance_records", uniqueConstraints = @UniqueConstraint(columnNames = {"date", "employee_id"}))
public class AttendanceRecord extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Designation designation;

    private LocalTime entryTime;

    private LocalTime exitTime;

    @ManyToOne
    private User enteredBy;

    @Column(nullable = false)
    private Instant enteredAt;

    @ManyToOne
    private User lastEditedBy;

    private Instant lastEditedAt;

    protected AttendanceRecord() {
    }

    public AttendanceRecord(LocalDate date, Employee employee, Designation designation, User enteredBy) {
        this.date = date;
        this.employee = employee;
        this.designation = designation;
        this.enteredBy = enteredBy;
        this.enteredAt = Instant.now();
    }

    public LocalDate getDate() {
        return date;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Designation getDesignation() {
        return designation;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalTime exitTime) {
        this.exitTime = exitTime;
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

    public Instant getLastEditedAt() {
        return lastEditedAt;
    }

    public void markEdited(User editor) {
        this.lastEditedBy = editor;
        this.lastEditedAt = Instant.now();
    }
}
