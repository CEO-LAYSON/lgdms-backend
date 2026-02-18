package com.crn.lgdms.modules.inventory.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.inventory.dto.request.CreateAdjustmentRequest;
import com.crn.lgdms.modules.inventory.dto.response.AdjustmentResponse;
import com.crn.lgdms.modules.inventory.dto.response.OnHandResponse;
import com.crn.lgdms.modules.inventory.service.InventoryService;
import com.crn.lgdms.modules.inventory.service.StockQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "Inventory operations with business rules")
public class InventoryController {

    private final StockQueryService stockQueryService;
    private final InventoryService inventoryService;
    private final PageRequestFactory pageRequestFactory;

    @GetMapping("/stock/{locationId}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get on-hand stock for a location")
    public ResponseEntity<ApiResponse<List<OnHandResponse>>> getOnHandStock(
        @PathVariable String locationId) {
        List<OnHandResponse> stock = stockQueryService.getOnHandStock(locationId);
        return ResponseEntity.ok(ApiResponse.success(stock));
    }

    @GetMapping("/stock/low")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get all low stock items across locations")
    public ResponseEntity<ApiResponse<List<OnHandResponse>>> getLowStockItems() {
        List<OnHandResponse> lowStock = stockQueryService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success(lowStock));
    }

    @GetMapping("/stock/value")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get total stock value")
    public ResponseEntity<ApiResponse<Double>> getTotalStockValue() {
        Double totalValue = stockQueryService.getTotalStockValue();
        return ResponseEntity.ok(ApiResponse.success(totalValue));
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Create a stock adjustment")
    public ResponseEntity<ApiResponse<AdjustmentResponse>> createAdjustment(
        @Valid @RequestBody CreateAdjustmentRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {

        AdjustmentResponse response = inventoryService.createAdjustment(request, userDetails.getUsername());
        return ResponseEntity
            .created(URI.create("/api/inventory/adjustments/" + response.getId()))
            .body(ApiResponse.success("Adjustment created successfully", response));
    }

    @GetMapping("/adjustments")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get all adjustments with pagination")
    public ResponseEntity<ApiResponse<PageResponse<AdjustmentResponse>>> getAdjustments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection) {

        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        // Implementation would fetch from repository
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(null)));
    }

    @PostMapping("/adjustments/{id}/approve")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Approve a pending adjustment")
    public ResponseEntity<ApiResponse<AdjustmentResponse>> approveAdjustment(
        @PathVariable String id,
        @AuthenticationPrincipal UserDetails userDetails) {

        AdjustmentResponse response = inventoryService.approveAdjustment(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Adjustment approved", response));
    }

    @PostMapping("/adjustments/{id}/reject")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Reject a pending adjustment")
    public ResponseEntity<ApiResponse<AdjustmentResponse>> rejectAdjustment(
        @PathVariable String id,
        @RequestParam String reason) {

        AdjustmentResponse response = inventoryService.rejectAdjustment(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Adjustment rejected", response));
    }
}
