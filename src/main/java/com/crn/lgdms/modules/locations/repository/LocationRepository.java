package com.crn.lgdms.modules.locations.repository;

import com.crn.lgdms.common.enums.LocationType;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

    Optional<Location> findByCode(String code);

    List<Location> findByLocationType(LocationType locationType);

    List<Location> findByLocationTypeAndIsActiveTrue(LocationType locationType);

    @Query("SELECT l FROM Location l WHERE " +
        "(:type IS NULL OR l.locationType = :type) AND " +
        "(:activeOnly = false OR l.isActive = true)")
    List<Location> findLocations(@Param("type") LocationType type,
                                 @Param("activeOnly") boolean activeOnly);

    @Query("SELECT l FROM Location l WHERE " +
        "LOWER(l.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(l.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(l.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Location> searchLocations(@Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(l) FROM Location l WHERE l.locationType = :type")
    long countByLocationType(@Param("type") LocationType type);

    @Query("SELECT l FROM Location l WHERE l.parentLocationId = :parentId")
    List<Location> findByParentId(@Param("parentId") String parentId);
}
