package com.niic.erp.store;

import com.niic.erp.store.dto.BomDto;
import com.niic.erp.store.dto.BomRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/boms")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    @GetMapping
    public List<BomDto> list(@RequestParam Long outputItemId) {
        return bomService.listBoms(outputItemId).stream().map(BomDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BomDto create(@Valid @RequestBody BomRequest request) {
        List<BomService.ComponentSpec> comps = request.components().stream()
                .map(c -> new BomService.ComponentSpec(c.componentItemId(), c.quantity()))
                .toList();
        return BomDto.from(bomService.createBom(
                request.outputItemId(), request.colour(), request.size(), request.batchQty(), comps));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        bomService.deactivate(id);
    }
}
