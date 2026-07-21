package com.niic.erp.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockBookingRepository extends JpaRepository<StockBooking, Long> {

    List<StockBooking> findByJobId(Long jobId);

    List<StockBooking> findByItemIdAndStatus(Long itemId, BookingStatus status);

    // Outstanding reservation (booked minus already-issued) across all OPEN bookings
    // of an item — the amount to subtract from on-hand when computing availability.
    @Query("select coalesce(sum(b.bookedQty - b.issuedQty), 0) from StockBooking b "
            + "where b.item.id = :itemId and b.status = com.niic.erp.store.BookingStatus.OPEN")
    java.math.BigDecimal bookedForItem(@Param("itemId") Long itemId);
}
