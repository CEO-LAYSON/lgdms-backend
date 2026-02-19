package com.crn.lgdms.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationStockSummaryResponse {
    private String locationId;
    private String locationName;
    private String locationType;
    private List<ProductStock> productStocks;
    private int totalFull;
    private int totalEmpty;
    private int lowStockCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStock {
        private String cylinderSizeId;
        private String cylinderSizeName;
        private int fullQuantity;
        private int emptyQuantity;
        private int minStockLevel;
        private boolean isLowStock;
    }
}
