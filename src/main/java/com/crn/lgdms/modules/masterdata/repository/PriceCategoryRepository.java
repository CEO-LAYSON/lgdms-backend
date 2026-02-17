package com.crn.lgdms.modules.masterdata.repository;

import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.domain.entity.PriceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceCategoryRepository extends JpaRepository<PriceCategory, String> {

    List<PriceCategory> findByCylinderSizeAndIsActiveTrue(CylinderSize cylinderSize);

    @Query("SELECT p FROM PriceCategory p WHERE " +
        "p.cylinderSize.id = :cylinderSizeId AND " +
        "p.productType = :productType AND " +
        "p.effectiveFrom <= :date AND " +
        "(p.effectiveTo IS NULL OR p.effectiveTo >= :date) AND " +
        "p.isActive = true")
    Optional<PriceCategory> findCurrentPrice(
        @Param("cylinderSizeId") String cylinderSizeId,
        @Param("productType") ProductType productType,
        @Param("date") LocalDate date);

    @Query("SELECT p FROM PriceCategory p WHERE " +
        "(:name IS NULL OR p.name LIKE %:name%) AND " +
        "(:cylinderSizeId IS NULL OR p.cylinderSize.id = :cylinderSizeId) AND " +
        "(:productType IS NULL OR p.productType = :productType) AND " +
        "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<PriceCategory> searchPriceCategories(
        @Param("name") String name,
        @Param("cylinderSizeId") String cylinderSizeId,
        @Param("productType") ProductType productType,
        @Param("isActive") Boolean isActive,
        Pageable pageable);

    @Query("SELECT p FROM PriceCategory p WHERE p.effectiveTo < :date AND p.isActive = true")
    List<PriceCategory> findExpiredPrices(@Param("date") LocalDate date);
}
