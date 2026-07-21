package com.niic.erp.production;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallanNumberCounterRepository extends JpaRepository<ChallanNumberCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ChallanNumberCounter c where c.year = :year")
    Optional<ChallanNumberCounter> lockByYear(@Param("year") Integer year);
}
