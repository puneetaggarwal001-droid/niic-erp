package com.niic.erp.sampling;

import com.niic.erp.sampling.dto.CompleteRequestForm;
import com.niic.erp.sampling.dto.RequestForm;
import com.niic.erp.sampling.dto.SampleRequestDto;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sampling/requests")
public class SampleRequestController {

    private final SampleRequestService requestService;

    public SampleRequestController(SampleRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public List<SampleRequestDto> list() {
        return requestService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SampleRequestDto create(@Valid @RequestBody RequestForm form) {
        return requestService.create(form);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SampleRequestDto update(@PathVariable Long id, @Valid @RequestBody RequestForm form) {
        return requestService.update(id, form);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sampling_access')")
    public SampleRequestDto start(@PathVariable Long id) {
        return requestService.start(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public SampleRequestDto cancel(@PathVariable Long id) {
        return requestService.cancel(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sampling_access')")
    public SampleRequestDto complete(@PathVariable Long id, @Valid @RequestBody CompleteRequestForm form) {
        return requestService.complete(id, form.completedSampleId());
    }
}
