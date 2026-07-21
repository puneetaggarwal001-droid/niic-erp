package com.niic.erp.store;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.Job;
import com.niic.erp.production.JobRepository;
import com.niic.erp.production.Workstation;
import com.niic.erp.production.WorkstationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssuanceService {

    private final IssuanceChallanRepository challanRepository;
    private final WorkstationRepository workstationRepository;
    private final JobRepository jobRepository;
    private final StoreItemRepository itemRepository;
    private final StoreVariantRepository variantRepository;
    private final StockBookingRepository bookingRepository;
    private final StoreService storeService;

    public IssuanceService(IssuanceChallanRepository challanRepository, WorkstationRepository workstationRepository,
                           JobRepository jobRepository, StoreItemRepository itemRepository,
                           StoreVariantRepository variantRepository, StockBookingRepository bookingRepository,
                           StoreService storeService) {
        this.challanRepository = challanRepository;
        this.workstationRepository = workstationRepository;
        this.jobRepository = jobRepository;
        this.itemRepository = itemRepository;
        this.variantRepository = variantRepository;
        this.bookingRepository = bookingRepository;
        this.storeService = storeService;
    }

    @Transactional
    public IssuanceChallan issue(Long jobId, Long workstationId, String notes, List<LineSpec> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BadRequestException("An issuance needs at least one line.");
        }
        Workstation ws = workstationRepository.findById(workstationId)
                .orElseThrow(() -> new NotFoundException("Workstation " + workstationId + " not found."));
        Job job = jobId != null
                ? jobRepository.findById(jobId)
                        .orElseThrow(() -> new NotFoundException("Job " + jobId + " not found."))
                : null;

        IssuanceChallan challan = new IssuanceChallan(job, ws, notes);
        for (LineSpec line : lines) {
            StoreItem item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item " + line.itemId() + " not found."));
            StoreVariant variant = line.variantId() != null
                    ? variantRepository.findById(line.variantId())
                            .orElseThrow(() -> new NotFoundException("Variant " + line.variantId() + " not found."))
                    : null;
            if (line.quantity() == null || line.quantity().signum() <= 0) {
                throw new BadRequestException("Issue quantity must be positive.");
            }
            // Post the stock movement (validates availability of on-hand).
            storeService.recordTransaction(item.getId(), line.variantId(), StockTxnType.ISSUE, line.quantity(),
                    LocalDate.now(), "ISS", notes);
            // Draw down the job's reservation for this item, if any.
            if (job != null) {
                drawDownBooking(job.getId(), item.getId(), line.quantity());
            }
            challan.getItems().add(new IssuanceChallanItem(challan, item, variant, line.quantity()));
        }
        IssuanceChallan saved = challanRepository.saveAndFlush(challan);
        saved.setIssNumber(String.format("ISS-%04d", saved.getId()));
        return saved;
    }

    private void drawDownBooking(Long jobId, Long itemId, BigDecimal qty) {
        BigDecimal remaining = qty;
        for (StockBooking booking : bookingRepository.findByJobId(jobId)) {
            if (remaining.signum() <= 0) {
                break;
            }
            if (!booking.getItem().getId().equals(itemId) || booking.getStatus() != BookingStatus.OPEN) {
                continue;
            }
            BigDecimal take = remaining.min(booking.outstanding());
            if (take.signum() <= 0) {
                continue;
            }
            booking.setIssuedQty(booking.getIssuedQty().add(take));
            if (booking.outstanding().signum() <= 0) {
                booking.setStatus(BookingStatus.CLOSED);
            }
            remaining = remaining.subtract(take);
        }
    }

    public List<IssuanceChallan> list() {
        return challanRepository.findByOrderByCreatedAtDesc();
    }

    public record LineSpec(Long itemId, Long variantId, BigDecimal quantity) {
    }
}
