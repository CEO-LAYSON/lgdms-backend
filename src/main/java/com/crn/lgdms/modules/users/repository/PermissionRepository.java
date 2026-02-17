package com.crn.lgdms.modules.users.repository;

import com.crn.lgdms.modules.users.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT p FROM Permission p WHERE p.name IN :names")
    Set<Permission> findByNameIn(@Param("names") Set<String> names);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource")
    Set<Permission> findByResource(@Param("resource") String resource);

    @Query("SELECT p FROM Permission p ORDER BY p.resource, p.action")
    Set<Permission> findAllGrouped();
}
