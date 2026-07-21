package com.niic.erp.production;

import com.niic.erp.production.dto.JobRequestReviewRequest;
import com.niic.erp.production.dto.RoutingChangeRequestDto;
import com.niic.erp.security.CurrentUserProvider;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/routing-change-requests")
@PreAuthorize("hasRole('ADMIN')")
public class RoutingChangeRequestController {

    private final RoutingChangeRequestService routingChangeRequestService;
    private final CurrentUserProvider currentUserProvider;

    public RoutingChangeRequestController(RoutingChangeRequestService routingChangeRequestService,
                                           CurrentUserProvider currentUserProvider) {
        this.routingChangeRequestService = routingChangeRequestService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<RoutingChangeRequestDto> list() {
        return routingChangeRequestService.listPending();
    }

    @PostMapping("/{id}/approve")
    public RoutingChangeRequestDto approve(@PathVariable Long id,
                                            @RequestBody(required = false) JobRequestReviewRequest request) {
        String remark = request != null ? request.adminRemark() : null;
        return routingChangeRequestService.approve(id, remark, currentUserProvider.require());
    }

    @PostMapping("/{id}/reject")
    public RoutingChangeRequestDto reject(@PathVariable Long id,
                                           @RequestBody(required = false) JobRequestReviewRequest request) {
        String remark = request != null ? request.adminRemark() : null;
        return routingChangeRequestService.reject(id, remark, currentUserProvider.require());
    }
}
