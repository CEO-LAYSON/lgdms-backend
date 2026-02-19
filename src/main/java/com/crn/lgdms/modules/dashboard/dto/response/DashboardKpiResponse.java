package com.crn.lgdms.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiResponse {
    private StockSummary stockSummary;
    private SalesSummary salesSummary;
    private CreditSummary creditSummary;
    private List<AlertResponse> alerts;
    private List<LocationStockSummaryResponse> locationStock;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockSummary {
        private int totalFullCylinders;
        private int totalEmptyCylinders;
        private int lowStockItems;
        private int outOfStockItems;
        private BigDecimal totalStockValue;
        private List<StockByLocation> stockByLocation;
        private List<StockByProduct> stockByProduct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesSummary {
        private BigDecimal todaySales;
        private int todayTransactions;
        private BigDecimal weekSales;
        private BigDecimal monthSales;
        private BigDecimal yearSales;
        private List<DailySales> dailySales;
        private List<SalesByLocation> salesByLocation;
        private List<SalesByProduct> salesByProduct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditSummary {
        private BigDecimal totalOutstanding;
        private int accountsOverLimit;
        private BigDecimal overdueAmount;
        private int overdueAccounts;
        private List<AgingBucket> agingBuckets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockByLocation {
        private String locationId;
        private String locationName;
        private String locationType;
        private int fullCylinders;
        private int emptyCylinders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockByProduct {
        private String cylinderSize;
        private int fullQuantity;
        private int emptyQuantity;
        private BigDecimal value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySales {
        private String date;
        private BigDecimal amount;
        private int transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesByLocation {
        private String locationId;
        private String locationName;
        private BigDecimal amount;
        private int transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesByProduct {
        private String productType;
        private String cylinderSize;
        private int quantity;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingBucket {
        private String bucket;
        private BigDecimal amount;
    }
}
