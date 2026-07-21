package com.niic.erp.store;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final StoreItemRepository itemRepository;
    private final StoreService storeService;

    public PurchaseOrderService(PurchaseOrderRepository poRepository, StoreItemRepository itemRepository,
                                StoreService storeService) {
        this.poRepository = poRepository;
        this.itemRepository = itemRepository;
        this.storeService = storeService;
    }

    @Transactional
    public PurchaseOrder create(String supplierName, String notes, List<LineSpec> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BadRequestException("A purchase order needs at least one line.");
        }
        PurchaseOrder po = new PurchaseOrder(supplierName, notes);
        for (LineSpec line : lines) {
            StoreItem item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item " + line.itemId() + " not found."));
            if (line.quantity() == null || line.quantity().signum() <= 0) {
                throw new BadRequestException("PO line quantity must be positive.");
            }
            po.getItems().add(new PurchaseOrderItem(po, item, line.quantity(), line.rate()));
        }
        // Number is derived from the identity assigned on flush.
        PurchaseOrder saved = poRepository.saveAndFlush(po);
        saved.setPoNumber(String.format("PO%04d", saved.getId()));
        return saved;
    }

    public List<PurchaseOrder> list() {
        return poRepository.findByOrderByCreatedAtDesc();
    }

    public PurchaseOrder get(Long id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order " + id + " not found."));
    }

    @Transactional
    public PurchaseOrder markOrdered(Long id) {
        PurchaseOrder po = get(id);
        if (po.getStatus() == PoStatus.PENDING) {
            po.setStatus(PoStatus.ORDERED);
        }
        return po;
    }

    @Transactional
    public PurchaseOrder cancel(Long id) {
        PurchaseOrder po = get(id);
        if (po.getStatus() == PoStatus.RECEIVED) {
            throw new BadRequestException("A fully received PO cannot be cancelled.");
        }
        po.setStatus(PoStatus.CANCELLED);
        return po;
    }

    /** Receive quantities against PO lines; each receipt posts an INWARD stock movement. */
    @Transactional
    public PurchaseOrder receive(Long poId, List<ReceiptSpec> receipts) {
        PurchaseOrder po = get(poId);
        if (po.getStatus() == PoStatus.CANCELLED) {
            throw new BadRequestException("Cannot receive against a cancelled PO.");
        }
        for (ReceiptSpec receipt : receipts) {
            PurchaseOrderItem line = po.getItems().stream()
                    .filter(i -> i.getId().equals(receipt.poItemId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("PO line " + receipt.poItemId() + " not on this PO."));
            BigDecimal qty = receipt.quantity();
            if (qty == null || qty.signum() <= 0) {
                continue;
            }
            if (qty.compareTo(line.outstanding()) > 0) {
                throw new BadRequestException("Receipt exceeds outstanding for " + line.getItem().getItemCode()
                        + " (outstanding " + line.outstanding() + ").");
            }
            line.setReceivedQty(line.getReceivedQty().add(qty));
            storeService.recordTransaction(line.getItem().getId(), null, StockTxnType.INWARD, qty,
                    LocalDate.now(), po.getPoNumber(), "PO receipt");
        }
        boolean allReceived = po.getItems().stream().allMatch(i -> i.outstanding().signum() <= 0);
        po.setStatus(allReceived ? PoStatus.RECEIVED : PoStatus.ORDERED);
        return po;
    }

    public record LineSpec(Long itemId, BigDecimal quantity, BigDecimal rate) {
    }

    public record ReceiptSpec(Long poItemId, BigDecimal quantity) {
    }
}
