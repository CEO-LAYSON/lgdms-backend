package com.crn.lgdms.modules.transfer.repository;

import com.crn.lgdms.modules.transfer.domain.entity.TransferItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransferItemRepository extends JpaRepository<TransferItem, String> {

    List<TransferItem> findByTransferId(String transferId);

    @Query("SELECT ti FROM TransferItem ti WHERE ti.transfer.fromLocation.id = :locationId")
    List<TransferItem> findOutgoingItems(@Param("locationId") String locationId);

    @Query("SELECT ti FROM TransferItem ti WHERE ti.transfer.toLocation.id = :locationId")
    List<TransferItem> findIncomingItems(@Param("locationId") String locationId);

    @Query("SELECT SUM(ti.quantity) FROM TransferItem ti WHERE ti.cylinderSize.id = :cylinderSizeId " +
        "AND ti.transfer.fromLocation.id = :locationId AND ti.transfer.status = 'COMPLETED'")
    Integer getTotalTransferredOut(@Param("locationId") String locationId,
                                   @Param("cylinderSizeId") String cylinderSizeId);

    @Query("SELECT SUM(ti.emptyReturnedQuantity) FROM TransferItem ti WHERE " +
        "ti.transfer.toLocation.id = :locationId AND ti.transfer.status = 'COMPLETED'")
    Integer getTotalEmptyReturned(@Param("locationId") String locationId);
}
