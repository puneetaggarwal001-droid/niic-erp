package com.niic.erp.store;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BomService {

    private final BomRepository bomRepository;
    private final StoreItemRepository itemRepository;

    public BomService(BomRepository bomRepository, StoreItemRepository itemRepository) {
        this.bomRepository = bomRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Bom createBom(Long outputItemId, String colour, String size, BigDecimal batchQty,
                         List<ComponentSpec> components) {
        StoreItem output = itemRepository.findById(outputItemId)
                .orElseThrow(() -> new NotFoundException("Output item " + outputItemId + " not found."));
        if (output.getItemType() == ItemType.RM) {
            throw new BadRequestException("A raw material cannot have a BOM.");
        }
        if (components == null || components.isEmpty()) {
            throw new BadRequestException("A BOM needs at least one component.");
        }
        Bom bom = new Bom(output, blankToNull(colour), blankToNull(size), batchQty);
        for (ComponentSpec spec : components) {
            StoreItem comp = itemRepository.findById(spec.componentItemId())
                    .orElseThrow(() -> new NotFoundException("Component item " + spec.componentItemId() + " not found."));
            if (comp.getId().equals(output.getId())) {
                throw new BadRequestException("A BOM cannot contain its own output item.");
            }
            if (spec.quantity() == null || spec.quantity().signum() <= 0) {
                throw new BadRequestException("Component quantity must be positive.");
            }
            bom.getComponents().add(new BomComponent(bom, comp, spec.quantity()));
        }
        return bomRepository.save(bom);
    }

    public List<Bom> listBoms(Long outputItemId) {
        return bomRepository.findByOutputItemId(outputItemId);
    }

    @Transactional
    public void deactivate(Long bomId) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new NotFoundException("BOM " + bomId + " not found."));
        bom.setActive(false);
    }

    /**
     * Best active BOM for an item variant. Precedence: exact colour+size, then
     * size-only (generic colour), then fully generic.
     */
    public Optional<Bom> resolveBom(Long itemId, String colour, String size) {
        Bom best = null;
        int bestScore = 0;
        for (Bom bom : bomRepository.findByOutputItemIdAndActiveTrue(itemId)) {
            int score = 0;
            if (eq(bom.getColour(), colour) && eq(bom.getSize(), size)) {
                score = 3;
            } else if (bom.getColour() == null && eq(bom.getSize(), size)) {
                score = 2;
            } else if (bom.getColour() == null && bom.getSize() == null) {
                score = 1;
            }
            if (score > bestScore) {
                bestScore = score;
                best = bom;
            }
        }
        return Optional.ofNullable(best);
    }

    private static boolean eq(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equalsIgnoreCase(b);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    public record ComponentSpec(Long componentItemId, BigDecimal quantity) {
    }
}
