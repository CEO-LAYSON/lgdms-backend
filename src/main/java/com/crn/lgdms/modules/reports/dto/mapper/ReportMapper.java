package com.crn.lgdms.modules.reports.dto.mapper;

import com.crn.lgdms.modules.reports.dto.response.ReportRowResponse;
import com.crn.lgdms.modules.reports.dto.response.ReportSummaryResponse;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportSummaryResponse toSalesReport(
        String reportName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String locationFilter,
        List<Map<String, Object>> data) {

        ReportSummaryResponse.ReportFilterResponse filters =
            ReportSummaryResponse.ReportFilterResponse.builder()
                .dateRange(startDate.format(DATE_FORMATTER) + " to " +
                    endDate.format(DATE_FORMATTER))
                .location(locationFilter)
                .build();

        // Calculate summary
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        List<ReportRowResponse> rows = new ArrayList<>();

        for (Map<String, Object> row : data) {
            totalAmount = totalAmount.add((BigDecimal) row.getOrDefault("amount", BigDecimal.ZERO));
            totalQuantity += (Integer) row.getOrDefault("quantity", 0);

            rows.add(ReportRowResponse.builder()
                .columns(row)
                .build());
        }

        BigDecimal averageAmount = totalQuantity > 0 ?
            totalAmount.divide(BigDecimal.valueOf(totalQuantity), 2, BigDecimal.ROUND_HALF_UP) :
            BigDecimal.ZERO;

        ReportSummaryResponse.SummaryData summary =
            ReportSummaryResponse.SummaryData.builder()
                .totalRecords(data.size())
                .totalAmount(totalAmount)
                .totalQuantity(totalQuantity)
                .averageAmount(averageAmount)
                .subtotals(new HashMap<>())
                .build();

        return ReportSummaryResponse.builder()
            .reportName(reportName)
            .reportType("SALES")
            .generatedAt(LocalDateTime.now().format(DATE_FORMATTER))
            .filters(filters)
            .summary(summary)
            .rows(rows)
            .charts(new ArrayList<>())
            .build();
    }

    public ReportSummaryResponse toStockReport(
        String reportName,
        String locationFilter,
        List<Map<String, Object>> data) {

        ReportSummaryResponse.ReportFilterResponse filters =
            ReportSummaryResponse.ReportFilterResponse.builder()
                .location(locationFilter)
                .build();

        int totalFull = 0;
        int totalEmpty = 0;
        BigDecimal totalValue = BigDecimal.ZERO;

        List<ReportRowResponse> rows = new ArrayList<>();

        for (Map<String, Object> row : data) {
            totalFull += (Integer) row.getOrDefault("fullQuantity", 0);
            totalEmpty += (Integer) row.getOrDefault("emptyQuantity", 0);
            totalValue = totalValue.add((BigDecimal) row.getOrDefault("value", BigDecimal.ZERO));

            rows.add(ReportRowResponse.builder()
                .columns(row)
                .build());
        }

        Map<String, BigDecimal> subtotals = new HashMap<>();
        subtotals.put("totalFull", BigDecimal.valueOf(totalFull));
        subtotals.put("totalEmpty", BigDecimal.valueOf(totalEmpty));
        subtotals.put("totalValue", totalValue);

        ReportSummaryResponse.SummaryData summary =
            ReportSummaryResponse.SummaryData.builder()
                .totalRecords(data.size())
                .totalAmount(totalValue)
                .totalQuantity(totalFull + totalEmpty)
                .subtotals(subtotals)
                .build();

        return ReportSummaryResponse.builder()
            .reportName(reportName)
            .reportType("STOCK")
            .generatedAt(LocalDateTime.now().format(DATE_FORMATTER))
            .filters(filters)
            .summary(summary)
            .rows(rows)
            .charts(new ArrayList<>())
            .build();
    }
}
