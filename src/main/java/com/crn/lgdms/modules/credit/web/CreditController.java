package com.crn.lgdms.modules.credit.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.credit.dto.request.SetCreditLimitRequest;
import com.crn.lgdms.modules.credit.dto.response.CreditAccountResponse;
import com.crn.lgdms.modules.credit.dto.response.CreditAgingResponse;
import com.crn.lgdms.modules.credit.dto.response.CreditTransactionResponse;
import com.crn.lgdms.modules.credit.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
@Tag(name = "Credit Management", description = "Credit account and limit management endpoints")
public class CreditController {

    private final CreditService creditService;
    private final PageRequestFactory pageRequestFactory;

    @PostMapping("/accounts/customer/{customerId}")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_APPROVE + "')")
    @Operation(summary = "Create credit account for customer")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> createCustomerCreditAccount(
        @PathVariable String customerId) {
        CreditAccountResponse response = creditService.createCreditAccountForCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success("Credit account created", response));
    }

    @PostMapping("/accounts/vehicle/{locationId}")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_APPROVE + "')")
    @Operation(summary = "Create credit account for vehicle (internal customer)")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> createVehicleCreditAccount(
        @PathVariable String locationId) {
        CreditAccountResponse response = creditService.createCreditAccountForVehicle(locationId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle credit account created", response));
    }

    @GetMapping("/accounts/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_VIEW + "')")
    @Operation(summary = "Get credit account by ID")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> getCreditAccountById(@PathVariable String id) {
        CreditAccountResponse response = creditService.getCreditAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts/customer/{customerId}")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_VIEW + "')")
    @Operation(summary = "Get credit account by customer ID")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> getCreditAccountByCustomerId(
        @PathVariable String customerId) {
        CreditAccountResponse response = creditService.getCreditAccountByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts/vehicle/{locationId}")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_VIEW + "')")
    @Operation(summary = "Get credit account by vehicle ID")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> getCreditAccountByVehicleId(
        @PathVariable String locationId) {
        CreditAccountResponse response = creditService.getCreditAccountByVehicleId(locationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_VIEW + "')")
    @Operation(summary = "Get all credit accounts")
    public ResponseEntity<ApiResponse<PageResponse<CreditAccountResponse>>> getAllCreditAccounts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection) {

        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(creditService.getAllCreditAccounts(pageable))));
    }

    @GetMapping("/accounts/search")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_VIEW + "')")
    @Operation(summary = "Search credit accounts")
    public ResponseEntity<ApiResponse<PageResponse<CreditAccountResponse>>> searchCreditAccounts(
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(creditService.searchCreditAccounts(q, pageable))));
    }

    @PostMapping("/limits")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_LIMIT_SET + "')")
    @Operation(summary = "Set credit limit for customer or vehicle")
    public ResponseEntity<ApiResponse<CreditAccountResponse>> setCreditLimit(
        @Valid @RequestBody SetCreditLimitRequest request) {
        CreditAccountResponse response = creditService.setCreditLimit(request);
        return ResponseEntity.ok(ApiResponse.success("Credit limit set successfully", response));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_VIEW + "')")
    @Operation(summary = "Get credit account transactions")
    public ResponseEntity<ApiResponse<PageResponse<CreditTransactionResponse>>> getAccountTransactions(
        @PathVariable String accountId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(creditService.getCreditAccountTransactions(accountId, pageable))));
    }

    @GetMapping("/aging")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Get credit aging report")
    public ResponseEntity<ApiResponse<CreditAgingResponse>> getAgingReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        CreditAgingResponse response = creditService.getAgingReport(asOfDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/accounts/over-limit")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_VIEW + "')")
    @Operation(summary = "Get accounts over credit limit")
    public ResponseEntity<ApiResponse<List<CreditAccountResponse>>> getAccountsOverLimit() {
        return ResponseEntity.ok(ApiResponse.success(creditService.getAccountsOverLimit()));
    }

    @PostMapping("/accounts/{accountId}/block")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_APPROVE + "')")
    @Operation(summary = "Block credit account")
    public ResponseEntity<ApiResponse<Void>> blockAccount(
        @PathVariable String accountId,
        @RequestParam String reason) {
        creditService.blockAccount(accountId, reason);
        return ResponseEntity.ok(ApiResponse.success("Account blocked", null));
    }

    @PostMapping("/accounts/{accountId}/unblock")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_APPROVE + "')")
    @Operation(summary = "Unblock credit account")
    public ResponseEntity<ApiResponse<Void>> unblockAccount(@PathVariable String accountId) {
        creditService.unblockAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success("Account unblocked", null));
    }
}
