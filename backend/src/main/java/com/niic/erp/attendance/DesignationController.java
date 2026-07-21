package com.niic.erp.attendance;

import com.niic.erp.attendance.dto.DesignationDto;
import com.niic.erp.common.BadRequestException;
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
@RequestMapping("/api/designations")
public class DesignationController {

    private final DesignationRepository designationRepository;

    public DesignationController(DesignationRepository designationRepository) {
        this.designationRepository = designationRepository;
    }

    @GetMapping
    public List<DesignationDto> list() {
        return designationRepository.findAll().stream().map(DesignationDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DesignationDto create(@Valid @RequestBody DesignationDto request) {
        if (designationRepository.findByName(request.name()).isPresent()) {
            throw new BadRequestException("Designation \"" + request.name() + "\" already exists.");
        }
        return DesignationDto.from(designationRepository.save(new Designation(request.name())));
    }
}
