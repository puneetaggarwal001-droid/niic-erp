package com.niic.erp.production;

import com.niic.erp.production.dto.OperationClosureDto;
import com.niic.erp.production.dto.OperationClosureRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/operation-closures")
public class OperationClosureController {

    private final OperationClosureService operationClosureService;

    public OperationClosureController(OperationClosureService operationClosureService) {
        this.operationClosureService = operationClosureService;
    }

    @GetMapping
    public List<OperationClosureDto> list(@RequestParam Long jobId) {
        return operationClosureService.listForJob(jobId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperationClosureDto close(@Valid @RequestBody OperationClosureRequest request) {
        return operationClosureService.close(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reopen(@PathVariable Long id) {
        operationClosureService.reopen(id);
    }
}
