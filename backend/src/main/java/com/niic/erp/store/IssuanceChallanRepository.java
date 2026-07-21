package com.niic.erp.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuanceChallanRepository extends JpaRepository<IssuanceChallan, Long> {
    List<IssuanceChallan> findByOrderByCreatedAtDesc();
    List<IssuanceChallan> findByJobId(Long jobId);
}
