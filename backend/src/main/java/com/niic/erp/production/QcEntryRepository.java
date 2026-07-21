package com.niic.erp.production;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcEntryRepository extends JpaRepository<QcEntry, Long> {
    List<QcEntry> findByJobId(Long jobId);

    List<QcEntry> findByStatus(QcStatus status);
}
