package com.niic.erp.production;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.common.RequestStatus;
import com.niic.erp.production.dto.RoutingChangeRequestDto;
import com.niic.erp.production.dto.RoutingSaveRequest;
import com.niic.erp.production.dto.RoutingWorkstationRequest;
import com.niic.erp.user.User;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutingChangeRequestService {

    private final RoutingChangeRequestRepository routingChangeRequestRepository;
    private final JobRepository jobRepository;
    private final RoutingService routingService;
    private final ObjectMapper objectMapper;

    public RoutingChangeRequestService(RoutingChangeRequestRepository routingChangeRequestRepository,
                                        JobRepository jobRepository, RoutingService routingService,
                                        ObjectMapper objectMapper) {
        this.routingChangeRequestRepository = routingChangeRequestRepository;
        this.jobRepository = jobRepository;
        this.routingService = routingService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RoutingChangeRequestDto submit(RoutingSaveRequest request, String reason, User requestedBy) {
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("Job not found: " + request.jobId()));
        String json = writeWorkstations(request.workstations());
        RoutingChangeRequest changeRequest = new RoutingChangeRequest(job, json, reason, requestedBy);
        return toDto(routingChangeRequestRepository.save(changeRequest));
    }

    public List<RoutingChangeRequestDto> listPending() {
        return routingChangeRequestRepository.findByStatus(RequestStatus.PENDING).stream().map(this::toDto).toList();
    }

    @Transactional
    public RoutingChangeRequestDto approve(Long id, String adminRemark, User reviewer) {
        RoutingChangeRequest changeRequest = findPending(id);
        RoutingSaveRequest saveRequest = new RoutingSaveRequest(changeRequest.getJob().getId(),
                readWorkstations(changeRequest));
        routingService.saveDirect(saveRequest, reviewer);

        changeRequest.setStatus(RequestStatus.APPROVED);
        changeRequest.setReviewedBy(reviewer);
        changeRequest.setReviewedAt(Instant.now());
        changeRequest.setAdminRemark(adminRemark);
        return toDto(changeRequest);
    }

    @Transactional
    public RoutingChangeRequestDto reject(Long id, String adminRemark, User reviewer) {
        RoutingChangeRequest changeRequest = findPending(id);
        changeRequest.setStatus(RequestStatus.REJECTED);
        changeRequest.setReviewedBy(reviewer);
        changeRequest.setReviewedAt(Instant.now());
        changeRequest.setAdminRemark(adminRemark);
        return toDto(changeRequest);
    }

    private RoutingChangeRequest findPending(Long id) {
        RoutingChangeRequest changeRequest = routingChangeRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Routing change request not found: " + id));
        if (changeRequest.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Routing change request has already been reviewed.");
        }
        return changeRequest;
    }

    private RoutingChangeRequestDto toDto(RoutingChangeRequest changeRequest) {
        return RoutingChangeRequestDto.from(changeRequest, readWorkstations(changeRequest));
    }

    private String writeWorkstations(List<RoutingWorkstationRequest> workstations) {
        try {
            return objectMapper.writeValueAsString(workstations);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize proposed routing", e);
        }
    }

    private List<RoutingWorkstationRequest> readWorkstations(RoutingChangeRequest changeRequest) {
        try {
            return objectMapper.readValue(changeRequest.getProposedRoutingJson(),
                    new TypeReference<List<RoutingWorkstationRequest>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse stored proposed routing", e);
        }
    }
}
