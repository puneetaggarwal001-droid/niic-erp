package com.niic.erp.attendance;

import com.niic.erp.attendance.dto.EmployeeRequest;
import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final EmployeeIdAllocator idAllocator;

    public EmployeeService(EmployeeRepository employeeRepository, DesignationRepository designationRepository,
                            EmployeeIdAllocator idAllocator) {
        this.employeeRepository = employeeRepository;
        this.designationRepository = designationRepository;
        this.idAllocator = idAllocator;
    }

    public List<Employee> findActive() {
        return employeeRepository.findByActiveTrue();
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee " + id + " not found."));
    }

    @Transactional
    public Employee create(EmployeeRequest request) {
        validate(request, null);
        Designation designation = requireDesignation(request.designationId());
        Employee employee = new Employee(
                idAllocator.nextEmpId(), request.name(), request.aadhar(), request.phone(),
                designation, request.dateOfJoining(), request.salaryType());
        applyOptionalFields(employee, request);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, EmployeeRequest request) {
        Employee employee = findById(id);
        validate(request, id);
        employee.setName(request.name());
        employee.setAadhar(request.aadhar());
        employee.setPhone(request.phone());
        employee.setDesignation(requireDesignation(request.designationId()));
        employee.setDateOfJoining(request.dateOfJoining());
        employee.setSalaryType(request.salaryType());
        applyOptionalFields(employee, request);
        return employee;
    }

    @Transactional
    public void deactivate(Long id) {
        Employee employee = findById(id);
        employee.setActive(false);
    }

    private void applyOptionalFields(Employee employee, EmployeeRequest request) {
        employee.setAddress(request.address());
        employee.setSalary(request.salary());
        employee.setPcRate(request.pcRate());
        employee.setContractorName(request.contractorName());
        employee.setEmpType(request.empType() != null ? request.empType() : EmployeeType.REGULAR);
        employee.setValidTill(request.validTill());
        employee.setDepartment(request.department());
        employee.setNotes(request.notes());
        employee.setAuthorizedWorkstations(request.authorizedWorkstations() != null
                ? request.authorizedWorkstations() : Set.of());
        employee.setPhotoUrl(request.photoUrl());
    }

    private Designation requireDesignation(Long designationId) {
        return designationRepository.findById(designationId)
                .orElseThrow(() -> new BadRequestException("Unknown designation id " + designationId));
    }

    private void validate(EmployeeRequest request, Long excludeId) {
        if (request.salaryType() == SalaryType.SALARIED
                && (request.salary() == null || request.salary().signum() <= 0)) {
            throw new BadRequestException("Monthly salary is required for salaried employees.");
        }
        if (request.salaryType() == SalaryType.CONTRACTOR
                && (request.contractorName() == null || request.contractorName().isBlank())) {
            throw new BadRequestException("Contractor name is required for contractor employees.");
        }
        if (request.empType() == EmployeeType.VISITOR && request.validTill() == null) {
            throw new BadRequestException("Valid Till date is required for Visitor/Trainee employees.");
        }
        // Sentinel that never matches a real id, so create() and update() share one check.
        long idToExclude = excludeId != null ? excludeId : -1L;
        if (employeeRepository.existsByAadharAndActiveTrueAndIdNot(request.aadhar(), idToExclude)) {
            throw new BadRequestException("Another active employee already has this Aadhaar number.");
        }
        if (employeeRepository.existsByPhoneAndActiveTrueAndIdNot(request.phone(), idToExclude)) {
            throw new BadRequestException("Another active employee already has this phone number.");
        }
    }
}
