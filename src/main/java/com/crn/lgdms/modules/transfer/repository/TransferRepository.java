package com.crn.lgdms.modules.transfer.repository;

import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.modules.transfer.domain.entity.Transfer;
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
public interface TransferRepository extends JpaRepository<Transfer, String> {

    Optional<Transfer> findByTransferNumber(String transferNumber);

    Optional<Transfer> findByTransferRequestId(String transferRequestId);

    boolean existsByTransferNumber(String transferNumber);

    List<Transfer> findByFromLocationId(String locationId);

    List<Transfer> findByToLocationId(String locationId);

    Page<Transfer> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.transferDate BETWEEN :startDate AND :endDate")
    List<Transfer> findByDateRange(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transfer t WHERE t.status = 'IN_TRANSIT' AND t.dispatchedAt < :date")
    List<Transfer> findOverdueTransfers(@Param("date") java.time.LocalDateTime date);

    @Query("SELECT t FROM Transfer t WHERE " +
        "(:fromLocationId IS NULL OR t.fromLocation.id = :fromLocationId) AND " +
        "(:toLocationId IS NULL OR t.toLocation.id = :toLocationId) AND " +
        "(:status IS NULL OR t.status = :status)")
    Page<Transfer> findTransfers(@Param("fromLocationId") String fromLocationId,
                                 @Param("toLocationId") String toLocationId,
                                 @Param("status") TransactionStatus status,
                                 Pageable pageable);
}
