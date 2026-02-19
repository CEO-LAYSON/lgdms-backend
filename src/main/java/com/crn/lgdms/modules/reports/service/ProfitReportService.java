package com.crn.lgdms.modules.reports.service;

import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.reports.dto.request.ReportFilterRequest;
import com.crn.lgdms.modules.reports.dto.response.ReportSummaryResponse;
import com.crn.lgdms.modules.reports.dto.mapper.ReportMapper;
import com.crn.lgdms.modules.sales.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfitReportService {

    private final SaleRepository saleRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final ReportMapper reportMapper;

    public ReportSummaryResponse generateProfitReport(ReportFilterRequest request) {
        log.info("Generating profit report");

        LocalDateTime startDateTime = request.getStartDate() != null ?
            request.getStartDate().atStartOfDay() :
            LocalDate.now().minusDays(30).atStartOfDay();

        LocalDateTime endDateTime = request.getEndDate() != null ?
            request.getEndDate().atTime(LocalTime.MAX) :
            LocalDateTime.now();

        List<Map<String, Object>> reportData = new ArrayList<>();

        // Get sales for the period
        var sales = saleRepository.findByDateRange(
            startDateTime.toLocalDate(),
            endDateTime.toLocalDate());

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (var sale : sales) {
            totalRevenue = totalRevenue.add(sale.getTotalAmount());

            // Calculate cost of goods sold (simplified)
            for (var item : sale.getItems()) {
                // In real implementation, would get actual cost from stock ledger
                BigDecimal cost = item.getUnitPrice().multiply(BigDecimal.valueOf(0.6)); // 60% cost
                totalCost = totalCost.add(cost.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("period", startDateTime.toLocalDate() + " to " + endDateTime.toLocalDate());
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalCost", totalCost);
        summary.put("grossProfit", totalRevenue.subtract(totalCost));
        summary.put("profitMargin", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            (totalRevenue.subtract(totalCost)).divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

        reportData.add(summary);

        return reportMapper.toStockReport(
            "Profit & Margin Report",
            "All Locations",
            reportData
        );
    }
}
