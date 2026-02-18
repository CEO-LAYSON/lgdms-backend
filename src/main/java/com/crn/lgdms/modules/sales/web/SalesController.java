package com.crn.lgdms.modules.sales.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.sales.dto.request.CreateSaleRequest;
import com.crn.lgdms.modules.sales.dto.response.SaleResponse;
import com.crn.lgdms.modules.sales.service.SalesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Sales management endpoints")
public class SalesController {

    private final SalesService salesService;
    private final PageRequestFactory pageRequestFactory;

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.SALE_CREATE + "')")
    @Operation(summary = "Create a new sale (with refill rule enforcement)")
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(
        @Valid @RequestBody CreateSaleRequest request) {
        SaleResponse response = salesService.createSale(request);
        return ResponseEntity
            .created(URI.create("/api/sales/" + response.getId()))
            .body(ApiResponse.success("Sale completed successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get sale by ID")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleById(@PathVariable String id) {
        SaleResponse response = salesService.getSaleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/invoice/{invoiceNumber}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get sale by invoice number")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleByInvoiceNumber(
        @PathVariable String invoiceNumber) {
        SaleResponse response = salesService.getSaleByInvoiceNumber(invoiceNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get all sales with pagination")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getAllSales(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection) {

        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(salesService.getAllSales(pageable))));
    }

    @GetMapping("/location/{locationId}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get sales by location")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getSalesByLocation(
        @PathVariable String locationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(salesService.getSalesByLocation(locationId, pageable))));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get sales by customer")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getSalesByCustomer(
        @PathVariable String customerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(salesService.getSalesByCustomer(customerId, pageable))));
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VOID + "')")
    @Operation(summary = "Void a sale (reverse stock and empty cylinders)")
    public ResponseEntity<ApiResponse<SaleResponse>> voidSale(
        @PathVariable String id,
        @RequestParam String reason) {
        SaleResponse response = salesService.voidSale(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Sale voided successfully", response));
    }
}
