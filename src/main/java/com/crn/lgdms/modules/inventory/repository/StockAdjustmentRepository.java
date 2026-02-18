package com.crn.lgdms.modules.inventory.repository;

import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.modules.inventory.domain.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, String> {

    Optional<StockAdjustment> findByAdjustmentNumber(String adjustmentNumber);

    List<StockAdjustment> findByLocationId(String locationId);

    List<StockAdjustment> findByStatus(TransactionStatus status);

    @Query("SELECT sa FROM StockAdjustment sa WHERE sa.status = 'PENDING' AND sa.createdAt < :date")
    List<StockAdjustment> findPendingOlderThan(@Param("date") java.time.LocalDateTime date);
}
