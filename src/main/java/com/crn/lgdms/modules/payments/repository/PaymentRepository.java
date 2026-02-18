package com.crn.lgdms.modules.payments.repository;

import com.crn.lgdms.common.enums.PaymentMethod;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.modules.payments.domain.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByCustomerId(String customerId);

    Page<Payment> findByLocationId(String locationId, Pageable pageable);

    Page<Payment> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    Page<Payment> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaymentsForPeriod(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.paymentMethod, COUNT(p), SUM(p.amount) FROM Payment p " +
        "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
        "GROUP BY p.paymentMethod")
    List<Object[]> getPaymentSummaryByMethod(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}
