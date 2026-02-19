package com.crn.lgdms.modules.reports.service;

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
public class SalesReportService {

    private final SaleRepository saleRepository;
    private final ReportMapper reportMapper;

    public ReportSummaryResponse generateSalesReport(ReportFilterRequest request) {
        log.info("Generating sales report with filters: {}", request);

        LocalDateTime startDateTime = request.getStartDate() != null ?
            request.getStartDate().atStartOfDay() :
            LocalDate.now().minusDays(30).atStartOfDay();

        LocalDateTime endDateTime = request.getEndDate() != null ?
            request.getEndDate().atTime(LocalTime.MAX) :
            LocalDateTime.now();

        List<Map<String, Object>> reportData = new ArrayList<>();

        // Query sales data based on filters
        var sales = saleRepository.findByDateRange(
            startDateTime.toLocalDate(),
            endDateTime.toLocalDate());

        for (var sale : sales) {
            if (request.getLocationId() != null &&
                !sale.getLocation().getId().equals(request.getLocationId())) {
                continue;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("date", sale.getSaleDate());
            row.put("invoice", sale.getInvoiceNumber());
            row.put("location", sale.getLocation().getName());
            row.put("customer", sale.getCustomer() != null ?
                sale.getCustomer().getName() : "Walk-in");
            row.put("items", sale.getItems().size());
            row.put("amount", sale.getTotalAmount());
            row.put("payment", sale.getPaymentMethod());
            row.put("status", sale.getStatus());

            reportData.add(row);
        }

        String locationFilter = request.getLocationId() != null ?
            saleRepository.findById(request.getLocationId())
                .map(s -> s.getLocation().getName())
                .orElse("All Locations") : "All Locations";

        return reportMapper.toSalesReport(
            "Sales Report",
            startDateTime,
            endDateTime,
            locationFilter,
            reportData
        );
    }

    public ReportSummaryResponse generateSalesByProductReport(ReportFilterRequest request) {
        log.info("Generating sales by product report");

        LocalDateTime startDateTime = request.getStartDate() != null ?
            request.getStartDate().atStartOfDay() :
            LocalDate.now().minusDays(30).atStartOfDay();

        LocalDateTime endDateTime = request.getEndDate() != null ?
            request.getEndDate().atTime(LocalTime.MAX) :
            LocalDateTime.now();

        List<Map<String, Object>> reportData = new ArrayList<>();

        // Group by product
        var sales = saleRepository.findByDateRange(
            startDateTime.toLocalDate(),
            endDateTime.toLocalDate());

        Map<String, Map<String, Object>> productGroups = new HashMap<>();

        for (var sale : sales) {
            if (request.getLocationId() != null &&
                !sale.getLocation().getId().equals(request.getLocationId())) {
                continue;
            }

            for (var item : sale.getItems()) {
                String key = item.getCylinderSize().getName() + "_" + item.getProductType();

                Map<String, Object> group = productGroups.computeIfAbsent(key, k -> {
                    Map<String, Object> newGroup = new HashMap<>();
                    newGroup.put("product", item.getCylinderSize().getName());
                    newGroup.put("type", item.getProductType());
                    newGroup.put("quantity", 0);
                    newGroup.put("amount", BigDecimal.ZERO);
                    return newGroup;
                });

                group.put("quantity", (Integer) group.get("quantity") + item.getQuantity());
                group.put("amount", ((BigDecimal) group.get("amount")).add(item.getTotalPrice()));
            }
        }

        reportData.addAll(productGroups.values());

        return reportMapper.toSalesReport(
            "Sales by Product Report",
            startDateTime,
            endDateTime,
            "All Locations",
            reportData
        );
    }
}
