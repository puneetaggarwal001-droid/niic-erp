package com.niic.erp.production;

import com.niic.erp.production.dto.PcRateDto;
import com.niic.erp.production.dto.PcRateRequest;
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
@RequestMapping("/api/production/pc-rates")
public class PcRateController {

    private final PcRateService pcRateService;

    public PcRateController(PcRateService pcRateService) {
        this.pcRateService = pcRateService;
    }

    @GetMapping
    public List<PcRateDto> list(@RequestParam Long workstationId, @RequestParam String styleCode,
                                 @RequestParam String modelNo) {
        return pcRateService.listActiveForGroup(workstationId, styleCode, modelNo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PcRateDto create(@Valid @RequestBody PcRateRequest request) {
        return pcRateService.save(request);
    }
}
