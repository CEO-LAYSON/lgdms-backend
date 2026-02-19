package com.crn.lgdms.modules.dashboard.dto.mapper;

import com.crn.lgdms.modules.dashboard.dto.response.AlertResponse;
import com.crn.lgdms.modules.dashboard.dto.response.DashboardKpiResponse;
import com.crn.lgdms.modules.dashboard.dto.response.LocationStockSummaryResponse;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardMapper {

    public DashboardKpiResponse toKpiResponse(
        DashboardKpiResponse.StockSummary stockSummary,
        DashboardKpiResponse.SalesSummary salesSummary,
        DashboardKpiResponse.CreditSummary creditSummary,
        List<AlertResponse> alerts,
        List<LocationStockSummaryResponse> locationStock) {

        return DashboardKpiResponse.builder()
            .stockSummary(stockSummary)
            .salesSummary(salesSummary)
            .creditSummary(creditSummary)
            .alerts(alerts)
            .locationStock(locationStock)
            .build();
    }

    public LocationStockSummaryResponse toLocationStockSummary(
        String locationId, String locationName, String locationType,
        List<LocationStockSummaryResponse.ProductStock> productStocks) {

        int totalFull = productStocks.stream()
            .mapToInt(LocationStockSummaryResponse.ProductStock::getFullQuantity)
            .sum();

        int totalEmpty = productStocks.stream()
            .mapToInt(LocationStockSummaryResponse.ProductStock::getEmptyQuantity)
            .sum();

        int lowStockCount = (int) productStocks.stream()
            .filter(LocationStockSummaryResponse.ProductStock::isLowStock)
            .count();

        return LocationStockSummaryResponse.builder()
            .locationId(locationId)
            .locationName(locationName)
            .locationType(locationType)
            .productStocks(productStocks)
            .totalFull(totalFull)
            .totalEmpty(totalEmpty)
            .lowStockCount(lowStockCount)
            .build();
    }

    public AlertResponse toAlertResponse(
        String type, String severity, String title, String message,
        String locationId, String locationName, String action) {

        return AlertResponse.builder()
            .id(java.util.UUID.randomUUID().toString())
            .type(type)
            .severity(severity)
            .title(title)
            .message(message)
            .locationId(locationId)
            .locationName(locationName)
            .timestamp(LocalDateTime.now())
            .acknowledged(false)
            .action(action)
            .build();
    }
}
