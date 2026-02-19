package com.crn.lgdms.modules.transfer.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.transfer.dto.request.*;
import com.crn.lgdms.modules.transfer.dto.response.TransferRequestResponse;
import com.crn.lgdms.modules.transfer.dto.response.TransferResponse;
import com.crn.lgdms.modules.transfer.service.TransferService;
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
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Transfer management endpoints for cylinder inventory transfers between locations")
public class TransferController {

    private final TransferService transferService;
    private final PageRequestFactory pageRequestFactory;

    // ==================== Transfer Request Endpoints ====================

    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_CREATE + "')")
    @Operation(summary = "Create a new transfer request", 
               description = "Create a request to transfer cylinders from one location to another")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> createTransferRequest(
            @Valid @RequestBody CreateTransferRequestRequest request) {
        TransferRequestResponse response = transferService.createTransferRequest(request);
        return ResponseEntity
                .created(URI.create("/api/transfers/requests/" + response.getId()))
                .body(ApiResponse.success("Transfer request created successfully", response));
    }

    @GetMapping("/requests/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_VIEW + "')")
    @Operation(summary = "Get transfer request by ID")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> getTransferRequestById(
            @PathVariable String id) {
        TransferRequestResponse response = transferService.getTransferRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/requests/number/{requestNumber}")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_VIEW + "')")
    @Operation(summary = "Get transfer request by request number")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> getTransferRequestByNumber(
            @PathVariable String requestNumber) {
        TransferRequestResponse response = transferService.getTransferRequestByNumber(requestNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_VIEW + "')")
    @Operation(summary = "Get all transfer requests with pagination")
    public ResponseEntity<ApiResponse<PageResponse<TransferRequestResponse>>> getAllTransferRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(transferService.getAllTransferRequests(pageable))));
    }

    @GetMapping("/requests/location/{locationId}/pending")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_VIEW + "')")
    @Operation(summary = "Get pending transfer requests for a location")
    public ResponseEntity<ApiResponse<List<TransferRequestResponse>>> getPendingRequestsForLocation(
            @PathVariable String locationId) {
        List<TransferRequestResponse> response = transferService.getPendingRequestsForLocation(locationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_APPROVE + "')")
    @Operation(summary = "Approve a transfer request", 
               description = "Approve a pending transfer request with optional quantity adjustments")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> approveTransferRequest(
            @PathVariable String id,
            @Valid @RequestBody ApproveTransferRequest request) {
        TransferRequestResponse response = transferService.approveTransferRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Transfer request approved successfully", response));
    }

    @PostMapping("/requests/{id}/reject")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_APPROVE + "')")
    @Operation(summary = "Reject a transfer request")
    public ResponseEntity<ApiResponse<TransferRequestResponse>> rejectTransferRequest(
            @PathVariable String id,
            @Valid @RequestBody RejectTransferRequest request) {
        TransferRequestResponse response = transferService.rejectTransferRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Transfer request rejected successfully", response));
    }

    // ==================== Transfer Execution Endpoints ====================

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_CREATE + "')")
    @Operation(summary = "Create a transfer from an approved request", 
               description = "Execute a transfer from an approved transfer request")
    public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(
            @Valid @RequestBody CreateTransferRequest request) {
        TransferResponse response = transferService.createTransfer(request);
        return ResponseEntity
                .created(URI.create("/api/transfers/" + response.getId()))
                .body(ApiResponse.success("Transfer created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_VIEW + "')")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<ApiResponse<TransferResponse>> getTransferById(@PathVariable String id) {
        TransferResponse response = transferService.getTransferById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{transferNumber}")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_VIEW + "')")
    @Operation(summary = "Get transfer by transfer number")
    public ResponseEntity<ApiResponse<TransferResponse>> getTransferByNumber(
            @PathVariable String transferNumber) {
        TransferResponse response = transferService.getTransferByNumber(transferNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_VIEW + "')")
    @Operation(summary = "Get all transfers with filtering and pagination")
    public ResponseEntity<ApiResponse<PageResponse<TransferResponse>>> getAllTransfers(
            @RequestParam(required = false) String fromLocationId,
            @RequestParam(required = false) String toLocationId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(transferService.getTransfers(fromLocationId, toLocationId, status, pageable))));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_RECEIVE + "')")
    @Operation(summary = "Receive a transfer", 
               description = "Record the receipt of a transfer at the destination location")
    public ResponseEntity<ApiResponse<TransferResponse>> receiveTransfer(
            @PathVariable String id,
            @Valid @RequestBody ReceiveTransferRequest request) {
        TransferResponse response = transferService.receiveTransfer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Transfer received successfully", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('" + Permissions.TRANSFER_CANCEL + "')")
    @Operation(summary = "Cancel a transfer", 
               description = "Cancel an in-transit transfer and reverse stock entries")
    public ResponseEntity<ApiResponse<Void>> cancelTransfer(
            @PathVariable String id,
            @RequestParam String reason) {
        transferService.cancelTransfer(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Transfer cancelled successfully", null));
    }
}
