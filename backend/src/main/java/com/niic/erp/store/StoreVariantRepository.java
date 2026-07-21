package com.niic.erp.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreVariantRepository extends JpaRepository<StoreVariant, Long> {
    List<StoreVariant> findByItemId(Long itemId);
}
