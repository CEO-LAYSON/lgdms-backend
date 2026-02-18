package com.crn.lgdms.modules.sales.repository;

import com.crn.lgdms.common.enums.PaymentMethod;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.modules.sales.domain.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, String> {

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Sale> findByLocationId(String locationId);

    List<Sale> findByCustomerId(String customerId);

    Page<Sale> findBySalesPersonId(String salesPersonId, Pageable pageable);

    Page<Sale> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    List<Sale> findByDateRange(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Sale s WHERE s.saleTime BETWEEN :startTime AND :endTime")
    Page<Sale> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime,
                               Pageable pageable);

    @Query("SELECT s FROM Sale s WHERE s.isCreditSale = true AND s.balanceDue > 0")
    Page<Sale> findOutstandingCreditSales(Pageable pageable);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate = :date")
    BigDecimal getTotalSalesForDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesForPeriod(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT s.paymentMethod, COUNT(s), SUM(s.totalAmount) FROM Sale s " +
        "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
        "GROUP BY s.paymentMethod")
    List<Object[]> getSalesSummaryByPaymentMethod(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}
