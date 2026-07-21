package com.niic.erp.production;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Legacy job numbers were a single counter shared across all styles (despite a
// stale source comment claiming "per style" numbering), allocated by reading
// then writing back a plain number with no locking — a real race condition
// under concurrent creators. We keep the same global-sequence *behavior* but
// make allocation atomic via a real DB sequence (job_number_seq, V2 migration).
@Component
public class JobNumberAllocator {

    private final EntityManager entityManager;

    public JobNumberAllocator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // REQUIRES_NEW so the number is burned the moment it's allocated, matching
    // legacy behavior (a rolled-back caller doesn't get the number back either).
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextJobDisplayId(String styleCode) {
        long next = ((Number) entityManager.createNativeQuery("select nextval('job_number_seq')").getSingleResult())
                .longValue();
        return "NIIC / " + styleCode + " / " + String.format("%03d", next);
    }
}
