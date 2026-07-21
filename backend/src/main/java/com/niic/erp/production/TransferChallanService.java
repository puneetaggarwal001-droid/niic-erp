package com.niic.erp.production;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.TransferChallanCreateRequest;
import com.niic.erp.production.dto.TransferChallanDto;
import com.niic.erp.production.dto.TransferChallanItemRequest;
import com.niic.erp.user.User;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferChallanService {

    private final TransferChallanRepository transferChallanRepository;
    private final JobRepository jobRepository;
    private final WorkstationRepository workstationRepository;
    private final ChallanNumberService challanNumberService;

    public TransferChallanService(TransferChallanRepository transferChallanRepository, JobRepository jobRepository,
                                   WorkstationRepository workstationRepository,
                                   ChallanNumberService challanNumberService) {
        this.transferChallanRepository = transferChallanRepository;
        this.jobRepository = jobRepository;
        this.workstationRepository = workstationRepository;
        this.challanNumberService = challanNumberService;
    }

    @Transactional
    public TransferChallanDto create(TransferChallanCreateRequest request, User actor) {
        if (request.fromWorkstationId().equals(request.toWorkstationId())) {
            throw new BadRequestException("From and to workstation must be different.");
        }
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("Job not found: " + request.jobId()));
        Workstation from = workstationRepository.findById(request.fromWorkstationId())
                .orElseThrow(() -> new NotFoundException("Workstation not found: " + request.fromWorkstationId()));
        Workstation to = workstationRepository.findById(request.toWorkstationId())
                .orElseThrow(() -> new NotFoundException("Workstation not found: " + request.toWorkstationId()));

        TransferChallan challan = new TransferChallan(challanNumberService.nextChallanNo(), job, from, to,
                request.remarks(), actor);
        for (TransferChallanItemRequest itemReq : request.items()) {
            challan.getItems().add(new TransferChallanItem(challan, itemReq.itemId(), itemReq.itemCode(),
                    itemReq.itemName(), itemReq.itemUnit(), itemReq.qty()));
        }
        return TransferChallanDto.from(transferChallanRepository.save(challan));
    }

    @Transactional(readOnly = true)
    public List<TransferChallanDto> listPendingForWorkstation(Long workstationId) {
        return transferChallanRepository.findByToWorkstationIdAndStatus(workstationId, ChallanStatus.PENDING)
                .stream().map(TransferChallanDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TransferChallanDto> listByStatus(ChallanStatus status) {
        return transferChallanRepository.findByStatus(status).stream().map(TransferChallanDto::from).toList();
    }

    @Transactional
    public TransferChallanDto receive(Long id, User actor) {
        TransferChallan challan = findPending(id);
        challan.setStatus(ChallanStatus.RECEIVED);
        challan.setReceivedBy(actor);
        challan.setReceivedAt(Instant.now());
        return TransferChallanDto.from(challan);
    }

    @Transactional
    public TransferChallanDto reject(Long id, String reason, User actor) {
        TransferChallan challan = findPending(id);
        challan.setStatus(ChallanStatus.REJECTED);
        challan.setRejectedBy(actor);
        challan.setRejectedAt(Instant.now());
        challan.setRejectionReason(reason);
        return TransferChallanDto.from(challan);
    }

    private TransferChallan findPending(Long id) {
        TransferChallan challan = transferChallanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer challan not found: " + id));
        if (challan.getStatus() != ChallanStatus.PENDING) {
            throw new BadRequestException("This challan has already been actioned.");
        }
        return challan;
    }
}
