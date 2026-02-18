package com.crn.lgdms.modules.transfer.repository;

import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.modules.transfer.domain.entity.TransferRequest;
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
public interface TransferRequestRepository extends JpaRepository<TransferRequest, String> {

    Optional<TransferRequest> findByRequestNumber(String requestNumber);

    boolean existsByRequestNumber(String requestNumber);

    List<TransferRequest> findByFromLocationId(String locationId);

    List<TransferRequest> findByToLocationId(String locationId);

    Page<TransferRequest> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT tr FROM TransferRequest tr WHERE tr.toLocation.id = :locationId AND tr.status = 'PENDING'")
    List<TransferRequest> findPendingRequestsForLocation(@Param("locationId") String locationId);

    @Query("SELECT tr FROM TransferRequest tr WHERE tr.requestDate BETWEEN :startDate AND :endDate")
    List<TransferRequest> findByDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT tr FROM TransferRequest tr WHERE " +
        "(:searchTerm IS NULL OR " +
        "LOWER(tr.requestNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(tr.requestedBy) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<TransferRequest> searchRequests(@Param("searchTerm") String searchTerm, Pageable pageable);
}
