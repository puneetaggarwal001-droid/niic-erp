package com.niic.erp.production;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutingRepository extends JpaRepository<Routing, Long> {
    Optional<Routing> findByJobId(Long jobId);
}
