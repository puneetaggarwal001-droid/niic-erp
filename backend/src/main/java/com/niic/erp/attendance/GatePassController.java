package com.niic.erp.attendance;

import com.niic.erp.attendance.dto.GatePassDto;
import com.niic.erp.attendance.dto.GatePassRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gate-passes")
public class GatePassController {

    private final GatePassService gatePassService;

    public GatePassController(GatePassService gatePassService) {
        this.gatePassService = gatePassService;
    }

    @GetMapping
    public List<GatePassDto> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return gatePassService.listByDate(date).stream().map(GatePassDto::from).toList();
    }

    @GetMapping("/config")
    public Map<String, Integer> config() {
        return Map.of("monthlyLimit", gatePassService.monthlyLimit());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GatePassDto issue(@Valid @RequestBody GatePassRequest request) {
        return GatePassDto.from(
                gatePassService.issue(request.employeeId(), request.date(), request.purpose()));
    }
}
