package com.niic.erp.production;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.RoutingDto;
import com.niic.erp.production.dto.RoutingOperationRequest;
import com.niic.erp.production.dto.RoutingSaveRequest;
import com.niic.erp.production.dto.RoutingWorkstationRequest;
import com.niic.erp.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutingService {

    private final RoutingRepository routingRepository;
    private final JobRepository jobRepository;
    private final WorkstationRepository workstationRepository;
    private final OperationRepository operationRepository;

    public RoutingService(RoutingRepository routingRepository, JobRepository jobRepository,
                           WorkstationRepository workstationRepository, OperationRepository operationRepository) {
        this.routingRepository = routingRepository;
        this.jobRepository = jobRepository;
        this.workstationRepository = workstationRepository;
        this.operationRepository = operationRepository;
    }

    // A job has exactly one routing "slot". Re-saving replaces it wholesale
    // (delete + recreate, DB cascades clean up the old rows) rather than
    // diffing in place — simpler, and the end state is identical either way.
    @Transactional
    public RoutingDto saveDirect(RoutingSaveRequest request, User user) {
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("Job not found: " + request.jobId()));

        routingRepository.findByJobId(job.getId()).ifPresent(routingRepository::delete);
        routingRepository.flush();

        Routing routing = new Routing(job, user);
        buildWorkstations(routing, request.workstations());
        job.setRoutingAssigned(true);
        return RoutingDto.from(routingRepository.save(routing));
    }

    @Transactional(readOnly = true)
    public RoutingDto getForJob(Long jobId) {
        return routingRepository.findByJobId(jobId)
                .map(RoutingDto::from)
                .orElseThrow(() -> new NotFoundException("No routing defined for job " + jobId));
    }

    // Convenience lookup mirroring the legacy "load previous routing" helper —
    // finds the most recent routed job with the same model (falling back to
    // the same style) so a new job's routing can be cloned from it.
    @Transactional(readOnly = true)
    public Optional<RoutingDto> findTemplate(String modelNo, String style) {
        return jobRepository.findAll().stream()
                .filter(j -> j.isRoutingAssigned() && j.getModelNo().equalsIgnoreCase(modelNo))
                .max(java.util.Comparator.comparing(Job::getCreatedAt))
                .or(() -> jobRepository.findAll().stream()
                        .filter(j -> j.isRoutingAssigned() && j.getStyle().getCode().equals(style))
                        .max(java.util.Comparator.comparing(Job::getCreatedAt)))
                .flatMap(j -> routingRepository.findByJobId(j.getId()))
                .map(RoutingDto::from);
    }

    private void buildWorkstations(Routing routing, java.util.List<RoutingWorkstationRequest> wsRequests) {
        Map<Long, RoutingOperation> byOperationId = new HashMap<>();
        int wsSeq = 0;
        for (RoutingWorkstationRequest wsReq : wsRequests) {
            Workstation ws = workstationRepository.findById(wsReq.workstationId())
                    .orElseThrow(() -> new NotFoundException("Workstation not found: " + wsReq.workstationId()));
            RoutingWorkstation rw = new RoutingWorkstation(routing, wsSeq++, ws);
            int opSeq = 0;
            for (RoutingOperationRequest opReq : wsReq.operations()) {
                Operation op = operationRepository.findById(opReq.operationId())
                        .orElseThrow(() -> new NotFoundException("Operation not found: " + opReq.operationId()));
                RoutingOperation ro = new RoutingOperation(rw, opSeq++, op);
                rw.getOperations().add(ro);
                byOperationId.put(op.getId(), ro);
            }
            routing.getWorkstations().add(rw);
        }

        for (RoutingWorkstationRequest wsReq : wsRequests) {
            for (RoutingOperationRequest opReq : wsReq.operations()) {
                if (opReq.dependsOnOperationIds() == null || opReq.dependsOnOperationIds().isEmpty()) {
                    continue;
                }
                RoutingOperation ro = byOperationId.get(opReq.operationId());
                for (Long depOpId : opReq.dependsOnOperationIds()) {
                    RoutingOperation dep = byOperationId.get(depOpId);
                    if (dep == null) {
                        throw new BadRequestException(
                                "Dependency operation " + depOpId + " is not part of this routing.");
                    }
                    ro.getDependsOn().add(dep);
                }
            }
        }
    }
}
