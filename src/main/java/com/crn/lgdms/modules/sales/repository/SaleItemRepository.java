package com.crn.lgdms.modules.sales.repository;

import com.crn.lgdms.modules.sales.domain.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, String> {

    List<SaleItem> findBySaleId(String saleId);

    @Query("SELECT si FROM SaleItem si WHERE si.sale.location.id = :locationId")
    List<SaleItem> findByLocationId(@Param("locationId") String locationId);

    @Query("SELECT si.cylinderSize.name, SUM(si.quantity) FROM SaleItem si " +
        "WHERE si.sale.saleDate BETWEEN :startDate AND :endDate " +
        "GROUP BY si.cylinderSize.name")
    List<Object[]> getSalesQuantityByCylinderSize(@Param("startDate") java.time.LocalDate startDate,
                                                  @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT si.productType, SUM(si.quantity) FROM SaleItem si " +
        "WHERE si.sale.saleDate BETWEEN :startDate AND :endDate " +
        "GROUP BY si.productType")
    List<Object[]> getSalesQuantityByProductType(@Param("startDate") java.time.LocalDate startDate,
                                                 @Param("endDate") java.time.LocalDate endDate);
}
