package com.crn.lgdms.modules.locations.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.enums.LocationType;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.locations.dto.request.CreateLocationRequest;
import com.crn.lgdms.modules.locations.dto.request.UpdateLocationRequest;
import com.crn.lgdms.modules.locations.dto.response.LocationResponse;
import com.crn.lgdms.modules.locations.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Location management endpoints - DYNAMIC Branches & Vehicles!")
public class LocationController {

    private final LocationService locationService;
    private final PageRequestFactory pageRequestFactory;

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Create a new location (Branch/Vehicle/HQ)")
    public ResponseEntity<ApiResponse<LocationResponse>> createLocation(
        @Valid @RequestBody CreateLocationRequest request) {
        LocationResponse response = locationService.createLocation(request);
        return ResponseEntity
            .created(URI.create("/api/locations/" + response.getId()))
            .body(ApiResponse.success("Location created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get location by ID")
    public ResponseEntity<ApiResponse<LocationResponse>> getLocationById(@PathVariable String id) {
        LocationResponse response = locationService.getLocationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get location by code")
    public ResponseEntity<ApiResponse<LocationResponse>> getLocationByCode(@PathVariable String code) {
        LocationResponse response = locationService.getLocationByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get all locations with optional filters")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getAllLocations(
        @RequestParam(required = false) LocationType type,
        @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success(
            locationService.getAllLocations(type, activeOnly)));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get locations by type (HQ, BRANCH, VEHICLE)")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getLocationsByType(
        @PathVariable LocationType type) {
        return ResponseEntity.ok(ApiResponse.success(
            locationService.getLocationsByType(type)));
    }

    @GetMapping("/branches")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get all branches (dynamic)")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getAllBranches() {
        return ResponseEntity.ok(ApiResponse.success(
            locationService.getLocationsByType(LocationType.BRANCH)));
    }

    @GetMapping("/vehicles")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get all vehicles (dynamic)")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getAllVehicles() {
        return ResponseEntity.ok(ApiResponse.success(
            locationService.getLocationsByType(LocationType.VEHICLE)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Search locations")
    public ResponseEntity<ApiResponse<PageResponse<LocationResponse>>> searchLocations(
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(locationService.searchLocations(q, pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Update location")
    public ResponseEntity<ApiResponse<LocationResponse>> updateLocation(
        @PathVariable String id,
        @Valid @RequestBody UpdateLocationRequest request) {
        LocationResponse response = locationService.updateLocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Delete location (deactivate)")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable String id) {
        locationService.deleteLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Location deactivated successfully", null));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Activate location")
    public ResponseEntity<ApiResponse<Void>> activateLocation(@PathVariable String id) {
        locationService.activateLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Location activated successfully", null));
    }
}
