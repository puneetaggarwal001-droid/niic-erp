package com.niic.erp.production;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationClosureRepository extends JpaRepository<OperationClosure, Long> {

    List<OperationClosure> findByJobId(Long jobId);

    // colour/size are nullable (PAIR jobs with no colour breakdown), and plain
    // equality on a null bind parameter never matches a null column in SQL —
    // hence the explicit null-safe comparison here instead of a derived query.
    @Query("select oc from OperationClosure oc where oc.job.id = :jobId "
            + "and (:colourId is null and oc.colour is null or oc.colour.id = :colourId) "
            + "and (:sizeId is null and oc.size is null or oc.size.id = :sizeId) "
            + "and oc.workstation.id = :workstationId and oc.operation.id = :operationId")
    Optional<OperationClosure> findMatch(@Param("jobId") Long jobId, @Param("colourId") Long colourId,
                                         @Param("sizeId") Long sizeId, @Param("workstationId") Long workstationId,
                                         @Param("operationId") Long operationId);
}
