package com.niic.erp.production;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferChallanRepository extends JpaRepository<TransferChallan, Long> {
    List<TransferChallan> findByStatus(ChallanStatus status);

    List<TransferChallan> findByToWorkstationIdAndStatus(Long workstationId, ChallanStatus status);
}
