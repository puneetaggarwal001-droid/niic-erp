package com.niic.erp.production;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkstationRepository extends JpaRepository<Workstation, Long> {
    Optional<Workstation> findByNameIgnoreCase(String name);
}
