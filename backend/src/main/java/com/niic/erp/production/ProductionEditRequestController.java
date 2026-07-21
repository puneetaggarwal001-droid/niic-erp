package com.niic.erp.production;

import com.niic.erp.production.dto.JobRequestReviewRequest;
import com.niic.erp.production.dto.ProductionEditRequestDto;
import com.niic.erp.production.dto.ProductionEditRequestSubmitRequest;
import com.niic.erp.production.dto.ProductionEntryDto;
import com.niic.erp.production.dto.ProductionEntryRequest;
import com.niic.erp.security.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/edit-requests")
public class ProductionEditRequestController {

    private final ProductionEditRequestService productionEditRequestService;
    private final CurrentUserProvider currentUserProvider;

    public ProductionEditRequestController(ProductionEditRequestService productionEditRequestService,
                                            CurrentUserProvider currentUserProvider) {
        this.productionEditRequestService = productionEditRequestService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductionEditRequestDto submit(@Valid @RequestBody ProductionEditRequestSubmitRequest request) {
        return productionEditRequestService.submit(request, currentUserProvider.require());
    }

    @GetMapping
    public List<ProductionEditRequestDto> list(@RequestParam(defaultValue = "false") boolean mine) {
        if (mine) {
            return productionEditRequestService.listMineUsable(currentUserProvider.require().getUsername());
        }
        return productionEditRequestService.listPending();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductionEditRequestDto approve(@PathVariable Long id,
                                             @RequestBody(required = false) JobRequestReviewRequest request) {
        String remark = request != null ? request.adminRemark() : null;
        return productionEditRequestService.approve(id, remark, currentUserProvider.require());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductionEditRequestDto reject(@PathVariable Long id,
                                            @RequestBody(required = false) JobRequestReviewRequest request) {
        String remark = request != null ? request.adminRemark() : null;
        return productionEditRequestService.reject(id, remark, currentUserProvider.require());
    }

    // Consumes an approved-and-unused edit request, wiping prior entries for the
    // (job, colour, size) and replacing them with the submitted correction.
    @PostMapping("/{id}/consolidate")
    public ProductionEntryDto consolidate(@PathVariable Long id, @Valid @RequestBody ProductionEntryRequest replacement) {
        return productionEditRequestService.consolidate(id, replacement, currentUserProvider.require());
    }
}
