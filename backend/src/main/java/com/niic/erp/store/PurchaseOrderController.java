package com.niic.erp.store;

import com.niic.erp.store.dto.CreatePoRequest;
import com.niic.erp.store.dto.PurchaseOrderDto;
import com.niic.erp.store.dto.ReceiveRequest;
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
@RequestMapping("/api/store/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    public PurchaseOrderController(PurchaseOrderService poService) {
        this.poService = poService;
    }

    @GetMapping
    public List<PurchaseOrderDto> list() {
        return poService.list().stream().map(PurchaseOrderDto::from).toList();
    }

    @GetMapping("/{id}")
    public PurchaseOrderDto get(@PathVariable Long id) {
        return PurchaseOrderDto.from(poService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderDto create(@Valid @RequestBody CreatePoRequest request) {
        List<PurchaseOrderService.LineSpec> lines = request.lines().stream()
                .map(l -> new PurchaseOrderService.LineSpec(l.itemId(), l.quantity(), l.rate()))
                .toList();
        return PurchaseOrderDto.from(poService.create(request.supplierName(), request.notes(), lines));
    }

    @PostMapping("/{id}/order")
    public PurchaseOrderDto markOrdered(@PathVariable Long id) {
        return PurchaseOrderDto.from(poService.markOrdered(id));
    }

    @PostMapping("/{id}/cancel")
    public PurchaseOrderDto cancel(@PathVariable Long id) {
        return PurchaseOrderDto.from(poService.cancel(id));
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderDto receive(@PathVariable Long id, @Valid @RequestBody ReceiveRequest request) {
        List<PurchaseOrderService.ReceiptSpec> receipts = request.receipts().stream()
                .map(r -> new PurchaseOrderService.ReceiptSpec(r.poItemId(), r.quantity()))
                .toList();
        return PurchaseOrderDto.from(poService.receive(id, receipts));
    }
}
