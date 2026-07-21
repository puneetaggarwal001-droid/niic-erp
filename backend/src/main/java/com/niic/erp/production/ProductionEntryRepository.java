package com.niic.erp.production;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionEntryRepository extends JpaRepository<ProductionEntry, Long> {

    List<ProductionEntry> findByDate(LocalDate date);

    List<ProductionEntry> findByJobId(Long jobId);

    List<ProductionEntry> findByDateBetween(LocalDate from, LocalDate to);

    List<ProductionEntry> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);

    // Null-safe match on the (date, employee, job, colour, size, side) merge key —
    // see production package-info; plain equality on a null bind parameter would
    // never match a null column.
    @Query("select e from ProductionEntry e where e.date = :date and e.employee.id = :employeeId "
            + "and e.job.id = :jobId "
            + "and (:colourId is null and e.colour is null or e.colour.id = :colourId) "
            + "and (:sizeId is null and e.size is null or e.size.id = :sizeId) "
            + "and (:side is null and e.side is null or e.side = :side)")
    Optional<ProductionEntry> findMergeTarget(@Param("date") LocalDate date, @Param("employeeId") Long employeeId,
                                               @Param("jobId") Long jobId, @Param("colourId") Long colourId,
                                               @Param("sizeId") Long sizeId, @Param("side") Side side);

    // Every entry for a (job, colour, size) combo regardless of side/date/employee —
    // used to consolidate all prior entries when an approved edit request is applied.
    @Query("select e from ProductionEntry e where e.job.id = :jobId "
            + "and (:colourId is null and e.colour is null or e.colour.id = :colourId) "
            + "and (:sizeId is null and e.size is null or e.size.id = :sizeId)")
    List<ProductionEntry> findAllForJobColourSize(@Param("jobId") Long jobId, @Param("colourId") Long colourId,
                                                   @Param("sizeId") Long sizeId);

    // Total pieces already logged at one operation within a (job, colour, size, side)
    // combo — the core input to the routing dependency-flow availability calc.
    @Query("select coalesce(sum(op.quantity), 0) from ProductionEntryOp op where op.productionEntry.job.id = :jobId "
            + "and (:colourId is null and op.productionEntry.colour is null or op.productionEntry.colour.id = :colourId) "
            + "and (:sizeId is null and op.productionEntry.size is null or op.productionEntry.size.id = :sizeId) "
            + "and (:side is null and op.productionEntry.side is null or op.productionEntry.side = :side) "
            + "and op.operation.id = :operationId")
    int sumQuantityForOperation(@Param("jobId") Long jobId, @Param("colourId") Long colourId,
                                 @Param("sizeId") Long sizeId, @Param("side") Side side,
                                 @Param("operationId") Long operationId);
}
