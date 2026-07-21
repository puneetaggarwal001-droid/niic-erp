package com.niic.erp.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRequisitionRepository extends JpaRepository<MaterialRequisition, Long> {
    List<MaterialRequisition> findByOrderByCreatedAtDesc();
}
