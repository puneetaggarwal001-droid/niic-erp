package com.niic.erp.attendance;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatePassRepository extends JpaRepository<GatePass, Long> {

    List<GatePass> findByDateOrderByCreatedAtDesc(LocalDate date);

    List<GatePass> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);

    int countByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);
}
