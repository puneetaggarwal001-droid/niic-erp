package com.niic.erp.production;

import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.PcRateDto;
import com.niic.erp.production.dto.PcRateRequest;
import com.niic.erp.security.CurrentUserProvider;
import com.niic.erp.user.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PcRateService {

    private final PcRateRepository pcRateRepository;
    private final WorkstationRepository workstationRepository;
    private final StyleRepository styleRepository;
    private final OperationRepository operationRepository;
    private final CurrentUserProvider currentUserProvider;

    public PcRateService(PcRateRepository pcRateRepository, WorkstationRepository workstationRepository,
                          StyleRepository styleRepository, OperationRepository operationRepository,
                          CurrentUserProvider currentUserProvider) {
        this.pcRateRepository = pcRateRepository;
        this.workstationRepository = workstationRepository;
        this.styleRepository = styleRepository;
        this.operationRepository = operationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Rates are versioned, not updated in place: saving a new rate for the same
    // (workstation, style, modelNo, operation) group deactivates whatever was
    // previously active and inserts a fresh active row, preserving history.
    @Transactional
    public PcRateDto save(PcRateRequest request) {
        Workstation workstation = workstationRepository.findById(request.workstationId())
                .orElseThrow(() -> new NotFoundException("Workstation not found: " + request.workstationId()));
        Style style = styleRepository.findByCode(request.styleCode())
                .orElseThrow(() -> new NotFoundException("Style not found: " + request.styleCode()));
        Operation operation = operationRepository.findById(request.operationId())
                .orElseThrow(() -> new NotFoundException("Operation not found: " + request.operationId()));

        pcRateRepository.findByWorkstationIdAndStyleCodeAndModelNoAndOperationIdAndActiveTrue(
                        workstation.getId(), style.getCode(), request.modelNo(), operation.getId())
                .ifPresent(existing -> existing.setActive(false));

        User createdBy = currentUserProvider.require();
        PcRate rate = new PcRate(workstation, style, request.modelNo(), operation, request.rate(),
                request.effectiveDate(), createdBy);
        return PcRateDto.from(pcRateRepository.save(rate));
    }

    public List<PcRateDto> listActiveForGroup(Long workstationId, String styleCode, String modelNo) {
        return pcRateRepository.findByWorkstationIdAndStyleCodeAndModelNoAndActiveTrue(workstationId, styleCode, modelNo)
                .stream().map(PcRateDto::from).toList();
    }

    // Used by payroll (attendance module) to price PC-rate employees' production output.
    public java.math.BigDecimal getActiveRate(Long workstationId, Long operationId) {
        return pcRateRepository
                .findByWorkstationIdAndOperationIdAndActiveTrueOrderByEffectiveDateDescCreatedAtDesc(workstationId, operationId)
                .stream().findFirst().map(PcRate::getRate)
                .orElseThrow(() -> new NotFoundException("No active PC rate for this workstation/operation."));
    }
}
