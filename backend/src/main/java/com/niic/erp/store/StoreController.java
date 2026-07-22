package com.niic.erp.store;

import com.niic.erp.store.dto.CategoryDto;
import com.niic.erp.store.dto.CategoryRequest;
import com.niic.erp.store.dto.ItemDto;
import com.niic.erp.store.dto.ItemRequest;
import com.niic.erp.store.dto.RejectRequest;
import com.niic.erp.store.dto.StockTxnDto;
import com.niic.erp.store.dto.StockTxnRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // ---- Categories ----
    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return storeService.listCategories().stream().map(CategoryDto::from).toList();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody CategoryRequest request) {
        return CategoryDto.from(storeService.addCategory(request.name(), request.code(), request.parentId()));
    }

    // ---- Items ----
    @GetMapping("/items")
    public List<ItemDto> items(@RequestParam(required = false) ItemType type) {
        return storeService.listItems(type);
    }

    @GetMapping("/items/{id}")
    public ItemDto item(@PathVariable Long id) {
        return storeService.getItemDto(id);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('create_item')")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@Valid @RequestBody ItemRequest request) {
        List<StoreService.VariantSpec> variants = request.variants() != null
                ? request.variants().stream().map(v -> new StoreService.VariantSpec(v.colour(), v.size())).toList()
                : List.of();
        return storeService.createItem(request.itemCode(), request.name(), request.itemType(),
                request.unit(), request.categoryId(), request.reorderLevel(), variants);
    }

    // ---- Approvals ----
    @GetMapping("/items/pending-approval")
    public List<ItemDto> pending() {
        return storeService.pendingApprovals();
    }

    @PostMapping("/items/{id}/approve")
    public ItemDto approve(@PathVariable Long id) {
        return storeService.approveItem(id);
    }

    @PostMapping("/items/{id}/reject")
    public ItemDto reject(@PathVariable Long id, @RequestBody(required = false) RejectRequest request) {
        return storeService.rejectItem(id, request != null ? request.reason() : null);
    }

    // ---- Stock ledger ----
    @GetMapping("/items/{id}/ledger")
    public List<StockTxnDto> ledger(@PathVariable Long id) {
        return storeService.ledger(id).stream().map(StockTxnDto::from).toList();
    }

    @PostMapping("/stock")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('store_entry')")
    @ResponseStatus(HttpStatus.CREATED)
    public StockTxnDto recordStock(@Valid @RequestBody StockTxnRequest request) {
        return StockTxnDto.from(storeService.recordTransaction(request.itemId(), request.variantId(),
                request.txnType(), request.quantity(), request.date(), request.reference(), request.note()));
    }
}
