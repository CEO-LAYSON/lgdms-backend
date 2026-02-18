package com.crn.lgdms.modules.sales.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.sales.dto.request.CreateCustomerRequest;
import com.crn.lgdms.modules.sales.dto.request.UpdateCustomerRequest;
import com.crn.lgdms.modules.sales.dto.response.CustomerResponse;
import com.crn.lgdms.modules.sales.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService customerService;
    private final PageRequestFactory pageRequestFactory;

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.SALE_CREATE + "')")
    @Operation(summary = "Create a new customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
        @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity
            .created(URI.create("/api/customers/" + response.getId()))
            .body(ApiResponse.success("Customer created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable String id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Get customer by phone")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByPhone(@PathVariable String phone) {
        CustomerResponse response = customerService.getCustomerByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Search customers")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> searchCustomers(
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = pageRequestFactory.create(page, size);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(customerService.searchCustomers(q, pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_UPDATE + "')")
    @Operation(summary = "Update customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
        @PathVariable String id,
        @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_DELETE + "')")
    @Operation(summary = "Delete customer (deactivate)")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deactivated successfully", null));
    }
}
