package com.niic.erp.production;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findByWorkstationId(Long workstationId);

    Optional<Operation> findByWorkstationIdAndNameIgnoreCase(Long workstationId, String name);
}
