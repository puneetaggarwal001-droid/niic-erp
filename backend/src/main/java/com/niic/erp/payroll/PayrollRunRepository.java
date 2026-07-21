package com.niic.erp.payroll;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    List<PayrollRun> findByOrderByPeriodMonthDesc();

    Optional<PayrollRun> findByPeriodMonthAndStatus(String periodMonth, PayrollRunStatus status);

    boolean existsByPeriodMonthAndStatus(String periodMonth, PayrollRunStatus status);
}
