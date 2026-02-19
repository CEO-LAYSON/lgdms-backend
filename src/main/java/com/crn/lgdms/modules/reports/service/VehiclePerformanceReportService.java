package com.crn.lgdms.modules.reports.service;

import com.crn.lgdms.modules.locations.repository.LocationRepository;
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
public class VehiclePerformanceReportService {

    private final LocationRepository locationRepository;
    private final SaleRepository saleRepository;
    private final ReportMapper reportMapper;

    public ReportSummaryResponse generateVehiclePerformanceReport(ReportFilterRequest request) {
        log.info("Generating vehicle performance report");

        LocalDateTime startDateTime = request.getStartDate() != null ?
            request.getStartDate().atStartOfDay() :
            LocalDate.now().minusDays(30).atStartOfDay();

        LocalDateTime endDateTime = request.getEndDate() != null ?
            request.getEndDate().atTime(LocalTime.MAX) :
            LocalDateTime.now();

        List<Map<String, Object>> reportData = new ArrayList<>();

        // Get all vehicles
        var vehicles = locationRepository.findByLocationType(
            com.crn.lgdms.common.enums.LocationType.VEHICLE);

        for (var vehicle : vehicles) {
            var sales = saleRepository.findByLocationId(vehicle.getId());

            BigDecimal totalSales = BigDecimal.ZERO;
            int transactionCount = 0;

            for (var sale : sales) {
                if (sale.getSaleDate().isBefore(startDateTime.toLocalDate()) ||
                    sale.getSaleDate().isAfter(endDateTime.toLocalDate())) {
                    continue;
                }
                totalSales = totalSales.add(sale.getTotalAmount());
                transactionCount++;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("vehicle", vehicle.getName());
            row.put("registration", vehicle.getVehicleRegistration());
            row.put("transactions", transactionCount);
            row.put("totalSales", totalSales);
            row.put("averagePerTrip", transactionCount > 0 ?
                totalSales.divide(BigDecimal.valueOf(transactionCount), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO);
            row.put("capacity", vehicle.getVehicleCapacity());

            reportData.add(row);
        }

        return reportMapper.toStockReport(
            "Vehicle Performance Report",
            "All Vehicles",
            reportData
        );
    }
}
