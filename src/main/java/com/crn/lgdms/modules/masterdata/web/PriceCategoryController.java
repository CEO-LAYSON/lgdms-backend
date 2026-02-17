package com.crn.lgdms.modules.masterdata.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.masterdata.dto.request.CreatePriceCategoryRequest;
import com.crn.lgdms.modules.masterdata.dto.request.UpdatePriceCategoryRequest;
import com.crn.lgdms.modules.masterdata.dto.response.PriceCategoryResponse;
import com.crn.lgdms.modules.masterdata.service.PriceCategoryService;
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
@RequestMapping("/api/masterdata/price-categories")
@RequiredArgsConstructor
@Tag(name = "Price Categories", description = "Price category management endpoints")
public class PriceCategoryController {

    private final PriceCategoryService priceCategoryService;
    private final PageRequestFactory pageRequestFactory;

    @PostMapping
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Create a new price category")
    public ResponseEntity<ApiResponse<PriceCategoryResponse>> createPriceCategory(
        @Valid @RequestBody CreatePriceCategoryRequest request) {
        PriceCategoryResponse response = priceCategoryService.createPriceCategory(request);
        return ResponseEntity
            .created(URI.create("/api/masterdata/price-categories/" + response.getId()))
            .body(ApiResponse.success("Price category created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get price category by ID")
    public ResponseEntity<ApiResponse<PriceCategoryResponse>> getPriceCategoryById(@PathVariable String id) {
        PriceCategoryResponse response = priceCategoryService.getPriceCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Search price categories")
    public ResponseEntity<ApiResponse<PageResponse<PriceCategoryResponse>>> searchPriceCategories(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String cylinderSizeId,
        @RequestParam(required = false) ProductType productType,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection) {

        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(priceCategoryService.searchPriceCategories(
                name, cylinderSizeId, productType, isActive, pageable))));
    }

    @GetMapping("/current/{cylinderSizeId}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get current prices for cylinder size")
    public ResponseEntity<ApiResponse<List<PriceCategoryResponse>>> getCurrentPrices(
        @PathVariable String cylinderSizeId) {
        return ResponseEntity.ok(ApiResponse.success(
            priceCategoryService.getCurrentPrices(cylinderSizeId)));
    }

    @GetMapping("/current/{cylinderSizeId}/{productType}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_VIEW + "')")
    @Operation(summary = "Get current price for specific product type")
    public ResponseEntity<ApiResponse<PriceCategoryResponse>> getCurrentPrice(
        @PathVariable String cylinderSizeId,
        @PathVariable ProductType productType) {
        PriceCategoryResponse response = priceCategoryService.getCurrentPrice(cylinderSizeId, productType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Update price category")
    public ResponseEntity<ApiResponse<PriceCategoryResponse>> updatePriceCategory(
        @PathVariable String id,
        @Valid @RequestBody UpdatePriceCategoryRequest request) {
        PriceCategoryResponse response = priceCategoryService.updatePriceCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Price category updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + Permissions.INVENTORY_ADJUST + "')")
    @Operation(summary = "Delete price category")
    public ResponseEntity<ApiResponse<Void>> deletePriceCategory(@PathVariable String id) {
        priceCategoryService.deletePriceCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Price category deleted successfully", null));
    }
}
