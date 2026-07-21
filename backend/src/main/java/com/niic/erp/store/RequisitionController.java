package com.niic.erp.store;

import com.niic.erp.store.dto.FulfilRequest;
import com.niic.erp.store.dto.RequisitionDto;
import com.niic.erp.store.dto.RequisitionRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/requisitions")
public class RequisitionController {

    private final RequisitionService requisitionService;

    public RequisitionController(RequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    @GetMapping
    public List<RequisitionDto> list() {
        return requisitionService.list().stream().map(RequisitionDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequisitionDto create(@Valid @RequestBody RequisitionRequest request) {
        List<RequisitionService.LineSpec> lines = request.lines().stream()
                .map(l -> new RequisitionService.LineSpec(l.itemId(), l.quantity()))
                .toList();
        return RequisitionDto.from(
                requisitionService.create(request.workstationId(), request.notes(), lines));
    }

    @PostMapping("/{id}/fulfil")
    public RequisitionDto fulfil(@PathVariable Long id, @Valid @RequestBody FulfilRequest request) {
        List<RequisitionService.FulfilSpec> fulfilments = request.fulfilments().stream()
                .map(f -> new RequisitionService.FulfilSpec(f.reqItemId(), f.quantity()))
                .toList();
        return RequisitionDto.from(requisitionService.fulfill(id, fulfilments));
    }
}
