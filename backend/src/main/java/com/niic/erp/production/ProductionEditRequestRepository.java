package com.niic.erp.production;

import com.niic.erp.common.RequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionEditRequestRepository extends JpaRepository<ProductionEditRequest, Long> {
    List<ProductionEditRequest> findByStatus(RequestStatus status);

    List<ProductionEditRequest> findByRequestedByUsernameAndStatus(String username, RequestStatus status);
}
