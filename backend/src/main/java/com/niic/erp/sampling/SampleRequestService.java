package com.niic.erp.sampling;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.sampling.dto.RequestForm;
import com.niic.erp.sampling.dto.SampleRequestDto;
import com.niic.erp.security.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin sample requests to the sampler. Creating a CHANGE request closes the
 * referenced in-review/draft sample (remark CHANGE_REQUESTED) and resolves the
 * request that produced it, mirroring the legacy "old jobs no longer open" rule.
 */
@Service
public class SampleRequestService {

    private final SampleRequestRepository requestRepository;
    private final SampleRepository sampleRepository;
    private final CurrentUserProvider currentUserProvider;

    public SampleRequestService(SampleRequestRepository requestRepository, SampleRepository sampleRepository,
                                CurrentUserProvider currentUserProvider) {
        this.requestRepository = requestRepository;
        this.sampleRepository = sampleRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<SampleRequestDto> list() {
        Map<Long, String> sampleNos = sampleRepository.findAll().stream()
                .collect(Collectors.toMap(Sample::getId, Sample::getSampleNo));
        return requestRepository.findByOrderByCreatedAtDesc().stream()
                .map(r -> SampleRequestDto.from(r,
                        r.getRefSampleId() != null ? sampleNos.get(r.getRefSampleId()) : null,
                        r.getCompletedSampleId() != null ? sampleNos.get(r.getCompletedSampleId()) : null))
                .toList();
    }

    public SampleRequest get(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sample request " + id + " not found."));
    }

    @Transactional
    public SampleRequestDto create(RequestForm form) {
        RequestType type = parseType(form.reqType());
        Long refSampleId = type == RequestType.CHANGE ? form.refSampleId() : null;
        if (type == RequestType.CHANGE && refSampleId == null) {
            throw new BadRequestException("A change request must reference a sample.");
        }
        SampleRequest request = new SampleRequest(type, form.title().trim(), form.description(),
                normalizePriority(form.priority()), refSampleId, currentUserProvider.require().getUsername());
        SampleRequest saved = requestRepository.save(request);

        if (type == RequestType.CHANGE && refSampleId != null) {
            Sample ref = sampleRepository.findById(refSampleId)
                    .orElseThrow(() -> new NotFoundException("Sample " + form.refSampleId() + " not found."));
            if (ref.getStatus() == SampleStatus.IN_REVIEW || ref.getStatus() == SampleStatus.DRAFT) {
                ref.setStatus(SampleStatus.CLOSED);
                ref.setClosedRemark(ClosedRemark.CHANGE_REQUESTED);
                ref.setClosedAt(Instant.now());
            }
            resolveProducing(refSampleId, ClosedRemark.CHANGE_REQUESTED);
        }
        return toDto(saved);
    }

    @Transactional
    public SampleRequestDto update(Long id, RequestForm form) {
        SampleRequest request = get(id);
        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BadRequestException("A completed request can no longer be edited.");
        }
        request.setTitle(form.title().trim());
        request.setDescription(form.description());
        request.setPriority(normalizePriority(form.priority()));
        return toDto(request);
    }

    @Transactional
    public SampleRequestDto start(Long id) {
        SampleRequest request = get(id);
        if (request.getStatus() == RequestStatus.PENDING) {
            request.setStatus(RequestStatus.IN_PROGRESS);
        }
        return toDto(request);
    }

    @Transactional
    public SampleRequestDto cancel(Long id) {
        SampleRequest request = get(id);
        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BadRequestException("A completed request cannot be cancelled.");
        }
        request.setStatus(RequestStatus.CANCELLED);
        return toDto(request);
    }

    /** The sampler marks a request done, linking the sample they produced. */
    @Transactional
    public SampleRequestDto complete(Long id, Long completedSampleId) {
        SampleRequest request = get(id);
        Sample sample = sampleRepository.findById(completedSampleId)
                .orElseThrow(() -> new NotFoundException("Sample " + completedSampleId + " not found."));
        request.setStatus(RequestStatus.COMPLETED);
        request.setCompletedSampleId(sample.getId());
        return toDto(request);
    }

    private void resolveProducing(Long sampleId, ClosedRemark remark) {
        String username = currentUserProvider.require().getUsername();
        for (SampleRequest r : requestRepository.findByCompletedSampleId(sampleId)) {
            if (r.getStatus() == RequestStatus.COMPLETED && r.getAdminResolvedAt() == null) {
                r.setAdminResolvedAt(Instant.now());
                r.setAdminResolvedBy(username);
                r.setAdminResolvedRemark(remark);
            }
        }
    }

    private SampleRequestDto toDto(SampleRequest r) {
        Function<Long, String> no = id -> id == null ? null
                : sampleRepository.findById(id).map(Sample::getSampleNo).orElse(null);
        return SampleRequestDto.from(r, no.apply(r.getRefSampleId()), no.apply(r.getCompletedSampleId()));
    }

    private static RequestType parseType(String s) {
        try {
            return RequestType.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Unknown request type: " + s);
        }
    }

    private static String normalizePriority(String p) {
        if (p == null || p.isBlank()) {
            return "NORMAL";
        }
        return p.trim().toUpperCase();
    }
}
