package com.crn.lgdms.modules.users.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.users.domain.entity.Permission;
import com.crn.lgdms.modules.users.domain.entity.Role;
import com.crn.lgdms.modules.users.dto.request.AssignPermissionRequest;
import com.crn.lgdms.modules.users.dto.request.CreateRoleRequest;
import com.crn.lgdms.modules.users.dto.request.UpdateRoleRequest;
import com.crn.lgdms.modules.users.dto.response.RoleResponse;
import com.crn.lgdms.modules.users.dto.mapper.RoleMapper;
import com.crn.lgdms.modules.users.repository.PermissionRepository;
import com.crn.lgdms.modules.users.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        log.info("Creating new role: {}", request.getName());

        // Check for existing role
        if (roleRepository.existsByName(request.getName())) {
            throw new ConflictException("Role already exists: " + request.getName());
        }

        Role role = roleMapper.toEntity(request);

        // Assign permissions if provided
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            if (permissions.size() != request.getPermissionIds().size()) {
                throw new NotFoundException("One or more permissions not found");
            }
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);

        // Audit log
        auditLogService.log(AuditAction.CREATE, "Role", savedRole.getId(),
            null, savedRole.getName(), getCurrentUsername());

        log.info("Role created successfully with ID: {}", savedRole.getId());
        return roleMapper.toResponse(savedRole);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#id")
    public RoleResponse getRoleById(String id) {
        log.debug("Fetching role by ID: {}", id);
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Role not found with id: " + id));
        return roleMapper.toResponse(role);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        log.debug("Fetching role by name: {}", name);
        Role role = roleRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException("Role not found with name: " + name));
        return roleMapper.toResponse(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll().stream()
            .map(roleMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", id);

        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Role not found with id: " + id));

        // System roles cannot be modified
        if (role.isSystemRole()) {
            throw new ConflictException("System roles cannot be modified");
        }

        String oldRoleInfo = role.getName(); // For audit log
        roleMapper.updateEntity(request, role);

        Role updatedRole = roleRepository.save(role);

        // Audit log
        auditLogService.log(AuditAction.UPDATE, "Role", id,
            oldRoleInfo, roleMapper.toResponse(updatedRole).toString(),
            getCurrentUsername());

        log.info("Role updated successfully with ID: {}", id);
        return roleMapper.toResponse(updatedRole);
    }

    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public void deleteRole(String id) {
        log.info("Deleting role with ID: {}", id);

        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Role not found with id: " + id));

        // System roles cannot be deleted
        if (role.isSystemRole()) {
            throw new ConflictException("System roles cannot be deleted");
        }

        // Check if role is assigned to any users
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new ConflictException("Cannot delete role assigned to users");
        }

        roleRepository.delete(role);

        // Audit log
        auditLogService.log(AuditAction.DELETE, "Role", id,
            role.getName(), null, getCurrentUsername());

        log.info("Role deleted successfully with ID: {}", id);
    }

    @Transactional
    @CacheEvict(value = "roles", key = "#roleId")
    public RoleResponse assignPermissions(String roleId, AssignPermissionRequest request) {
        log.info("Assigning permissions to role ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));

        // System roles permissions cannot be modified
        if (role.isSystemRole()) {
            throw new ConflictException("System role permissions cannot be modified");
        }

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
        if (permissions.size() != request.getPermissionIds().size()) {
            throw new NotFoundException("One or more permissions not found");
        }

        role.setPermissions(permissions);
        Role updatedRole = roleRepository.save(role);

        // Audit log
        auditLogService.log(AuditAction.UPDATE, "RolePermissions", roleId,
            null, "Permissions assigned: " + request.getPermissionIds(), getCurrentUsername());

        log.info("Permissions assigned successfully to role ID: {}", roleId);
        return roleMapper.toResponse(updatedRole);
    }

    private String getCurrentUsername() {
        // Implementation similar to UserService
        return "SYSTEM";
    }
}
