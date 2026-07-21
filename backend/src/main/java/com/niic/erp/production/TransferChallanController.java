package com.niic.erp.production;

import com.niic.erp.production.dto.TransferChallanCreateRequest;
import com.niic.erp.production.dto.TransferChallanDto;
import com.niic.erp.production.dto.TransferChallanRejectRequest;
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
@RequestMapping("/api/production/transfer-challans")
public class TransferChallanController {

    private final TransferChallanService transferChallanService;
    private final CurrentUserProvider currentUserProvider;

    public TransferChallanController(TransferChallanService transferChallanService,
                                      CurrentUserProvider currentUserProvider) {
        this.transferChallanService = transferChallanService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<TransferChallanDto> list(@RequestParam(required = false) Long pendingForWorkstationId,
                                          @RequestParam(defaultValue = "PENDING") ChallanStatus status) {
        if (pendingForWorkstationId != null) {
            return transferChallanService.listPendingForWorkstation(pendingForWorkstationId);
        }
        return transferChallanService.listByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferChallanDto create(@Valid @RequestBody TransferChallanCreateRequest request) {
        return transferChallanService.create(request, currentUserProvider.require());
    }

    // Legacy also lets the destination workstation's own assigned employee confirm
    // receipt, but that requires a User<->Employee link this schema doesn't model
    // yet — restricted to admin/store_admin here until that link exists.
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_ADMIN')")
    public TransferChallanDto receive(@PathVariable Long id) {
        return transferChallanService.receive(id, currentUserProvider.require());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_ADMIN')")
    public TransferChallanDto reject(@PathVariable Long id, @Valid @RequestBody TransferChallanRejectRequest request) {
        return transferChallanService.reject(id, request.reason(), currentUserProvider.require());
    }
}
