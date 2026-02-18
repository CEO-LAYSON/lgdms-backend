package com.crn.lgdms.modules.credit.repository;

import com.crn.lgdms.modules.credit.domain.entity.CreditAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditAccountRepository extends JpaRepository<CreditAccount, String> {

    Optional<CreditAccount> findByAccountNumber(String accountNumber);

    Optional<CreditAccount> findByCustomerId(String customerId);

    Optional<CreditAccount> findByLocationId(String locationId);

    List<CreditAccount> findByAccountType(CreditAccount.CreditAccountType accountType);

    @Query("SELECT ca FROM CreditAccount ca WHERE ca.currentBalance > ca.creditLimit")
    List<CreditAccount> findAccountsOverLimit();

    @Query("SELECT ca FROM CreditAccount ca WHERE ca.currentBalance > 0")
    Page<CreditAccount> findAccountsWithBalance(Pageable pageable);

    @Query("SELECT ca FROM CreditAccount ca WHERE " +
        "(:searchTerm IS NULL OR " +
        "LOWER(ca.accountNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(ca.customer.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(ca.location.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CreditAccount> searchCreditAccounts(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT SUM(ca.currentBalance) FROM CreditAccount ca")
    BigDecimal getTotalOutstandingCredit();

    @Query("SELECT ca.accountType, SUM(ca.currentBalance) FROM CreditAccount ca " +
        "GROUP BY ca.accountType")
    List<Object[]> getOutstandingByAccountType();
}
