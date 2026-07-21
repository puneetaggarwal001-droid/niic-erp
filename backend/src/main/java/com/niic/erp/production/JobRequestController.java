package com.niic.erp.production;

import com.niic.erp.production.dto.JobCreateRequest;
import com.niic.erp.production.dto.JobRequestDto;
import com.niic.erp.production.dto.JobRequestReviewRequest;
import com.niic.erp.security.CurrentUserProvider;
import com.niic.erp.user.Role;
import com.niic.erp.user.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
@RequestMapping("/api/production/job-requests")
public class JobRequestController {

    private final JobRequestService jobRequestService;
    private final CurrentUserProvider currentUserProvider;

    public JobRequestController(JobRequestService jobRequestService, CurrentUserProvider currentUserProvider) {
        this.jobRequestService = jobRequestService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobRequestDto submit(@Valid @RequestBody JobCreateRequest request) {
        User user = currentUserProvider.require();
        if (user.getRole() != Role.ADMIN && !user.getRights().contains("create_job")) {
            throw new AccessDeniedException("You do not have permission to create jobs.");
        }
        return jobRequestService.submit(request, user);
    }

    @GetMapping
    public List<JobRequestDto> list(@RequestParam(defaultValue = "false") boolean mine) {
        User user = currentUserProvider.require();
        if (mine) {
            return jobRequestService.listMine(user.getUsername());
        }
        return jobRequestService.listPending();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public JobRequestDto approve(@PathVariable Long id, @RequestBody(required = false) JobRequestReviewRequest request) {
        String remark = request != null ? request.adminRemark() : null;
        return jobRequestService.approve(id, remark, currentUserProvider.require());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public JobRequestDto reject(@PathVariable Long id, @RequestBody(required = false) JobRequestReviewRequest request) {
        String remark = request != null ? request.adminRemark() : null;
        return jobRequestService.reject(id, remark, currentUserProvider.require());
    }
}
