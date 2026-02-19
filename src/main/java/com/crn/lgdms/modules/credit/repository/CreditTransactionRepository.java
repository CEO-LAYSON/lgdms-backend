package com.crn.lgdms.modules.credit.repository;

import com.crn.lgdms.modules.credit.domain.entity.CreditTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, String> {

    List<CreditTransaction> findByCreditAccountId(String creditAccountId);

    Page<CreditTransaction> findByCreditAccountId(String creditAccountId, Pageable pageable);

    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.creditAccount.id = :accountId " +
        "AND ct.transactionDate BETWEEN :startDate AND :endDate")
    List<CreditTransaction> findTransactionsByDateRange(@Param("accountId") String accountId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.sale.id = :saleId")
    Optional<CreditTransaction> findBySaleId(@Param("saleId") String saleId);

    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.payment.id = :paymentId")
    Optional<CreditTransaction> findByPaymentId(@Param("paymentId") String paymentId);

    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.transactionType = 'SALE' " +
        "AND ct.transactionDate < :date AND ct.status = 'COMPLETED'")
    List<CreditTransaction> findUnpaidTransactionsOlderThan(@Param("date") LocalDateTime date);
}
