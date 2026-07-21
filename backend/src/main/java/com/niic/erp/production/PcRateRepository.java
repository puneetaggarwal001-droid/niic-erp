package com.niic.erp.production;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcRateRepository extends JpaRepository<PcRate, Long> {

    Optional<PcRate> findByWorkstationIdAndStyleCodeAndModelNoAndOperationIdAndActiveTrue(
            Long workstationId, String styleCode, String modelNo, Long operationId);

    List<PcRate> findByWorkstationIdAndStyleCodeAndModelNoAndActiveTrue(
            Long workstationId, String styleCode, String modelNo);

    // Current active rate for wages calculation. Multiple active rows can exist across
    // different (style, modelNo) groups for the same (workstation, operation) pair — see
    // production package-info for why the legacy lookup is ambiguous on that axis. We
    // resolve it by taking the most recently effective row.
    List<PcRate> findByWorkstationIdAndOperationIdAndActiveTrueOrderByEffectiveDateDescCreatedAtDesc(
            Long workstationId, Long operationId);
}
