package com.crn.lgdms.modules.inventory.repository;

import com.crn.lgdms.modules.inventory.domain.entity.EmptyLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmptyLedgerRepository extends JpaRepository<EmptyLedger, String> {

    List<EmptyLedger> findByLocationId(String locationId);

    @Query("SELECT el.runningBalance FROM EmptyLedger el WHERE el.location.id = :locationId " +
        "AND el.cylinderSize.id = :cylinderSizeId " +
        "ORDER BY el.transactionDate DESC LIMIT 1")
    Integer getCurrentEmptyBalance(@Param("locationId") String locationId,
                                   @Param("cylinderSizeId") String cylinderSizeId);

    @Query("SELECT el FROM EmptyLedger el WHERE el.referenceId = :referenceId")
    List<EmptyLedger> findByReferenceId(@Param("referenceId") String referenceId);
}
