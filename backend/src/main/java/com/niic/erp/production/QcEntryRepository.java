package com.niic.erp.production;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcEntryRepository extends JpaRepository<QcEntry, Long> {
    List<QcEntry> findByJobId(Long jobId);

    List<QcEntry> findByStatus(QcStatus status);

    List<QcEntry> findByDateBetween(LocalDate from, LocalDate to);
}
