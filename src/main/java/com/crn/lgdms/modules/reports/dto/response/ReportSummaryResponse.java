package com.crn.lgdms.modules.reports.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryResponse {
    private String reportName;
    private String reportType;
    private String generatedAt;
    private ReportFilterResponse filters;
    private SummaryData summary;
    private List<ReportRowResponse> rows;
    private List<ChartData> charts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportFilterResponse {
        private String dateRange;
        private String location;
        private String customer;
        private String product;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryData {
        private int totalRecords;
        private BigDecimal totalAmount;
        private int totalQuantity;
        private BigDecimal averageAmount;
        private Map<String, BigDecimal> subtotals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String chartType;
        private String title;
        private List<String> labels;
        private List<Dataset> datasets;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dataset {
        private String label;
        private List<BigDecimal> data;
        private String color;
    }
}
