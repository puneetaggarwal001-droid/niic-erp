package com.niic.erp.production;

import com.niic.erp.production.dto.QcReworkDto;
import com.niic.erp.production.dto.QcReworkResultRequest;
import com.niic.erp.security.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/qc-rework")
public class QcReworkController {

    private final QcReworkService qcReworkService;
    private final CurrentUserProvider currentUserProvider;

    public QcReworkController(QcReworkService qcReworkService, CurrentUserProvider currentUserProvider) {
        this.qcReworkService = qcReworkService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<QcReworkDto> list(@RequestParam(required = false) Long jobId,
                                   @RequestParam(defaultValue = "false") boolean pendingOnly) {
        if (pendingOnly) {
            return qcReworkService.listPending();
        }
        return qcReworkService.listForJob(jobId);
    }

    @PostMapping("/{id}/result")
    public QcReworkDto recordResult(@PathVariable Long id, @Valid @RequestBody QcReworkResultRequest request) {
        return qcReworkService.recordResult(id, request.done(), request.reject(), currentUserProvider.require());
    }
}
