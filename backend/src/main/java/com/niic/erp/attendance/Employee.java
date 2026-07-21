package com.niic.erp.attendance;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

    // Human-facing code, e.g. "EMP-001" — see EmployeeIdAllocator.
    @Column(nullable = false, unique = true)
    private String empId;

    @Column(nullable = false)
    private String name;

    private String address;

    // TODO(phase 2): Aadhaar is sensitive PII — encrypt at rest / mask in API responses
    // before this goes anywhere near production data.
    @Column(nullable = false)
    private String aadhar;

    @Column(nullable = false)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "designation_id", nullable = false)
    private Designation designation;

    @Column(nullable = false)
    private LocalDate dateOfJoining;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalaryType salaryType;

    private BigDecimal salary;

    private BigDecimal pcRate;

    private String contractorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeType empType = EmployeeType.REGULAR;

    private LocalDate validTill;

    private String department;

    @Column(length = 2000)
    private String notes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_authorized_workstations", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "workstation_code")
    private Set<String> authorizedWorkstations = new HashSet<>();

    // Path/URL to the stored photo, not the binary itself.
    private String photoUrl;

    @Column(nullable = false)
    private boolean active = true;

    protected Employee() {
    }

    public Employee(String empId, String name, String aadhar, String phone, Designation designation,
                     LocalDate dateOfJoining, SalaryType salaryType) {
        this.empId = empId;
        this.name = name;
        this.aadhar = aadhar;
        this.phone = phone;
        this.designation = designation;
        this.dateOfJoining = dateOfJoining;
        this.salaryType = salaryType;
    }

    public String getEmpId() {
        return empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        this.aadhar = aadhar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BigDecimal getPcRate() {
        return pcRate;
    }

    public void setPcRate(BigDecimal pcRate) {
        this.pcRate = pcRate;
    }

    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
    }

    public EmployeeType getEmpType() {
        return empType;
    }

    public void setEmpType(EmployeeType empType) {
        this.empType = empType;
    }

    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Set<String> getAuthorizedWorkstations() {
        return authorizedWorkstations;
    }

    public void setAuthorizedWorkstations(Set<String> authorizedWorkstations) {
        this.authorizedWorkstations = authorizedWorkstations;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
