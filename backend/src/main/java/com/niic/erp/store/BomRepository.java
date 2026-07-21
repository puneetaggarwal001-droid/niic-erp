package com.niic.erp.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BomRepository extends JpaRepository<Bom, Long> {
    List<Bom> findByOutputItemIdAndActiveTrue(Long outputItemId);
    List<Bom> findByOutputItemId(Long outputItemId);
}
