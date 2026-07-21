package com.niic.erp.sampling;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRequestRepository extends JpaRepository<SampleRequest, Long> {

    List<SampleRequest> findByOrderByCreatedAtDesc();

    List<SampleRequest> findByCompletedSampleId(Long completedSampleId);

    List<SampleRequest> findByRefSampleIdInAndReqTypeAndStatusIn(
            Collection<Long> refSampleIds, RequestType reqType, Collection<RequestStatus> statuses);
}
