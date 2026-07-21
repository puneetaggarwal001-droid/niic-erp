package com.niic.erp.production;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.common.RequestStatus;
import com.niic.erp.production.dto.ProductionEditRequestDto;
import com.niic.erp.production.dto.ProductionEditRequestSubmitRequest;
import com.niic.erp.production.dto.ProductionEntryDto;
import com.niic.erp.production.dto.ProductionEntryRequest;
import com.niic.erp.user.User;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionEditRequestService {

    private final ProductionEditRequestRepository productionEditRequestRepository;
    private final JobRepository jobRepository;
    private final JobColourRepository jobColourRepository;
    private final JobSizeRepository jobSizeRepository;
    private final ProductionEntryService productionEntryService;

    public ProductionEditRequestService(ProductionEditRequestRepository productionEditRequestRepository,
                                         JobRepository jobRepository, JobColourRepository jobColourRepository,
                                         JobSizeRepository jobSizeRepository,
                                         ProductionEntryService productionEntryService) {
        this.productionEditRequestRepository = productionEditRequestRepository;
        this.jobRepository = jobRepository;
        this.jobColourRepository = jobColourRepository;
        this.jobSizeRepository = jobSizeRepository;
        this.productionEntryService = productionEntryService;
    }

    @Transactional
    public ProductionEditRequestDto submit(ProductionEditRequestSubmitRequest request, User requestedBy) {
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("Job not found: " + request.jobId()));
        JobColour colour = request.colourId() != null ? jobColourRepository.findById(request.colourId())
                .orElseThrow(() -> new NotFoundException("Job colour not found: " + request.colourId())) : null;
        JobSize size = request.sizeId() != null ? jobSizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Job size not found: " + request.sizeId())) : null;

        ProductionEditRequest editRequest = new ProductionEditRequest(job, colour, size, requestedBy, request.reason());
        return ProductionEditRequestDto.from(productionEditRequestRepository.save(editRequest));
    }

    public List<ProductionEditRequestDto> listPending() {
        return productionEditRequestRepository.findByStatus(RequestStatus.PENDING).stream()
                .map(ProductionEditRequestDto::from).toList();
    }

    // Approved-but-not-yet-used requests for the requesting user — these are the
    // ones that can currently be consumed via consolidate().
    public List<ProductionEditRequestDto> listMineUsable(String username) {
        return productionEditRequestRepository.findByRequestedByUsernameAndStatus(username, RequestStatus.APPROVED)
                .stream().filter(r -> !r.isUsed()).map(ProductionEditRequestDto::from).toList();
    }

    @Transactional
    public ProductionEditRequestDto approve(Long id, String resolution, User resolvedBy) {
        ProductionEditRequest editRequest = findPending(id);
        editRequest.setStatus(RequestStatus.APPROVED);
        editRequest.setResolvedBy(resolvedBy);
        editRequest.setResolvedAt(Instant.now());
        editRequest.setResolution(resolution);
        return ProductionEditRequestDto.from(editRequest);
    }

    @Transactional
    public ProductionEditRequestDto reject(Long id, String resolution, User resolvedBy) {
        ProductionEditRequest editRequest = findPending(id);
        editRequest.setStatus(RequestStatus.REJECTED);
        editRequest.setResolvedBy(resolvedBy);
        editRequest.setResolvedAt(Instant.now());
        editRequest.setResolution(resolution);
        return ProductionEditRequestDto.from(editRequest);
    }

    @Transactional
    public ProductionEntryDto consolidate(Long id, ProductionEntryRequest replacement, User actor) {
        ProductionEditRequest editRequest = productionEditRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Edit request not found: " + id));
        if (editRequest.getStatus() != RequestStatus.APPROVED) {
            throw new BadRequestException("This edit request has not been approved.");
        }
        if (editRequest.isUsed()) {
            throw new BadRequestException("This edit request has already been used.");
        }

        ProductionEntryDto dto = productionEntryService.replaceForApprovedEdit(editRequest, replacement, actor);

        editRequest.setUsed(true);
        editRequest.setUsedAt(Instant.now());
        return dto;
    }

    private ProductionEditRequest findPending(Long id) {
        ProductionEditRequest editRequest = productionEditRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Edit request not found: " + id));
        if (editRequest.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Edit request has already been reviewed.");
        }
        return editRequest;
    }
}
