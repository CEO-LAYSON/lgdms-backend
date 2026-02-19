package com.crn.lgdms.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private String id;
    private String type; // LOW_STOCK, EMPTY_VARIANCE, CREDIT_LIMIT, etc.
    private String severity; // INFO, WARNING, CRITICAL
    private String title;
    private String message;
    private String locationId;
    private String locationName;
    private String entityId;
    private String entityName;
    private LocalDateTime timestamp;
    private boolean acknowledged;
    private String action; // Suggested action
}
