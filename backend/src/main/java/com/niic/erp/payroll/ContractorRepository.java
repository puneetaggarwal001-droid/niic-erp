package com.niic.erp.payroll;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorRepository extends JpaRepository<Contractor, Long> {
    List<Contractor> findByActiveTrueOrderByName();
}
