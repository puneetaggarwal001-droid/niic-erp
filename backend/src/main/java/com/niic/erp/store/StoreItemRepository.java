package com.niic.erp.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {
    List<StoreItem> findByActiveTrueOrderByName();
    List<StoreItem> findByItemTypeAndActiveTrueOrderByName(ItemType itemType);
    List<StoreItem> findByApprovalStatus(ItemApprovalStatus approvalStatus);
    boolean existsByItemCode(String itemCode);
    long countByItemType(ItemType itemType);
}
