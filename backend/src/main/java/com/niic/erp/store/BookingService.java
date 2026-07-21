package com.niic.erp.store;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.Job;
import com.niic.erp.production.JobRepository;
import com.niic.erp.store.dto.MrpResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stock reservations for a job. Booking a job explodes its BOM (via {@link MrpService})
 * and reserves the required material so it isn't double-committed to another job.
 */
@Service
public class BookingService {

    private final StockBookingRepository bookingRepository;
    private final JobRepository jobRepository;
    private final StoreItemRepository itemRepository;
    private final MrpService mrpService;

    public BookingService(StockBookingRepository bookingRepository, JobRepository jobRepository,
                          StoreItemRepository itemRepository, MrpService mrpService) {
        this.bookingRepository = bookingRepository;
        this.jobRepository = jobRepository;
        this.itemRepository = itemRepository;
        this.mrpService = mrpService;
    }

    @Transactional
    public List<StockBooking> bookForJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job " + jobId + " not found."));
        if (!bookingRepository.findByJobId(jobId).isEmpty()) {
            throw new BadRequestException("This job already has stock bookings.");
        }
        MrpResponse mrp = mrpService.explode(jobId);
        List<StockBooking> created = new ArrayList<>();
        for (MrpResponse.Line line : mrp.lines()) {
            BigDecimal qty = line.required();
            if (qty == null || qty.signum() <= 0) {
                continue;
            }
            StoreItem item = itemRepository.findById(line.itemId()).orElse(null);
            if (item == null) {
                continue;
            }
            created.add(bookingRepository.save(new StockBooking(item, null, job, qty)));
        }
        return created;
    }

    public List<StockBooking> listByJob(Long jobId) {
        return bookingRepository.findByJobId(jobId);
    }
}
