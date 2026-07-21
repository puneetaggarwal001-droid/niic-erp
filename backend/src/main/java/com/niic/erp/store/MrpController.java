package com.niic.erp.store;

import com.niic.erp.store.dto.MrpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/mrp")
public class MrpController {

    private final MrpService mrpService;

    public MrpController(MrpService mrpService) {
        this.mrpService = mrpService;
    }

    @GetMapping
    public MrpResponse explode(@RequestParam Long jobId) {
        return mrpService.explode(jobId);
    }
}
