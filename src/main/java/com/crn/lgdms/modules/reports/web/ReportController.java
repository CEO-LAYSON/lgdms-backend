package com.crn.lgdms.modules.reports.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.modules.reports.dto.request.ReportFilterRequest;
import com.crn.lgdms.modules.reports.dto.response.ReportSummaryResponse;
import com.crn.lgdms.modules.reports.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation endpoints")
public class ReportController {

    private final SalesReportService salesReportService;
    private final StockMovementReportService stockMovementReportService;
    private final EmptyReconciliationReportService emptyReconciliationReportService;
    private final VehiclePerformanceReportService vehiclePerformanceReportService;
    private final ProfitReportService profitReportService;

    @PostMapping("/sales")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Generate sales report")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> generateSalesReport(
        @Valid @RequestBody ReportFilterRequest request) {
        ReportSummaryResponse response = salesReportService.generateSalesReport(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/sales-by-product")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Generate sales by product report")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> generateSalesByProductReport(
        @Valid @RequestBody ReportFilterRequest request) {
        ReportSummaryResponse response = salesReportService.generateSalesByProductReport(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/stock-movement")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Generate stock movement report")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> generateStockMovementReport(
        @Valid @RequestBody ReportFilterRequest request) {
        ReportSummaryResponse response = stockMovementReportService.generateStockMovementReport(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/empty-reconciliation")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Generate empty cylinder reconciliation report")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> generateEmptyReconciliationReport(
        @Valid @RequestBody ReportFilterRequest request) {
        ReportSummaryResponse response = emptyReconciliationReportService.generateEmptyReconciliationReport(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/vehicle-performance")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Generate vehicle performance report")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> generateVehiclePerformanceReport(
        @Valid @RequestBody ReportFilterRequest request) {
        ReportSummaryResponse response = vehiclePerformanceReportService.generateVehiclePerformanceReport(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/profit")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Generate profit and margin report")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> generateProfitReport(
        @Valid @RequestBody ReportFilterRequest request) {
        ReportSummaryResponse response = profitReportService.generateProfitReport(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_EXPORT + "')")
    @Operation(summary = "Export report to PDF/Excel")
    public ResponseEntity<byte[]> exportReport(
        @Valid @RequestBody com.crn.lgdms.modules.reports.dto.request.ExportRequest request) {
        // Implementation would generate PDF/Excel file
        // For now, return empty response
        return ResponseEntity.ok().build();
    }
}
