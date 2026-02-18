package com.crn.lgdms.modules.inventory.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.modules.inventory.dto.request.ReconcileRequest;
import com.crn.lgdms.modules.inventory.dto.response.EmptyBalanceResponse;
import com.crn.lgdms.modules.inventory.dto.response.ReconciliationResponse;
import com.crn.lgdms.modules.inventory.service.EmptyBalanceService;
import com.crn.lgdms.modules.inventory.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Stock Reconciliation", description = "Stock reconciliation operations")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final EmptyBalanceService emptyBalanceService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Perform stock reconciliation")
    public ResponseEntity<ApiResponse<ReconciliationResponse>> performReconciliation(
        @Valid @RequestBody ReconcileRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {

        ReconciliationResponse response = reconciliationService.performReconciliation(
            request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reconciliation completed", response));
    }

    @GetMapping("/history/{locationId}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get reconciliation history")
    public ResponseEntity<ApiResponse<List<ReconciliationResponse>>> getReconciliationHistory(
        @PathVariable String locationId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ReconciliationResponse> history = reconciliationService.getReconciliationHistory(
            locationId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/required")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get locations needing reconciliation")
    public ResponseEntity<ApiResponse<List<ReconciliationResponse>>> getLocationsNeedingReconciliation() {
        List<ReconciliationResponse> locations = reconciliationService.getLocationsNeedingReconciliation();
        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    @GetMapping("/empty-balance/{locationId}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get empty cylinder balances")
    public ResponseEntity<ApiResponse<List<EmptyBalanceResponse>>> getEmptyBalances(
        @PathVariable String locationId) {
        List<EmptyBalanceResponse> balances = emptyBalanceService.getAllEmptyBalances(locationId);
        return ResponseEntity.ok(ApiResponse.success(balances));
    }

    @GetMapping("/empty-variance")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get locations with empty cylinder variance")
    public ResponseEntity<ApiResponse<List<EmptyBalanceResponse>>> getLocationsWithVariance() {
        List<EmptyBalanceResponse> variances = emptyBalanceService.getLocationsWithVariance();
        return ResponseEntity.ok(ApiResponse.success(variances));
    }
}
