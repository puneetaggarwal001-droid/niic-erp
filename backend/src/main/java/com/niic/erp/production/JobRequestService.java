package com.niic.erp.production;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.common.RequestStatus;
import com.niic.erp.production.dto.JobColourRequest;
import com.niic.erp.production.dto.JobCreateRequest;
import com.niic.erp.production.dto.JobRequestDto;
import com.niic.erp.user.User;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobRequestService {

    private final JobRequestRepository jobRequestRepository;
    private final StyleRepository styleRepository;
    private final JobService jobService;
    private final ObjectMapper objectMapper;

    public JobRequestService(JobRequestRepository jobRequestRepository, StyleRepository styleRepository,
                              JobService jobService, ObjectMapper objectMapper) {
        this.jobRequestRepository = jobRequestRepository;
        this.styleRepository = styleRepository;
        this.jobService = jobService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JobRequestDto submit(JobCreateRequest request, User requestedBy) {
        Style style = styleRepository.findByCode(request.styleCode())
                .orElseThrow(() -> new NotFoundException("Style not found: " + request.styleCode()));
        String coloursJson = writeColours(request.colours());
        JobRequest jobRequest = new JobRequest(style, request.modelNo(), request.fgItemId(), request.unit(),
                coloursJson, requestedBy);
        return toDto(jobRequestRepository.save(jobRequest));
    }

    public List<JobRequestDto> listPending() {
        return jobRequestRepository.findByStatus(RequestStatus.PENDING).stream().map(this::toDto).toList();
    }

    public List<JobRequestDto> listMine(String username) {
        return jobRequestRepository.findByRequestedByUsername(username).stream().map(this::toDto).toList();
    }

    // Approving a request allocates a real job number and creates the live Job —
    // the request itself never held one (see JobNumberAllocator javadoc for why
    // that's a deliberate change from legacy behavior).
    @Transactional
    public JobRequestDto approve(Long id, String adminRemark, User reviewer) {
        JobRequest jobRequest = findPending(id);
        JobCreateRequest createRequest = new JobCreateRequest(jobRequest.getStyle().getCode(), jobRequest.getModelNo(),
                jobRequest.getFgItemId(), jobRequest.getUnit(), readColours(jobRequest));
        jobService.createJob(createRequest, JobSource.APPROVED_REQUEST, jobRequest.getRequestedBy());

        jobRequest.setStatus(RequestStatus.APPROVED);
        jobRequest.setReviewedBy(reviewer);
        jobRequest.setReviewedAt(Instant.now());
        jobRequest.setAdminRemark(adminRemark);
        return toDto(jobRequest);
    }

    @Transactional
    public JobRequestDto reject(Long id, String adminRemark, User reviewer) {
        JobRequest jobRequest = findPending(id);
        jobRequest.setStatus(RequestStatus.REJECTED);
        jobRequest.setReviewedBy(reviewer);
        jobRequest.setReviewedAt(Instant.now());
        jobRequest.setAdminRemark(adminRemark);
        return toDto(jobRequest);
    }

    private JobRequest findPending(Long id) {
        JobRequest jobRequest = jobRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job request not found: " + id));
        if (jobRequest.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Job request has already been reviewed.");
        }
        return jobRequest;
    }

    private JobRequestDto toDto(JobRequest jobRequest) {
        return JobRequestDto.from(jobRequest, readColours(jobRequest));
    }

    private String writeColours(List<JobColourRequest> colours) {
        try {
            return objectMapper.writeValueAsString(colours);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize job request payload", e);
        }
    }

    private List<JobColourRequest> readColours(JobRequest jobRequest) {
        try {
            return objectMapper.readValue(jobRequest.getColoursJson(), new TypeReference<List<JobColourRequest>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse stored job request payload", e);
        }
    }
}
