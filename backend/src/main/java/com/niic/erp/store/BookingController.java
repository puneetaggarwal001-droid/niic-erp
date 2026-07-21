package com.niic.erp.store;

import com.niic.erp.store.dto.BookingDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingDto> byJob(@RequestParam Long jobId) {
        return bookingService.listByJob(jobId).stream().map(BookingDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<BookingDto> bookForJob(@RequestParam Long jobId) {
        return bookingService.bookForJob(jobId).stream().map(BookingDto::from).toList();
    }
}
