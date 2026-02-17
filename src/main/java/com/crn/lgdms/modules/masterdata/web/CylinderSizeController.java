package com.crn.lgdms.modules.masterdata.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.modules.masterdata.dto.request.CreateCylinderSizeRequest;
import com.crn.lgdms.modules.masterdata.dto.request.UpdateCylinderSizeRequest;
import com.crn.lgdms.modules.masterdata.dto.response.CylinderSizeResponse;
import com.crn.lgdms.modules.masterdata.service.CylinderSizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/masterdata/cylinder-sizes")
@RequiredArgsConstructor
@Tag(name = "Cylinder Sizes", description = "Cylinder size management endpoints")
public class CylinderSizeController {

    private final CylinderSizeService cylinderSizeService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Create a new cylinder size")
    public ResponseEntity<ApiResponse<CylinderSizeResponse>> createCylinderSize(
        @Valid @RequestBody CreateCylinderSizeRequest request) {
        CylinderSizeResponse response = cylinderSizeService.createCylinderSize(request);
        return ResponseEntity
            .created(URI.create("/api/masterdata/cylinder-sizes/" + response.getId()))
            .body(ApiResponse.success("Cylinder size created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get cylinder size by ID")
    public ResponseEntity<ApiResponse<CylinderSizeResponse>> getCylinderSizeById(@PathVariable String id) {
        CylinderSizeResponse response = cylinderSizeService.getCylinderSizeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get all cylinder sizes")
    public ResponseEntity<ApiResponse<List<CylinderSizeResponse>>> getAllCylinderSizes(
        @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(
            cylinderSizeService.getAllCylinderSizes(includeInactive)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Update cylinder size")
    public ResponseEntity<ApiResponse<CylinderSizeResponse>> updateCylinderSize(
        @PathVariable String id,
        @Valid @RequestBody UpdateCylinderSizeRequest request) {
        CylinderSizeResponse response = cylinderSizeService.updateCylinderSize(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cylinder size updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Delete cylinder size")
    public ResponseEntity<ApiResponse<Void>> deleteCylinderSize(@PathVariable String id) {
        cylinderSizeService.deleteCylinderSize(id);
        return ResponseEntity.ok(ApiResponse.success("Cylinder size deleted successfully", null));
    }
}
