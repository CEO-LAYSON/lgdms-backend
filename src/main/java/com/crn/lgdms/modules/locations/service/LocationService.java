package com.crn.lgdms.modules.locations.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.LocationType;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.dto.request.CreateLocationRequest;
import com.crn.lgdms.modules.locations.dto.request.UpdateLocationRequest;
import com.crn.lgdms.modules.locations.dto.response.LocationResponse;
import com.crn.lgdms.modules.locations.dto.mapper.LocationMapper;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public LocationResponse createLocation(CreateLocationRequest request) {
        log.info("Creating new {} location: {}", request.getLocationType(), request.getName());

        if (request.getCode() != null && locationRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Location code already exists: " + request.getCode());
        }

        Location location = locationMapper.toEntity(request);

        // Auto-generate code if not provided
        if (location.getCode() == null) {
            location.setCode(generateLocationCode(request));
        }

        Location saved = locationRepository.save(location);

        auditLogService.log(AuditAction.CREATE, "Location", saved.getId(),
            null, saved.getName(), getCurrentUsername());

        log.info("Location created successfully with ID: {} and code: {}", saved.getId(), saved.getCode());
        return locationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "locations", key = "#id")
    public LocationResponse getLocationById(String id) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + id));
        return locationMapper.toResponse(location);
    }

    @Transactional(readOnly = true)
    public LocationResponse getLocationByCode(String code) {
        Location location = locationRepository.findByCode(code)
            .orElseThrow(() -> new NotFoundException("Location not found with code: " + code));
        return locationMapper.toResponse(location);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations(LocationType type, boolean activeOnly) {
        return locationRepository.findLocations(type, activeOnly).stream()
            .map(locationMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationsByType(LocationType type) {
        return locationRepository.findByLocationTypeAndIsActiveTrue(type).stream()
            .map(locationMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LocationResponse> searchLocations(String searchTerm, Pageable pageable) {
        return locationRepository.searchLocations(searchTerm, pageable)
            .map(locationMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "locations", key = "#id")
    public LocationResponse updateLocation(String id, UpdateLocationRequest request) {
        log.info("Updating location with ID: {}", id);

        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + id));

        locationMapper.updateEntity(request, location);
        Location updated = locationRepository.save(location);

        auditLogService.log(AuditAction.UPDATE, "Location", id,
            null, updated.getName(), getCurrentUsername());

        return locationMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "locations", key = "#id")
    public void deleteLocation(String id) {
        log.info("Deleting location with ID: {}", id);

        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + id));

        // Check if location has children
        List<Location> children = locationRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new ConflictException("Cannot delete location with child locations");
        }

        // Soft delete by deactivating
        location.setActive(false);
        locationRepository.save(location);

        auditLogService.log(AuditAction.DELETE, "Location", id,
            location.getName(), "deactivated", getCurrentUsername());

        log.info("Location deactivated successfully with ID: {}", id);
    }

    @Transactional
    public void activateLocation(String id) {
        log.info("Activating location with ID: {}", id);

        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + id));

        location.setActive(true);
        locationRepository.save(location);

        auditLogService.log(AuditAction.UPDATE, "Location", id,
            "deactivated", "activated", getCurrentUsername());
    }

    private String generateLocationCode(CreateLocationRequest request) {
        String prefix = switch (request.getLocationType()) {
            case HQ -> "HQ";
            case BRANCH -> "BR";
            case VEHICLE -> "VH";
        };

        // Get count of existing locations of this type
        long count = locationRepository.countByLocationType(request.getLocationType()) + 1;

        return String.format("%s%03d", prefix, count);
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
