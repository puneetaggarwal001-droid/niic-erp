package com.niic.erp.sampling;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRepository extends JpaRepository<Sample, Long> {

    List<Sample> findByOrderByCreatedAtDesc();

    List<Sample> findByRevBase(String revBase);

    List<Sample> findByStatus(SampleStatus status);
}
