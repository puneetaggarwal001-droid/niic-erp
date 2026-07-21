package com.niic.erp.production;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Legacy challan numbers (TC-YY-NNNN) reset every calendar year and were
// allocated by counting existing rows with a matching prefix — race-prone,
// like the job number allocator. We keep the per-year reset behavior but make
// allocation atomic via a row-level lock on a small counter table (a real DB
// sequence can't reset itself yearly, so a sequence wasn't an option here).
//
// Known gap: the very first challan of a new year still races on INSERT if two
// requests hit it in the same instant (no row yet to lock) — acceptable given
// how rarely that moment occurs, and no worse than the legacy behavior it replaces.
@Component
public class ChallanNumberService {

    private final ChallanNumberCounterRepository counterRepository;

    public ChallanNumberService(ChallanNumberCounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextChallanNo() {
        int year = LocalDate.now().getYear() % 100;
        ChallanNumberCounter counter = counterRepository.lockByYear(year)
                .orElseGet(() -> counterRepository.save(new ChallanNumberCounter(year, 0)));
        counter.setLastNumber(counter.getLastNumber() + 1);
        return String.format("TC-%02d-%04d", year, counter.getLastNumber());
    }
}
