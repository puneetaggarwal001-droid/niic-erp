package com.niic.erp.payroll;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorBillRepository extends JpaRepository<ContractorBill, Long> {
    List<ContractorBill> findByPeriodMonthOrderByCreatedAtDesc(String periodMonth);
}
