package com.crn.lgdms.modules.receiving.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.receiving.dto.request.CreateReceivingRequest;
import com.crn.lgdms.modules.receiving.dto.request.UpdateReceivingRequest;
import com.crn.lgdms.modules.receiving.dto.request.VerifyReceivingRequest;
import com.crn.lgdms.modules.receiving.dto.response.ReceivingResponse;
import com.crn.lgdms.modules.receiving.service.ReceivingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/receiving")
@RequiredArgsConstructor
@Tag(name = "Goods Receiving", description = "Goods receiving management endpoints")
public class ReceivingController {

    private final ReceivingService receivingService;
    private final PageRequestFactory pageRequestFactory;

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Create new goods receiving")
    public ResponseEntity<ApiResponse<ReceivingResponse>> createReceiving(
        @Valid @RequestBody CreateReceivingRequest request) {
        ReceivingResponse response = receivingService.createReceiving(request);
        return ResponseEntity
            .created(URI.create("/api/receiving/" + response.getId()))
            .body(ApiResponse.success("Goods receiving created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get receiving by ID")
    public ResponseEntity<ApiResponse<ReceivingResponse>> getReceivingById(@PathVariable String id) {
        ReceivingResponse response = receivingService.getReceivingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{receivingNumber}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get receiving by number")
    public ResponseEntity<ApiResponse<ReceivingResponse>> getReceivingByNumber(
        @PathVariable String receivingNumber) {
        ReceivingResponse response = receivingService.getReceivingByNumber(receivingNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get all receivings with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ReceivingResponse>>> getAllReceivings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection) {

        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(receivingService.getAllReceivings(pageable))));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Search receivings")
    public ResponseEntity<ApiResponse<PageResponse<ReceivingResponse>>> searchReceivings(
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(receivingService.searchReceivings(q, pageable))));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get receivings by date range")
    public ResponseEntity<ApiResponse<List<ReceivingResponse>>> getReceivingsByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(ApiResponse.success(
            receivingService.getReceivingsByDateRange(startDate, endDate)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Update receiving (only if pending)")
    public ResponseEntity<ApiResponse<ReceivingResponse>> updateReceiving(
        @PathVariable String id,
        @Valid @RequestBody UpdateReceivingRequest request) {

        ReceivingResponse response = receivingService.updateReceiving(id, request);
        return ResponseEntity.ok(ApiResponse.success("Receiving updated successfully", response));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Verify receiving and update stock")
    public ResponseEntity<ApiResponse<ReceivingResponse>> verifyReceiving(
        @PathVariable String id,
        @Valid @RequestBody VerifyReceivingRequest request) {

        ReceivingResponse response = receivingService.verifyReceiving(id, request);
        return ResponseEntity.ok(ApiResponse.success("Receiving verified and stock updated", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Cancel receiving")
    public ResponseEntity<ApiResponse<Void>> cancelReceiving(
        @PathVariable String id,
        @RequestParam String reason) {

        receivingService.cancelReceiving(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Receiving cancelled", null));
    }
}
