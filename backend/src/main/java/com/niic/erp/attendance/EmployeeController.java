package com.niic.erp.attendance;

import com.niic.erp.attendance.dto.EmployeeDto;
import com.niic.erp.attendance.dto.EmployeeRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeDto> list() {
        return employeeService.findActive().stream().map(EmployeeDto::from).toList();
    }

    @GetMapping("/{id}")
    public EmployeeDto get(@PathVariable Long id) {
        return EmployeeDto.from(employeeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('manage_employees')")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto create(@Valid @RequestBody EmployeeRequest request) {
        return EmployeeDto.from(employeeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('manage_employees')")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return EmployeeDto.from(employeeService.update(id, request));
    }

    // Soft delete only — matches the legacy app, which never hard-deletes an employee record.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('manage_employees')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        employeeService.deactivate(id);
    }
}
