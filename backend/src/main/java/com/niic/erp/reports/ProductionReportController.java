package com.niic.erp.reports;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ProductionReportController {

    private final ProductionReportService reportService;

    public ProductionReportController(ProductionReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/wip")
    public WipResponse wip() {
        return reportService.wip();
    }

    @GetMapping("/rejects")
    public RejectsResponse rejects(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.rejects(from, to);
    }
}
