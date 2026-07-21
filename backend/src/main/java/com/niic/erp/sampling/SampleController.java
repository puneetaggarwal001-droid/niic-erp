package com.niic.erp.sampling;

import com.niic.erp.sampling.dto.CloseForm;
import com.niic.erp.sampling.dto.PhotoDto;
import com.niic.erp.sampling.dto.PhotoForm;
import com.niic.erp.sampling.dto.PpmForm;
import com.niic.erp.sampling.dto.PpsForm;
import com.niic.erp.sampling.dto.SampleDto;
import com.niic.erp.sampling.dto.SampleForm;
import com.niic.erp.sampling.dto.SampleSummaryDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/sampling/samples")
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping
    public List<SampleSummaryDto> list(@RequestParam(required = false) SampleStatus status,
                                       @RequestParam(defaultValue = "false") boolean includeClosed) {
        return sampleService.list(status, includeClosed);
    }

    @GetMapping("/{id}")
    public SampleDto get(@PathVariable Long id) {
        return sampleService.getDto(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SampleDto create(@Valid @RequestBody SampleForm form,
                            @RequestParam(defaultValue = "false") boolean submit) {
        return sampleService.create(form, submit);
    }

    @PutMapping("/{id}")
    public SampleDto update(@PathVariable Long id, @Valid @RequestBody SampleForm form,
                            @RequestParam(defaultValue = "false") boolean submit) {
        return sampleService.update(id, form, submit);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sampleService.delete(id);
    }

    @PostMapping("/{id}/submit")
    public SampleDto submit(@PathVariable Long id) {
        return sampleService.submit(id);
    }

    @PostMapping("/{id}/select")
    @PreAuthorize("hasRole('ADMIN')")
    public SampleDto select(@PathVariable Long id) {
        return sampleService.select(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public SampleDto reject(@PathVariable Long id) {
        return sampleService.reject(id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public SampleDto close(@PathVariable Long id, @RequestBody(required = false) CloseForm form) {
        return sampleService.close(id, form != null ? form.remark() : null);
    }

    @PostMapping("/{id}/revise")
    public SampleDto revise(@PathVariable Long id) {
        return sampleService.revise(id);
    }

    @PostMapping("/{id}/pps")
    public SampleDto savePps(@PathVariable Long id, @RequestBody PpsForm form,
                             @RequestParam(defaultValue = "false") boolean approve) {
        return sampleService.savePps(id, form, approve);
    }

    @PostMapping("/{id}/ppm")
    public SampleDto savePpm(@PathVariable Long id, @Valid @RequestBody PpmForm form) {
        return sampleService.savePpm(id, form);
    }

    @PostMapping("/{id}/ppm/complete")
    public SampleDto completePpm(@PathVariable Long id) {
        return sampleService.completePpm(id);
    }

    @PostMapping("/{id}/photos")
    @ResponseStatus(HttpStatus.CREATED)
    public PhotoDto addPhoto(@PathVariable Long id, @Valid @RequestBody PhotoForm form) {
        return sampleService.addPhoto(id, form);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto(@PathVariable Long id, @PathVariable Long photoId) {
        sampleService.deletePhoto(id, photoId);
    }
}
