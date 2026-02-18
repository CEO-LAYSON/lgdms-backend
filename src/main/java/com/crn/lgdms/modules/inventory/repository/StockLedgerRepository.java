package com.crn.lgdms.modules.inventory.repository;

import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, String> {

    List<StockLedger> findByLocationId(String locationId);

    List<StockLedger> findByReferenceId(String referenceId);

    @Query("SELECT sl FROM StockLedger sl WHERE sl.location.id = :locationId " +
        "AND sl.cylinderSize.id = :cylinderSizeId " +
        "AND sl.productType = :productType " +
        "ORDER BY sl.transactionDate DESC")
    List<StockLedger> findStockMovements(@Param("locationId") String locationId,
                                         @Param("cylinderSizeId") String cylinderSizeId,
                                         @Param("productType") ProductType productType);

    @Query("SELECT sl FROM StockLedger sl WHERE sl.transactionDate BETWEEN :startDate AND :endDate")
    Page<StockLedger> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    @Query("SELECT sl.runningBalance FROM StockLedger sl WHERE sl.location.id = :locationId " +
        "AND sl.cylinderSize.id = :cylinderSizeId " +
        "AND sl.productType = :productType " +
        "ORDER BY sl.transactionDate DESC LIMIT 1")
    Integer getCurrentStock(@Param("locationId") String locationId,
                            @Param("cylinderSizeId") String cylinderSizeId,
                            @Param("productType") ProductType productType);

    @Query("SELECT SUM(sl.totalValue) FROM StockLedger sl WHERE sl.location.id = :locationId " +
        "AND sl.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalStockValue(@Param("locationId") String locationId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
}
