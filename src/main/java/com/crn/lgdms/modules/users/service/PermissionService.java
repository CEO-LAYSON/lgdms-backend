package com.crn.lgdms.modules.users.service;

import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.users.domain.entity.Permission;
import com.crn.lgdms.modules.users.dto.response.PermissionResponse;
import com.crn.lgdms.modules.users.dto.mapper.PermissionMapper;
import com.crn.lgdms.modules.users.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "permissions", key = "#id")
    public PermissionResponse getPermissionById(String id) {
        log.debug("Fetching permission by ID: {}", id);
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Permission not found with id: " + id));
        return permissionMapper.toResponse(permission);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionByName(String name) {
        log.debug("Fetching permission by name: {}", name);
        Permission permission = permissionRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException("Permission not found with name: " + name));
        return permissionMapper.toResponse(permission);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        log.debug("Fetching all permissions");
        return permissionRepository.findAll().stream()
            .map(permissionMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getPermissionsGrouped() {
        log.debug("Fetching permissions grouped by resource");
        return permissionRepository.findAllGrouped().stream()
            .map(permissionMapper::toResponse)
            .collect(Collectors.groupingBy(PermissionResponse::getResource));
    }
}
