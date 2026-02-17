package com.crn.lgdms.modules.users.dto.response;

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
public class AuditLogResponse {
    private String id;
    private String userId;
    private String username;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String ipAddress;
    private LocalDateTime createdAt;
}
