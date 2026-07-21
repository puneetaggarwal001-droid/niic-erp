package com.niic.erp.production;

import com.niic.erp.common.RequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutingChangeRequestRepository extends JpaRepository<RoutingChangeRequest, Long> {
    List<RoutingChangeRequest> findByStatus(RequestStatus status);
}
