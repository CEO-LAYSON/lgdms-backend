package com.crn.lgdms.modules.masterdata.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.dto.request.CreateCylinderSizeRequest;
import com.crn.lgdms.modules.masterdata.dto.request.UpdateCylinderSizeRequest;
import com.crn.lgdms.modules.masterdata.dto.response.CylinderSizeResponse;
import com.crn.lgdms.modules.masterdata.dto.mapper.CylinderSizeMapper;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CylinderSizeService {

    private final CylinderSizeRepository cylinderSizeRepository;
    private final CylinderSizeMapper cylinderSizeMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public CylinderSizeResponse createCylinderSize(CreateCylinderSizeRequest request) {
        log.info("Creating new cylinder size: {}", request.getName());

        if (cylinderSizeRepository.existsByName(request.getName())) {
            throw new ConflictException("Cylinder size already exists: " + request.getName());
        }

        CylinderSize cylinderSize = cylinderSizeMapper.toEntity(request);
        CylinderSize saved = cylinderSizeRepository.save(cylinderSize);

        auditLogService.log(AuditAction.CREATE, "CylinderSize", saved.getId(),
            null, saved.getName(), getCurrentUsername());

        return cylinderSizeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cylinderSizes", key = "#id")
    public CylinderSizeResponse getCylinderSizeById(String id) {
        CylinderSize cylinderSize = cylinderSizeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + id));
        return cylinderSizeMapper.toResponse(cylinderSize);
    }

    @Transactional(readOnly = true)
    public List<CylinderSizeResponse> getAllCylinderSizes(boolean includeInactive) {
        List<CylinderSize> sizes;
        if (includeInactive) {
            sizes = cylinderSizeRepository.findAllOrdered();
        } else {
            sizes = cylinderSizeRepository.findByIsActiveTrue(Sort.by(Sort.Direction.ASC, "displayOrder", "weightKg"));
        }
        return sizes.stream()
            .map(cylinderSizeMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "cylinderSizes", key = "#id")
    public CylinderSizeResponse updateCylinderSize(String id, UpdateCylinderSizeRequest request) {
        log.info("Updating cylinder size with ID: {}", id);

        CylinderSize cylinderSize = cylinderSizeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(cylinderSize.getName())) {
            if (cylinderSizeRepository.existsByName(request.getName())) {
                throw new ConflictException("Cylinder size already exists: " + request.getName());
            }
        }

        cylinderSizeMapper.updateEntity(request, cylinderSize);
        CylinderSize updated = cylinderSizeRepository.save(cylinderSize);

        auditLogService.log(AuditAction.UPDATE, "CylinderSize", id,
            null, updated.getName(), getCurrentUsername());

        return cylinderSizeMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "cylinderSizes", key = "#id")
    public void deleteCylinderSize(String id) {
        log.info("Deleting cylinder size with ID: {}", id);

        CylinderSize cylinderSize = cylinderSizeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + id));

        cylinderSizeRepository.delete(cylinderSize);

        auditLogService.log(AuditAction.DELETE, "CylinderSize", id,
            cylinderSize.getName(), null, getCurrentUsername());
    }

    private String getCurrentUsername() {
        // Implementation from UserService
        return "SYSTEM";
    }
}
