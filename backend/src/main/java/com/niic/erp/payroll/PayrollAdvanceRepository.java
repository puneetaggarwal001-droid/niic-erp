package com.niic.erp.payroll;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollAdvanceRepository extends JpaRepository<PayrollAdvance, Long> {

    List<PayrollAdvance> findByPeriodMonthOrderByCreatedAtDesc(String periodMonth);

    List<PayrollAdvance> findByEmployeeIdAndPeriodMonth(Long employeeId, String periodMonth);

    List<PayrollAdvance> findByEmployeeIdAndPeriodMonthAndDeductedFalse(Long employeeId, String periodMonth);
}
