package com.niic.erp.store;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.security.CurrentUserProvider;
import com.niic.erp.store.dto.ItemDto;
import com.niic.erp.user.Role;
import com.niic.erp.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {

    private final StoreItemRepository itemRepository;
    private final StoreCategoryRepository categoryRepository;
    private final StoreVariantRepository variantRepository;
    private final StockTransactionRepository txnRepository;
    private final StockBookingRepository bookingRepository;
    private final CurrentUserProvider currentUserProvider;

    public StoreService(StoreItemRepository itemRepository, StoreCategoryRepository categoryRepository,
                        StoreVariantRepository variantRepository, StockTransactionRepository txnRepository,
                        StockBookingRepository bookingRepository, CurrentUserProvider currentUserProvider) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.txnRepository = txnRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // ---- Categories ------------------------------------------------------

    @Transactional
    public StoreCategory addCategory(String name, String code, Long parentId) {
        if (name == null || name.isBlank() || code == null || code.isBlank()) {
            throw new BadRequestException("Category name and code are required.");
        }
        if (categoryRepository.existsByCode(code)) {
            throw new BadRequestException("Category code already exists: " + code);
        }
        StoreCategory parent = parentId != null
                ? categoryRepository.findById(parentId)
                        .orElseThrow(() -> new NotFoundException("Parent category " + parentId + " not found."))
                : null;
        return categoryRepository.save(new StoreCategory(name.trim(), code.trim(), parent));
    }

    public List<StoreCategory> listCategories() {
        return categoryRepository.findByOrderByName();
    }

    // ---- Items -----------------------------------------------------------

    @Transactional
    public ItemDto createItem(String itemCode, String name, ItemType itemType, String unit, Long categoryId,
                              BigDecimal reorderLevel, List<VariantSpec> variants) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Item name is required.");
        }
        StoreCategory category = categoryId != null
                ? categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new NotFoundException("Category " + categoryId + " not found."))
                : null;

        String code = (itemCode != null && !itemCode.isBlank())
                ? itemCode.trim()
                : generateItemCode(itemType, category);
        if (itemRepository.existsByItemCode(code)) {
            throw new BadRequestException("Item code already exists: " + code);
        }

        StoreItem item = new StoreItem(code, name.trim(), itemType, unit, category, reorderLevel);

        // Store users raise items for approval; admins / approve_item holders create
        // them already approved.
        User user = currentUserProvider.require();
        item.setApprovalStatus(canApprove(user) ? ItemApprovalStatus.APPROVED : ItemApprovalStatus.PENDING_APPROVAL);

        if (itemType == ItemType.FG && variants != null) {
            for (VariantSpec v : variants) {
                item.getVariants().add(new StoreVariant(item, v.colour(), v.size()));
            }
        }
        StoreItem saved = itemRepository.save(item);
        return ItemDto.from(saved, onHand(saved.getId()));
    }

    private String generateItemCode(ItemType type, StoreCategory category) {
        long seq = itemRepository.countByItemType(type) + 1;
        String padded = String.format("%03d", seq);
        return switch (type) {
            case FG -> "FG" + padded;
            case SFG -> (category != null ? category.getCode() : "SFG") + "-" + padded;
            case RM -> "RM-" + padded;
        };
    }

    public StoreItem getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item " + id + " not found."));
    }

    // DTO-returning reads run inside the transaction so the lazy `variants`
    // collection can be initialized during mapping (see JobService for the same
    // pattern) — mapping in the controller after the session closes would throw
    // LazyInitializationException.
    @Transactional(readOnly = true)
    public List<ItemDto> listItems(ItemType type) {
        List<StoreItem> items = type != null
                ? itemRepository.findByItemTypeAndActiveTrueOrderByName(type)
                : itemRepository.findByActiveTrueOrderByName();
        return items.stream().map(i -> ItemDto.from(i, onHand(i.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public ItemDto getItemDto(Long id) {
        StoreItem item = getItem(id);
        return ItemDto.from(item, onHand(id));
    }

    @Transactional(readOnly = true)
    public List<ItemDto> pendingApprovals() {
        return itemRepository.findByApprovalStatus(ItemApprovalStatus.PENDING_APPROVAL)
                .stream().map(i -> ItemDto.from(i, onHand(i.getId()))).toList();
    }

    @Transactional
    public ItemDto approveItem(Long id) {
        StoreItem item = getItem(id);
        item.setApprovalStatus(ItemApprovalStatus.APPROVED);
        item.setRejectionReason(null);
        return ItemDto.from(item, onHand(id));
    }

    @Transactional
    public ItemDto rejectItem(Long id, String reason) {
        StoreItem item = getItem(id);
        item.setApprovalStatus(ItemApprovalStatus.REJECTED);
        item.setRejectionReason(reason);
        return ItemDto.from(item, onHand(id));
    }

    // ---- Stock ledger ----------------------------------------------------

    public BigDecimal onHand(Long itemId) {
        return txnRepository.onHand(itemId);
    }

    /** Reserved against open bookings. */
    public BigDecimal booked(Long itemId) {
        return bookingRepository.bookedForItem(itemId);
    }

    /** Free-to-use stock: on-hand minus outstanding bookings, floored at zero. */
    public BigDecimal available(Long itemId) {
        BigDecimal avail = onHand(itemId).subtract(booked(itemId));
        return avail.signum() < 0 ? BigDecimal.ZERO : avail;
    }

    @Transactional
    public StockTransaction recordTransaction(Long itemId, Long variantId, StockTxnType txnType,
                                              BigDecimal magnitude, LocalDate date, String reference, String note) {
        StoreItem item = getItem(itemId);
        if (item.getApprovalStatus() != ItemApprovalStatus.APPROVED) {
            throw new BadRequestException("Item is not approved; cannot transact stock.");
        }
        if (magnitude == null || magnitude.signum() == 0) {
            throw new BadRequestException("Quantity must be non-zero.");
        }
        StoreVariant variant = variantId != null
                ? variantRepository.findById(variantId)
                        .orElseThrow(() -> new NotFoundException("Variant " + variantId + " not found."))
                : null;

        // Convert the (positive) magnitude into a signed delta. ADJUST keeps the
        // caller's sign so it can correct in either direction.
        BigDecimal signed = switch (txnType) {
            case INWARD, RETURN -> magnitude.abs();
            case ISSUE, REJECT -> magnitude.abs().negate();
            case ADJUST -> magnitude;
        };

        if (signed.signum() < 0) {
            BigDecimal current = variantId != null
                    ? txnRepository.onHandForVariant(itemId, variantId)
                    : txnRepository.onHand(itemId);
            if (current.add(signed).signum() < 0) {
                throw new BadRequestException("Insufficient stock: on-hand " + current + ", requested " + signed.abs());
            }
        }

        LocalDate txnDate = date != null ? date : LocalDate.now();
        return txnRepository.save(new StockTransaction(
                item, variant, txnType, signed, txnDate, reference, note, currentUserProvider.require()));
    }

    public List<StockTransaction> ledger(Long itemId) {
        return txnRepository.findByItemIdOrderByCreatedAtDesc(itemId);
    }

    private static boolean canApprove(User user) {
        return user.getRole() == Role.ADMIN || user.getRights().contains("approve_item");
    }

    /** Lightweight FG variant spec used when creating an item. */
    public record VariantSpec(String colour, String size) {
    }
}
