package com.niic.erp.store;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.Workstation;
import com.niic.erp.production.WorkstationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequisitionService {

    private final MaterialRequisitionRepository requisitionRepository;
    private final WorkstationRepository workstationRepository;
    private final StoreItemRepository itemRepository;
    private final StoreService storeService;

    public RequisitionService(MaterialRequisitionRepository requisitionRepository,
                              WorkstationRepository workstationRepository, StoreItemRepository itemRepository,
                              StoreService storeService) {
        this.requisitionRepository = requisitionRepository;
        this.workstationRepository = workstationRepository;
        this.itemRepository = itemRepository;
        this.storeService = storeService;
    }

    @Transactional
    public MaterialRequisition create(Long workstationId, String notes, List<LineSpec> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BadRequestException("A requisition needs at least one line.");
        }
        Workstation ws = workstationRepository.findById(workstationId)
                .orElseThrow(() -> new NotFoundException("Workstation " + workstationId + " not found."));
        MaterialRequisition req = new MaterialRequisition(ws, notes);
        for (LineSpec line : lines) {
            StoreItem item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item " + line.itemId() + " not found."));
            if (line.quantity() == null || line.quantity().signum() <= 0) {
                throw new BadRequestException("Requested quantity must be positive.");
            }
            req.getItems().add(new MaterialRequisitionItem(req, item, line.quantity()));
        }
        MaterialRequisition saved = requisitionRepository.saveAndFlush(req);
        saved.setMrNumber(String.format("MR-%04d", saved.getId()));
        return saved;
    }

    /** Fulfil (issue) quantities against requisition lines; each posts an ISSUE movement. */
    @Transactional
    public MaterialRequisition fulfill(Long reqId, List<FulfilSpec> fulfilments) {
        MaterialRequisition req = get(reqId);
        for (FulfilSpec f : fulfilments) {
            MaterialRequisitionItem line = req.getItems().stream()
                    .filter(i -> i.getId().equals(f.reqItemId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Line " + f.reqItemId() + " not on this requisition."));
            BigDecimal qty = f.quantity();
            if (qty == null || qty.signum() <= 0) {
                continue;
            }
            if (qty.compareTo(line.outstanding()) > 0) {
                throw new BadRequestException("Fulfilment exceeds outstanding for " + line.getItem().getItemCode()
                        + " (outstanding " + line.outstanding() + ").");
            }
            storeService.recordTransaction(line.getItem().getId(), null, StockTxnType.ISSUE, qty,
                    LocalDate.now(), req.getMrNumber(), "Requisition fulfilment");
            line.setFulfilledQty(line.getFulfilledQty().add(qty));
        }
        boolean allDone = req.getItems().stream().allMatch(i -> i.outstanding().signum() <= 0);
        boolean anyDone = req.getItems().stream().anyMatch(i -> i.getFulfilledQty().signum() > 0);
        req.setStatus(allDone ? RequisitionStatus.FULFILLED
                : anyDone ? RequisitionStatus.PARTIAL : RequisitionStatus.PENDING);
        return req;
    }

    public MaterialRequisition get(Long id) {
        return requisitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Requisition " + id + " not found."));
    }

    public List<MaterialRequisition> list() {
        return requisitionRepository.findByOrderByCreatedAtDesc();
    }

    public record LineSpec(Long itemId, BigDecimal quantity) {
    }

    public record FulfilSpec(Long reqItemId, BigDecimal quantity) {
    }
}
