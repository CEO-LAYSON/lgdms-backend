package com.crn.lgdms.modules.dashboard.service;

import com.crn.lgdms.common.enums.LocationType;
import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.modules.credit.repository.CreditAccountRepository;
import com.crn.lgdms.modules.credit.repository.CreditTransactionRepository;
import com.crn.lgdms.modules.dashboard.dto.response.AlertResponse;
import com.crn.lgdms.modules.dashboard.dto.response.DashboardKpiResponse;
import com.crn.lgdms.modules.dashboard.dto.response.LocationStockSummaryResponse;
import com.crn.lgdms.modules.dashboard.dto.mapper.DashboardMapper;
import com.crn.lgdms.modules.inventory.dto.response.EmptyBalanceResponse;   // NEW
import com.crn.lgdms.modules.inventory.dto.response.OnHandResponse;          // NEW
import com.crn.lgdms.modules.inventory.repository.EmptyLedgerRepository;
import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.inventory.service.EmptyBalanceService;          // NEW
import com.crn.lgdms.modules.inventory.service.StockQueryService;            // NEW
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.sales.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StockLedgerRepository stockLedgerRepository;
    private final EmptyLedgerRepository emptyLedgerRepository;
    private final SaleRepository saleRepository;
    private final CreditAccountRepository creditAccountRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final LocationRepository locationRepository;
    private final CylinderSizeRepository cylinderSizeRepository;

    // NEW: Add inventory services
    private final StockQueryService stockQueryService;
    private final EmptyBalanceService emptyBalanceService;

    private final DashboardMapper dashboardMapper;

    @Cacheable(value = "dashboard", key = "'kpi'")
    public DashboardKpiResponse getDashboardKpis() {
        log.info("Generating dashboard KPIs");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        LocalDateTime startOfWeek = today.minusDays(7).atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfYear = today.withDayOfYear(1).atStartOfDay();

        // Stock Summary - NOW USING REAL DATA
        DashboardKpiResponse.StockSummary stockSummary = buildStockSummary();

        // Sales Summary
        DashboardKpiResponse.SalesSummary salesSummary = buildSalesSummary(
            startOfDay, endOfDay, startOfWeek, startOfMonth, startOfYear);

        // Credit Summary
        DashboardKpiResponse.CreditSummary creditSummary = buildCreditSummary();

        // Alerts - NOW USING REAL DATA
        List<AlertResponse> alerts = buildAlerts();

        // Location Stock - NOW USING REAL DATA
        List<LocationStockSummaryResponse> locationStock = buildLocationStockSummary();

        return dashboardMapper.toKpiResponse(
            stockSummary, salesSummary, creditSummary, alerts, locationStock);
    }

    /**
     * UPDATED: Uses real data from StockQueryService
     */
    private DashboardKpiResponse.StockSummary buildStockSummary() {
        List<Location> locations = locationRepository.findAll();

        int totalFull = 0;
        int totalEmpty = 0;
        BigDecimal totalValue = BigDecimal.ZERO;
        int lowStockItems = 0;
        int outOfStockItems = 0;

        List<DashboardKpiResponse.StockByLocation> stockByLocation = new ArrayList<>();
        Map<String, DashboardKpiResponse.StockByProduct> stockByProductMap = new HashMap<>();

        for (Location location : locations) {
            // Use StockQueryService to get real on-hand stock
            List<OnHandResponse> locationStock = stockQueryService.getOnHandStock(location.getId());

            int locationFull = 0;
            int locationEmpty = 0;

            for (OnHandResponse item : locationStock) {
                if (item.getProductType() == ProductType.COMPLETE) {
                    locationFull += item.getQuantity();
                    totalFull += item.getQuantity();

                    if (item.isLowStock()) lowStockItems++;
                    if (item.isOutOfStock()) outOfStockItems++;

                    // Update product summary
                    String key = item.getCylinderSizeName() + "_COMPLETE";
                    DashboardKpiResponse.StockByProduct product = stockByProductMap.get(key);
                    if (product == null) {
                        product = DashboardKpiResponse.StockByProduct.builder()
                            .cylinderSize(item.getCylinderSizeName())
                            .fullQuantity(item.getQuantity())
                            .emptyQuantity(0)
                            .value(calculateStockValue(item))
                            .build();
                        stockByProductMap.put(key, product);
                    } else {
                        product.setFullQuantity(product.getFullQuantity() + item.getQuantity());
                        product.setValue(product.getValue().add(calculateStockValue(item)));
                    }
                } else {
                    locationEmpty += item.getQuantity();
                    totalEmpty += item.getQuantity();

                    // Update product summary
                    String key = item.getCylinderSizeName() + "_EMPTY";
                    DashboardKpiResponse.StockByProduct product = stockByProductMap.get(key);
                    if (product == null) {
                        product = DashboardKpiResponse.StockByProduct.builder()
                            .cylinderSize(item.getCylinderSizeName())
                            .fullQuantity(0)
                            .emptyQuantity(item.getQuantity())
                            .value(BigDecimal.ZERO)
                            .build();
                        stockByProductMap.put(key, product);
                    } else {
                        product.setEmptyQuantity(product.getEmptyQuantity() + item.getQuantity());
                    }
                }
            }

            stockByLocation.add(DashboardKpiResponse.StockByLocation.builder()
                .locationId(location.getId())
                .locationName(location.getName())
                .locationType(location.getLocationType().toString())
                .fullCylinders(locationFull)
                .emptyCylinders(locationEmpty)
                .build());
        }

        // Get total stock value
        totalValue = BigDecimal.valueOf(stockQueryService.getTotalStockValue());

        return DashboardKpiResponse.StockSummary.builder()
            .totalFullCylinders(totalFull)
            .totalEmptyCylinders(totalEmpty)
            .lowStockItems(lowStockItems)
            .outOfStockItems(outOfStockItems)
            .totalStockValue(totalValue.doubleValue())
            .stockByLocation(stockByLocation)
            .stockByProduct(new ArrayList<>(stockByProductMap.values()))
            .build();
    }

    private BigDecimal calculateStockValue(OnHandResponse item) {
        // In real implementation, would use actual cost from receiving
        double unitValue = switch(item.getCylinderSizeName()) {
            case "3kg" -> item.getProductType() == ProductType.COMPLETE ? 35000 : 25000;
            case "6kg" -> item.getProductType() == ProductType.COMPLETE ? 65000 : 50000;
            case "15kg" -> item.getProductType() == ProductType.COMPLETE ? 150000 : 120000;
            case "38kg" -> item.getProductType() == ProductType.COMPLETE ? 350000 : 280000;
            case "100kg" -> item.getProductType() == ProductType.COMPLETE ? 850000 : 700000;
            default -> 50000;
        };
        return BigDecimal.valueOf(item.getQuantity() * unitValue);
    }

    private DashboardKpiResponse.SalesSummary buildSalesSummary(
        LocalDateTime startOfDay, LocalDateTime endOfDay,
        LocalDateTime startOfWeek, LocalDateTime startOfMonth,
        LocalDateTime startOfYear) {

        BigDecimal todaySales = saleRepository.getTotalSalesForPeriod(
            startOfDay.toLocalDate(), endOfDay.toLocalDate());
        if (todaySales == null) todaySales = BigDecimal.ZERO;

        int todayTransactions = saleRepository.findByDateRange(
            startOfDay.toLocalDate(), endOfDay.toLocalDate()).size();

        BigDecimal weekSales = saleRepository.getTotalSalesForPeriod(
            startOfWeek.toLocalDate(), endOfDay.toLocalDate());
        if (weekSales == null) weekSales = BigDecimal.ZERO;

        BigDecimal monthSales = saleRepository.getTotalSalesForPeriod(
            startOfMonth.toLocalDate(), endOfDay.toLocalDate());
        if (monthSales == null) monthSales = BigDecimal.ZERO;

        BigDecimal yearSales = saleRepository.getTotalSalesForPeriod(
            startOfYear.toLocalDate(), endOfDay.toLocalDate());
        if (yearSales == null) yearSales = BigDecimal.ZERO;

        // Daily sales for last 7 days
        List<DashboardKpiResponse.DailySales> dailySales = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            BigDecimal dayAmount = saleRepository.getTotalSalesForPeriod(date, date);
            int dayTransactions = saleRepository.findByDateRange(date, date).size();

            dailySales.add(DashboardKpiResponse.DailySales.builder()
                .date(date.toString())
                .amount(dayAmount != null ? dayAmount.doubleValue() : 0.0)
                .transactions(dayTransactions)
                .build());
        }

        // Sales by location
        List<DashboardKpiResponse.SalesByLocation> salesByLocation = new ArrayList<>();
        var locations = locationRepository.findAll();
        for (Location location : locations) {
            var sales = saleRepository.findByLocationId(location.getId());
            BigDecimal locationAmount = sales.stream()
                .map(s -> s.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            salesByLocation.add(DashboardKpiResponse.SalesByLocation.builder()
                .locationId(location.getId())
                .locationName(location.getName())
                .amount(locationAmount.doubleValue())
                .transactions(sales.size())
                .build());
        }

        return DashboardKpiResponse.SalesSummary.builder()
            .todaySales(todaySales.doubleValue())
            .todayTransactions(todayTransactions)
            .weekSales(weekSales.doubleValue())
            .monthSales(monthSales.doubleValue())
            .yearSales(yearSales.doubleValue())
            .dailySales(dailySales)
            .salesByLocation(salesByLocation)
            .build();
    }

    private DashboardKpiResponse.CreditSummary buildCreditSummary() {
        BigDecimal totalOutstanding = creditAccountRepository.getTotalOutstandingCredit();
        if (totalOutstanding == null) totalOutstanding = BigDecimal.ZERO;

        List<Object[]> outstandingByType = creditAccountRepository.getOutstandingByAccountType();

        List<DashboardKpiResponse.AgingBucket> agingBuckets = new ArrayList<>();

        // Simple aging buckets (in real implementation, would calculate from transactions)
        agingBuckets.add(DashboardKpiResponse.AgingBucket.builder()
            .bucket("Current")
            .amount(totalOutstanding.multiply(BigDecimal.valueOf(0.6)).doubleValue())
            .build());
        agingBuckets.add(DashboardKpiResponse.AgingBucket.builder()
            .bucket("1-30 days")
            .amount(totalOutstanding.multiply(BigDecimal.valueOf(0.2)).doubleValue())
            .build());
        agingBuckets.add(DashboardKpiResponse.AgingBucket.builder()
            .bucket("31-60 days")
            .amount(totalOutstanding.multiply(BigDecimal.valueOf(0.1)).doubleValue())
            .build());
        agingBuckets.add(DashboardKpiResponse.AgingBucket.builder()
            .bucket("61-90 days")
            .amount(totalOutstanding.multiply(BigDecimal.valueOf(0.05)).doubleValue())
            .build());
        agingBuckets.add(DashboardKpiResponse.AgingBucket.builder()
            .bucket("90+ days")
            .amount(totalOutstanding.multiply(BigDecimal.valueOf(0.05)).doubleValue())
            .build());

        return DashboardKpiResponse.CreditSummary.builder()
            .totalOutstanding(totalOutstanding.doubleValue())
            .accountsOverLimit(creditAccountRepository.findAccountsOverLimit().size())
            .overdueAmount(totalOutstanding.multiply(BigDecimal.valueOf(0.2)).doubleValue()) // Simplified
            .overdueAccounts(5) // Placeholder
            .agingBuckets(agingBuckets)
            .build();
    }

    /**
     * UPDATED: Now uses real data from StockQueryService and EmptyBalanceService
     */
    private List<AlertResponse> buildAlerts() {
        List<AlertResponse> alerts = new ArrayList<>();

        // Get real low stock alerts from StockQueryService
        List<OnHandResponse> lowStockItems = stockQueryService.getLowStockItems();
        for (OnHandResponse item : lowStockItems) {
            String severity = item.isOutOfStock() ? "CRITICAL" : "WARNING";

            alerts.add(dashboardMapper.toAlertResponse(
                "LOW_STOCK",
                severity,
                "Low Stock Alert",
                String.format("%s has only %d %s %s remaining",
                    item.getLocationName(),
                    item.getQuantity(),
                    item.getCylinderSizeName(),
                    item.getProductType()),
                item.getLocationId(),
                item.getLocationName(),
                "Transfer stock from HQ or place order with supplier"
            ));
        }

        // Get real empty variance alerts from EmptyBalanceService
        List<EmptyBalanceResponse> variances = emptyBalanceService.getLocationsWithVariance();
        for (EmptyBalanceResponse variance : variances) {
            if (variance.getVariance() != 0) {
                alerts.add(dashboardMapper.toAlertResponse(
                    "EMPTY_VARIANCE",
                    variance.getVarianceStatus(),
                    "Empty Cylinder Variance",
                    String.format("%s has %d empty cylinder variance for %s",
                        variance.getLocationName(),
                        variance.getVariance(),
                        variance.getCylinderSizeName()),
                    variance.getLocationId(),
                    variance.getLocationName(),
                    "Perform reconciliation"
                ));
            }
        }

        // Credit limit alerts (keep as is)
        var accountsOverLimit = creditAccountRepository.findAccountsOverLimit();
        for (var account : accountsOverLimit) {
            String customerName = account.getCustomer() != null ?
                account.getCustomer().getName() :
                account.getLocation() != null ? account.getLocation().getName() : "Unknown";

            alerts.add(dashboardMapper.toAlertResponse(
                "CREDIT_LIMIT",
                "CRITICAL",
                "Credit Limit Exceeded",
                String.format("%s has exceeded credit limit. Balance: %s, Limit: %s",
                    customerName,
                    account.getCurrentBalance(),
                    account.getCreditLimit()),
                account.getLocation() != null ? account.getLocation().getId() : null,
                account.getLocation() != null ? account.getLocation().getName() : null,
                "Contact customer for payment or block account"
            ));
        }

        return alerts;
    }

    /**
     * UPDATED: Now uses real data from StockQueryService
     */
    private List<LocationStockSummaryResponse> buildLocationStockSummary() {
        List<LocationStockSummaryResponse> summaries = new ArrayList<>();

        var locations = locationRepository.findAll();
        var cylinderSizes = cylinderSizeRepository.findByIsActiveTrue(
            org.springframework.data.domain.Sort.by("displayOrder"));

        for (Location location : locations) {
            // Get real on-hand stock for this location
            List<OnHandResponse> locationStock = stockQueryService.getOnHandStock(location.getId());

            List<LocationStockSummaryResponse.ProductStock> productStocks = new ArrayList<>();

            for (var cylinderSize : cylinderSizes) {
                // Find matching items for this cylinder size
                OnHandResponse completeItem = locationStock.stream()
                    .filter(s -> s.getCylinderSizeId().equals(cylinderSize.getId()) &&
                        s.getProductType() == ProductType.COMPLETE)
                    .findFirst()
                    .orElse(null);

                OnHandResponse refillItem = locationStock.stream()
                    .filter(s -> s.getCylinderSizeId().equals(cylinderSize.getId()) &&
                        s.getProductType() == ProductType.REFILL)
                    .findFirst()
                    .orElse(null);

                int full = completeItem != null ? completeItem.getQuantity() : 0;
                int empty = refillItem != null ? refillItem.getQuantity() : 0;

                productStocks.add(LocationStockSummaryResponse.ProductStock.builder()
                    .cylinderSizeId(cylinderSize.getId())
                    .cylinderSizeName(cylinderSize.getName())
                    .fullQuantity(full)
                    .emptyQuantity(empty)
                    .minStockLevel(10)
                    .isLowStock(full < 10)
                    .build());
            }

            summaries.add(dashboardMapper.toLocationStockSummary(
                location.getId(),
                location.getName(),
                location.getLocationType().toString(),
                productStocks
            ));
        }

        return summaries;
    }

    private BigDecimal calculateStockValue(
        com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize cylinderSize,
        int quantity,
        ProductType productType) {
        // In real implementation, would use actual cost from receiving
        // For now, use approximate values
        BigDecimal unitValue = switch(cylinderSize.getName()) {
            case "3kg" -> BigDecimal.valueOf(35000);
            case "6kg" -> BigDecimal.valueOf(65000);
            case "15kg" -> BigDecimal.valueOf(150000);
            case "38kg" -> BigDecimal.valueOf(350000);
            case "100kg" -> BigDecimal.valueOf(850000);
            default -> BigDecimal.valueOf(50000);
        };

        if (productType == ProductType.REFILL) {
            unitValue = unitValue.multiply(BigDecimal.valueOf(0.7)); // Refill is cheaper
        }

        return unitValue.multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal calculateLocationStockValue(Location location) {
        BigDecimal total = BigDecimal.ZERO;
        var cylinderSizes = cylinderSizeRepository.findAll();

        for (var cylinderSize : cylinderSizes) {
            Integer fullStock = stockLedgerRepository.getCurrentStock(
                location.getId(), cylinderSize.getId(), ProductType.COMPLETE);
            if (fullStock != null) {
                total = total.add(calculateStockValue(cylinderSize, fullStock, ProductType.COMPLETE));
            }
        }

        return total;
    }
}
