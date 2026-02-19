package com.crn.lgdms.modules.dashboard.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.modules.dashboard.dto.response.DashboardKpiResponse;
import com.crn.lgdms.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard KPIs and metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpi")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Get dashboard KPIs")
    public ResponseEntity<ApiResponse<DashboardKpiResponse>> getDashboardKpis() {
        DashboardKpiResponse response = dashboardService.getDashboardKpis();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
