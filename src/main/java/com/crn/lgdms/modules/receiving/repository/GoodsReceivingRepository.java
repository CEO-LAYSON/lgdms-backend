package com.crn.lgdms.modules.receiving.repository;

import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.modules.receiving.domain.entity.GoodsReceiving;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceivingRepository extends JpaRepository<GoodsReceiving, String> {

    Optional<GoodsReceiving> findByReceivingNumber(String receivingNumber);

    boolean existsByReceivingNumber(String receivingNumber);

    List<GoodsReceiving> findBySupplierId(String supplierId);

    List<GoodsReceiving> findByLocationId(String locationId);

    Page<GoodsReceiving> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT gr FROM GoodsReceiving gr WHERE gr.receivingDate BETWEEN :startDate AND :endDate")
    List<GoodsReceiving> findByDateRange(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT gr FROM GoodsReceiving gr WHERE " +
        "(:searchTerm IS NULL OR " +
        "LOWER(gr.receivingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(gr.invoiceNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(gr.deliveryNoteNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<GoodsReceiving> searchReceivings(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT SUM(gr.totalAmount) FROM GoodsReceiving gr WHERE " +
        "gr.receivingDate BETWEEN :startDate AND :endDate AND gr.status = 'COMPLETED'")
    BigDecimal getTotalReceivingAmount(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT gr FROM GoodsReceiving gr WHERE gr.status = 'PENDING' AND gr.receivingDate < :date")
    List<GoodsReceiving> findPendingOlderThan(@Param("date") LocalDate date);
}
