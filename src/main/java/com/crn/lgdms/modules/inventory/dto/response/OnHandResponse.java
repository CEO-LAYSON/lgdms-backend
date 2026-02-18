package com.crn.lgdms.modules.inventory.dto.response;

import com.crn.lgdms.common.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnHandResponse {
    private String locationId;
    private String locationName;
    private String cylinderSizeId;
    private String cylinderSizeName;
    private ProductType productType;
    private Integer quantity;
    private LocalDateTime lastUpdated;

    // Business rule indicators
    private boolean isLowStock;
    private boolean isOutOfStock;
    private Integer reorderPoint;
}
