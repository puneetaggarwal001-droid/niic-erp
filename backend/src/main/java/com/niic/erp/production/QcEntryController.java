package com.niic.erp.production;

import com.niic.erp.production.dto.QcEntryCreateRequest;
import com.niic.erp.production.dto.QcEntryDto;
import com.niic.erp.production.dto.QcEntryFillRequest;
import com.niic.erp.security.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/production/qc-entries")
public class QcEntryController {

    private final QcEntryService qcEntryService;
    private final CurrentUserProvider currentUserProvider;

    public QcEntryController(QcEntryService qcEntryService, CurrentUserProvider currentUserProvider) {
        this.qcEntryService = qcEntryService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<QcEntryDto> list(@RequestParam(required = false) Long jobId,
                                  @RequestParam(defaultValue = "false") boolean pendingOnly) {
        if (pendingOnly) {
            return qcEntryService.listPending();
        }
        return qcEntryService.listForJob(jobId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QcEntryDto create(@Valid @RequestBody QcEntryCreateRequest request) {
        return qcEntryService.create(request, currentUserProvider.require());
    }

    @PutMapping("/{id}/fill-details")
    public QcEntryDto fillDetails(@PathVariable Long id, @Valid @RequestBody QcEntryFillRequest request) {
        return qcEntryService.fillDetails(id, request, currentUserProvider.require());
    }
}
