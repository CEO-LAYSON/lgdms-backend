package com.crn.lgdms.modules.payments.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.payments.dto.request.RecordPaymentRequest;
import com.crn.lgdms.modules.payments.dto.response.PaymentResponse;
import com.crn.lgdms.modules.payments.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and allocation endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final PageRequestFactory pageRequestFactory;

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.SALE_CREATE + "')")
    @Operation(summary = "Record a new payment (with optional allocations)")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
        @Valid @RequestBody RecordPaymentRequest request) {
        PaymentResponse response = paymentService.recordPayment(request);
        return ResponseEntity
            .created(URI.create("/api/payments/" + response.getId()))
            .body(ApiResponse.success("Payment recorded successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable String id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{paymentNumber}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get payment by number")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByNumber(
        @PathVariable String paymentNumber) {
        PaymentResponse response = paymentService.getPaymentByNumber(paymentNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get all payments with pagination")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection) {

        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(paymentService.getAllPayments(pageable))));
    }

    @GetMapping("/location/{locationId}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get payments by location")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPaymentsByLocation(
        @PathVariable String locationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(paymentService.getPaymentsByLocation(locationId, pageable))));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get payments by customer")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPaymentsByCustomer(
        @PathVariable String customerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(paymentService.getPaymentsByCustomer(customerId, pageable))));
    }

    @PostMapping("/{id}/allocate")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_UPDATE + "')")
    @Operation(summary = "Allocate payment to specific sales")
    public ResponseEntity<ApiResponse<PaymentResponse>> allocatePayment(
        @PathVariable String id,
        @Valid @RequestBody java.util.List<RecordPaymentRequest.AllocationRequest> allocations) {
        PaymentResponse response = paymentService.allocatePayment(id, allocations);
        return ResponseEntity.ok(ApiResponse.success("Payment allocated successfully", response));
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VOID + "')")
    @Operation(summary = "Void a payment")
    public ResponseEntity<ApiResponse<Void>> voidPayment(
        @PathVariable String id,
        @RequestParam String reason) {
        paymentService.voidPayment(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Payment voided", null));
    }
}
