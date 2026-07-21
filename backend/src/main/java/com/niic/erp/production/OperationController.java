package com.niic.erp.production;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.OperationDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/operations")
public class OperationController {

    private final OperationRepository operationRepository;
    private final WorkstationRepository workstationRepository;

    public OperationController(OperationRepository operationRepository, WorkstationRepository workstationRepository) {
        this.operationRepository = operationRepository;
        this.workstationRepository = workstationRepository;
    }

    @GetMapping
    public List<OperationDto> list(@RequestParam(required = false) Long workstationId) {
        List<Operation> operations = workstationId != null
                ? operationRepository.findByWorkstationId(workstationId)
                : operationRepository.findAll();
        return operations.stream().map(OperationDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperationDto create(@Valid @RequestBody OperationDto request) {
        Workstation workstation = workstationRepository.findById(request.workstationId())
                .orElseThrow(() -> new NotFoundException("Workstation not found: " + request.workstationId()));
        if (operationRepository.findByWorkstationIdAndNameIgnoreCase(workstation.getId(), request.name()).isPresent()) {
            throw new BadRequestException(
                    "Operation \"" + request.name() + "\" already exists for workstation \"" + workstation.getName() + "\".");
        }
        return OperationDto.from(operationRepository.save(new Operation(workstation, request.name())));
    }
}
