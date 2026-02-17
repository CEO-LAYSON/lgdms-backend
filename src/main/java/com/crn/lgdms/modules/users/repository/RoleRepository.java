package com.crn.lgdms.modules.users.repository;

import com.crn.lgdms.modules.users.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r WHERE r.isSystemRole = true")
    Set<Role> findSystemRoles();

    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    Set<Role> findByNameIn(@Param("names") Set<String> names);

    @Query("SELECT r.permissions FROM Role r WHERE r.id = :roleId")
    Set<com.crn.lgdms.modules.users.domain.entity.Permission> findPermissionsByRoleId(@Param("roleId") String roleId);
}
