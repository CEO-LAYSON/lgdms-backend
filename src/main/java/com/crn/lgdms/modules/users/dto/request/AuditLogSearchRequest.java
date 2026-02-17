package com.crn.lgdms.modules.users.dto.request;

import com.crn.lgdms.common.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchRequest {
    private String userId;
    private AuditAction action;
    private String entityType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
