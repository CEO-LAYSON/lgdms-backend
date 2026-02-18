package com.crn.lgdms.modules.sales.repository;

import com.crn.lgdms.modules.sales.domain.entity.InvoiceNumberSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface InvoiceNumberSequenceRepository extends JpaRepository<InvoiceNumberSequence, String> {

    Optional<InvoiceNumberSequence> findByLocationIdAndSaleDate(String locationId, LocalDate saleDate);

    @Query("SELECT COALESCE(MAX(i.sequenceNumber), 0) FROM InvoiceNumberSequence i " +
        "WHERE i.locationId = :locationId AND i.saleDate = :saleDate")
    Integer getMaxSequenceForDate(@Param("locationId") String locationId,
                                  @Param("saleDate") LocalDate saleDate);
}
