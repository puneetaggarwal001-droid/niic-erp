package com.niic.erp.production;

import com.niic.erp.production.dto.JobCreateRequest;
import com.niic.erp.production.dto.JobDto;
import com.niic.erp.security.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/jobs")
public class JobController {

    private final JobService jobService;
    private final CurrentUserProvider currentUserProvider;

    public JobController(JobService jobService, CurrentUserProvider currentUserProvider) {
        this.jobService = jobService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<JobDto> list(@RequestParam(defaultValue = "true") boolean activeOnly) {
        return activeOnly ? jobService.listActive() : jobService.listAll();
    }

    @GetMapping("/{id}")
    public JobDto get(@PathVariable Long id) {
        return jobService.get(id);
    }

    // Admin-only direct creation — non-admins with the create_job right go
    // through JobRequestController instead and wait for admin approval.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public JobDto create(@Valid @RequestBody JobCreateRequest request) {
        return jobService.createJob(request, JobSource.ADMIN_DIRECT, currentUserProvider.require());
    }

    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public JobDto setActive(@PathVariable Long id, @RequestParam boolean active) {
        return jobService.setActive(id, active);
    }
}
