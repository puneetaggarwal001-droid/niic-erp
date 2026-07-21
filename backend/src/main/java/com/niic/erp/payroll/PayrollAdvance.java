package com.niic.erp.payroll;

import com.niic.erp.attendance.Employee;
import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * An advance or loan paid to an employee against a given payroll month
 * ({@code periodMonth} is "YYYY-MM"). It stays PENDING (deducted=false) until a
 * payroll run for that month is finalized, at which point it is deducted from net pay.
 */
@Entity
@Table(name = "payroll_advances")
public class PayrollAdvance extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 7)
    private String periodMonth;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private boolean deducted = false;

    protected PayrollAdvance() {
    }

    public PayrollAdvance(Employee employee, String periodMonth, BigDecimal amount, String reason) {
        this.employee = employee;
        this.periodMonth = periodMonth;
        this.amount = amount;
        this.reason = reason;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getPeriodMonth() {
        return periodMonth;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public boolean isDeducted() {
        return deducted;
    }

    public void setDeducted(boolean deducted) {
        this.deducted = deducted;
    }
}
