package com.niic.erp.production;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.WorkstationDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/workstations")
public class WorkstationController {

    private final WorkstationRepository workstationRepository;

    public WorkstationController(WorkstationRepository workstationRepository) {
        this.workstationRepository = workstationRepository;
    }

    @GetMapping
    public List<WorkstationDto> list() {
        return workstationRepository.findAll().stream().map(WorkstationDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkstationDto create(@Valid @RequestBody WorkstationDto request) {
        if (workstationRepository.findByNameIgnoreCase(request.name()).isPresent()) {
            throw new BadRequestException("Workstation \"" + request.name() + "\" already exists.");
        }
        Workstation workstation = new Workstation(request.name());
        workstation.setCode(request.code());
        return WorkstationDto.from(workstationRepository.save(workstation));
    }

    @PutMapping("/{id}")
    public WorkstationDto update(@PathVariable Long id, @Valid @RequestBody WorkstationDto request) {
        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workstation not found: " + id));
        workstationRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Workstation \"" + request.name() + "\" already exists.");
                });
        workstation.setName(request.name());
        workstation.setCode(request.code());
        workstation.setActive(request.active());
        return WorkstationDto.from(workstation);
    }
}
