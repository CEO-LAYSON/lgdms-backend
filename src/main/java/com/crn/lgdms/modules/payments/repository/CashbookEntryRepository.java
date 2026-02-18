package com.crn.lgdms.modules.payments.repository;

import com.crn.lgdms.modules.payments.domain.entity.CashbookEntry;
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
public interface CashbookEntryRepository extends JpaRepository<CashbookEntry, String> {

    List<CashbookEntry> findByLocationId(String locationId);

    Page<CashbookEntry> findByEntryDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT SUM(CASE WHEN c.entryType = 'RECEIPT' THEN c.amount ELSE -c.amount END) " +
        "FROM CashbookEntry c WHERE c.location.id = :locationId " +
        "AND c.entryDate BETWEEN :startDate AND :endDate")
    BigDecimal getNetCashflow(@Param("locationId") String locationId,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);
}
