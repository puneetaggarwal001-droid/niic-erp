package com.niic.erp.payroll;

import com.niic.erp.attendance.Employee;
import com.niic.erp.attendance.SalaryType;
import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * One employee's computed pay within a run. Salary/pcRate are snapshotted so a
 * later master-data change doesn't rewrite a historical run.
 */
@Entity
@Table(name = "payroll_run_lines")
public class PayrollRunLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private PayrollRun run;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SalaryType salaryType;

    @Column(nullable = false)
    private int presentDays;

    @Column(nullable = false)
    private int overtimeMinutes;

    @Column(nullable = false)
    private int totalPieces;

    private BigDecimal monthlySalary;

    private BigDecimal pcRate;

    @Column(nullable = false)
    private BigDecimal grossPay;

    @Column(nullable = false)
    private BigDecimal advancesDeducted;

    @Column(nullable = false)
    private BigDecimal netPay;

    protected PayrollRunLine() {
    }

    public PayrollRunLine(PayrollRun run, Employee employee, SalaryType salaryType) {
        this.run = run;
        this.employee = employee;
        this.salaryType = salaryType;
    }

    public PayrollRun getRun() {
        return run;
    }

    public Employee getEmployee() {
        return employee;
    }

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public int getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(int overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    public int getTotalPieces() {
        return totalPieces;
    }

    public void setTotalPieces(int totalPieces) {
        this.totalPieces = totalPieces;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(BigDecimal monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public BigDecimal getPcRate() {
        return pcRate;
    }

    public void setPcRate(BigDecimal pcRate) {
        this.pcRate = pcRate;
    }

    public BigDecimal getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(BigDecimal grossPay) {
        this.grossPay = grossPay;
    }

    public BigDecimal getAdvancesDeducted() {
        return advancesDeducted;
    }

    public void setAdvancesDeducted(BigDecimal advancesDeducted) {
        this.advancesDeducted = advancesDeducted;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }
}
