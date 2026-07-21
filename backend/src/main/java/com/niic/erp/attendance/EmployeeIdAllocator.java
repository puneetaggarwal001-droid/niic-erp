package com.niic.erp.attendance;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Reproduces the legacy app's "EMP-001" numbering: next number is
 * max(existing empId suffixes) + 1. Synchronized because two admins could
 * otherwise race for the same number between the SELECT and the INSERT.
 */
@Component
public class EmployeeIdAllocator {

    private static final Pattern EMP_ID_PATTERN = Pattern.compile("^EMP-(\\d+)$");

    private final EmployeeRepository employeeRepository;

    public EmployeeIdAllocator(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public synchronized String nextEmpId() {
        int max = employeeRepository.findAllEmpIds().stream()
                .map(EMP_ID_PATTERN::matcher)
                .filter(java.util.regex.Matcher::matches)
                .mapToInt(m -> Integer.parseInt(m.group(1)))
                .max()
                .orElse(0);
        return "EMP-" + String.format("%03d", max + 1);
    }
}
