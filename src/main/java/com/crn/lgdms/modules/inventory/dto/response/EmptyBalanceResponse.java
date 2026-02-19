package com.crn.lgdms.modules.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmptyBalanceResponse {
    private String locationId;
    private String locationName;
    private String cylinderSizeId;
    private String cylinderSizeName;
    private Integer currentBalance;
    private Integer expectedBalance;
    private Integer variance;
    private String varianceStatus; // NORMAL, WARNING, CRITICAL
    private LocalDateTime lastCalculated;
}
