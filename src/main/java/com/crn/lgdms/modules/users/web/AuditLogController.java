package com.crn.lgdms.modules.users.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.api.PageResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.common.pagination.PageRequestFactory;
import com.crn.lgdms.modules.users.dto.request.AuditLogSearchRequest;
import com.crn.lgdms.modules.users.dto.response.AuditLogResponse;
import com.crn.lgdms.modules.users.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit log management endpoints")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final PageRequestFactory pageRequestFactory;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('" + Permissions.REPORT_VIEW + "')")
    @Operation(summary = "Search audit logs")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> searchAuditLogs(
        @Valid @RequestBody AuditLogSearchRequest request,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection) {

        Pageable pageable = pageRequestFactory.create(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.from(auditLogService.searchAuditLogs(request, pageable))));
    }
}
