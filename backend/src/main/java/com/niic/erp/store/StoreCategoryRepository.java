package com.niic.erp.store;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {
    List<StoreCategory> findByOrderByName();
    Optional<StoreCategory> findByCode(String code);
    boolean existsByCode(String code);
}
