package com.niic.erp.production;

import com.niic.erp.production.dto.ProductionEntryDto;
import com.niic.erp.production.dto.ProductionEntryRequest;
import com.niic.erp.security.CurrentUserProvider;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/production/entries")
public class ProductionEntryController {

    private final ProductionEntryService productionEntryService;
    private final CurrentUserProvider currentUserProvider;

    public ProductionEntryController(ProductionEntryService productionEntryService,
                                      CurrentUserProvider currentUserProvider) {
        this.productionEntryService = productionEntryService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<ProductionEntryDto> list(@RequestParam(required = false) LocalDate date,
                                          @RequestParam(required = false) Long jobId) {
        if (date != null) {
            return productionEntryService.listForDate(date);
        }
        if (jobId != null) {
            return productionEntryService.listForJob(jobId);
        }
        throw new com.niic.erp.common.BadRequestException("Provide either date or jobId.");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductionEntryDto save(@Valid @RequestBody ProductionEntryRequest request) {
        return productionEntryService.save(request, currentUserProvider.require());
    }
}
