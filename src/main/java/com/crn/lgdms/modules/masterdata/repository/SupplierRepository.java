package com.crn.lgdms.modules.masterdata.repository;

import com.crn.lgdms.modules.masterdata.domain.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {

    Optional<Supplier> findByName(String name);

    Optional<Supplier> findByCode(String code);

    List<Supplier> findByIsActiveTrue();

    @Query("SELECT s FROM Supplier s WHERE " +
        "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
        "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
