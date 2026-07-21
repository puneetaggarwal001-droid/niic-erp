package com.niic.erp.production;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcReworkRepository extends JpaRepository<QcRework, Long> {
    Optional<QcRework> findByQcEntryId(Long qcEntryId);

    List<QcRework> findByJobId(Long jobId);

    List<QcRework> findByStatus(ReworkStatus status);

    List<QcRework> findByDateBetween(LocalDate from, LocalDate to);
}
