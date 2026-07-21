package com.niic.erp.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByItemIdOrderByCreatedAtDesc(Long itemId);

    @Query("select coalesce(sum(t.quantity), 0) from StockTransaction t where t.item.id = :itemId")
    java.math.BigDecimal onHand(@Param("itemId") Long itemId);

    @Query("select coalesce(sum(t.quantity), 0) from StockTransaction t "
            + "where t.item.id = :itemId and (:variantId is null and t.variant is null or t.variant.id = :variantId)")
    java.math.BigDecimal onHandForVariant(@Param("itemId") Long itemId, @Param("variantId") Long variantId);
}
