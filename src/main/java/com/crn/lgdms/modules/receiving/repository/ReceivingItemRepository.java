package com.crn.lgdms.modules.receiving.repository;

import com.crn.lgdms.modules.receiving.domain.entity.ReceivingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReceivingItemRepository extends JpaRepository<ReceivingItem, String> {

    List<ReceivingItem> findByGoodsReceivingId(String receivingId);

    @Query("SELECT SUM(ri.quantity) FROM ReceivingItem ri WHERE ri.cylinderSize.id = :cylinderSizeId")
    Integer getTotalReceivedQuantityByCylinderSize(@Param("cylinderSizeId") String cylinderSizeId);

    @Query("SELECT SUM(ri.totalPrice) FROM ReceivingItem ri WHERE ri.goodsReceiving.id = :receivingId")
    BigDecimal getTotalPriceByReceivingId(@Param("receivingId") String receivingId);

    @Query("SELECT ri FROM ReceivingItem ri WHERE ri.batchNumber = :batchNumber")
    List<ReceivingItem> findByBatchNumber(@Param("batchNumber") String batchNumber);

    @Query("SELECT ri FROM ReceivingItem ri WHERE ri.expiryDate < CURRENT_DATE AND ri.expiryDate IS NOT NULL")
    List<ReceivingItem> findExpiredItems();
}
