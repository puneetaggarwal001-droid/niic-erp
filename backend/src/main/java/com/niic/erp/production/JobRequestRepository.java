package com.niic.erp.production;

import com.niic.erp.common.RequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRequestRepository extends JpaRepository<JobRequest, Long> {
    List<JobRequest> findByStatus(RequestStatus status);

    List<JobRequest> findByRequestedByUsername(String username);
}
