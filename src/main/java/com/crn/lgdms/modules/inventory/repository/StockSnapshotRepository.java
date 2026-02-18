package com.crn.lgdms.modules.inventory.repository;

import com.crn.lgdms.modules.inventory.domain.entity.StockSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockSnapshotRepository extends JpaRepository<StockSnapshot, String> {

    List<StockSnapshot> findBySnapshotDate(LocalDate date);

    @Query("SELECT ss FROM StockSnapshot ss WHERE ss.location.id = :locationId AND ss.snapshotDate = :date")
    List<StockSnapshot> findByLocationAndDate(@Param("locationId") String locationId,
                                              @Param("date") LocalDate date);

    @Query("SELECT ss FROM StockSnapshot ss WHERE ss.snapshotDate = " +
        "(SELECT MAX(s.snapshotDate) FROM StockSnapshot s)")
    List<StockSnapshot> findLatestSnapshot();
}
