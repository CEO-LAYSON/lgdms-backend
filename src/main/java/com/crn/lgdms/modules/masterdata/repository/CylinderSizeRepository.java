package com.crn.lgdms.modules.masterdata.repository;

import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CylinderSizeRepository extends JpaRepository<CylinderSize, String> {

    Optional<CylinderSize> findByName(String name);

    List<CylinderSize> findByIsActiveTrue(Sort sort);

    List<CylinderSize> findByIsActiveTrue();

    @Query("SELECT c FROM CylinderSize c WHERE c.weightKg = :weight")
    Optional<CylinderSize> findByWeightKg(@Param("weight") BigDecimal weight);

    boolean existsByName(String name);

    @Query("SELECT c FROM CylinderSize c ORDER BY c.displayOrder, c.weightKg")
    List<CylinderSize> findAllOrdered();
}
