package com.niic.erp.store;

import com.niic.erp.store.dto.IssuanceDto;
import com.niic.erp.store.dto.IssueRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/issuances")
public class IssuanceController {

    private final IssuanceService issuanceService;

    public IssuanceController(IssuanceService issuanceService) {
        this.issuanceService = issuanceService;
    }

    @GetMapping
    public List<IssuanceDto> list() {
        return issuanceService.list().stream().map(IssuanceDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssuanceDto issue(@Valid @RequestBody IssueRequest request) {
        List<IssuanceService.LineSpec> lines = request.lines().stream()
                .map(l -> new IssuanceService.LineSpec(l.itemId(), l.variantId(), l.quantity()))
                .toList();
        return IssuanceDto.from(
                issuanceService.issue(request.jobId(), request.workstationId(), request.notes(), lines));
    }
}
