package com.crn.lgdms.modules.inventory.dto.response;

import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentResponse {
    private String id;
    private String adjustmentNumber;
    private String locationId;
    private String locationName;
    private String cylinderSizeId;
    private String cylinderSizeName;
    private ProductType productType;
    private Integer oldQuantity;
    private Integer newQuantity;
    private Integer difference;
    private String reason;
    private TransactionStatus status;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
