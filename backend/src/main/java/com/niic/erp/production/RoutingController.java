package com.niic.erp.production;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.production.dto.RoutingDto;
import com.niic.erp.production.dto.RoutingSaveRequest;
import com.niic.erp.production.dto.RoutingSaveResultDto;
import com.niic.erp.security.CurrentUserProvider;
import com.niic.erp.user.Role;
import com.niic.erp.user.User;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/routing")
public class RoutingController {

    private final RoutingService routingService;
    private final RoutingChangeRequestService routingChangeRequestService;
    private final CurrentUserProvider currentUserProvider;

    public RoutingController(RoutingService routingService, RoutingChangeRequestService routingChangeRequestService,
                              CurrentUserProvider currentUserProvider) {
        this.routingService = routingService;
        this.routingChangeRequestService = routingChangeRequestService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/{jobId}")
    public RoutingDto getForJob(@PathVariable Long jobId) {
        return routingService.getForJob(jobId);
    }

    @GetMapping("/template")
    public Optional<RoutingDto> findTemplate(@RequestParam String modelNo, @RequestParam String style) {
        return routingService.findTemplate(modelNo, style);
    }

    // Admins save routing directly; everyone else submits a change request that
    // an admin must approve before it takes effect (mirrors rtSaveOrRequest).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutingSaveResultDto save(@Valid @RequestBody RoutingSaveRequest request,
                                      @RequestParam(required = false) String reason) {
        User user = currentUserProvider.require();
        if (user.getRole() == Role.ADMIN) {
            return RoutingSaveResultDto.direct(routingService.saveDirect(request, user));
        }
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("A reason is required when requesting a routing change.");
        }
        return RoutingSaveResultDto.pending(routingChangeRequestService.submit(request, reason, user));
    }
}
