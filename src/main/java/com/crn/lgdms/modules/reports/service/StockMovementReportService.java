package com.crn.lgdms.modules.reports.service;

import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.reports.dto.request.ReportFilterRequest;
import com.crn.lgdms.modules.reports.dto.response.ReportSummaryResponse;
import com.crn.lgdms.modules.reports.dto.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMovementReportService {

    private final StockLedgerRepository stockLedgerRepository;
    private final ReportMapper reportMapper;

    public ReportSummaryResponse generateStockMovementReport(ReportFilterRequest request) {
        log.info("Generating stock movement report");

        LocalDateTime startDateTime = request.getStartDate() != null ?
            request.getStartDate().atStartOfDay() :
            LocalDateTime.now().minusDays(30);

        LocalDateTime endDateTime = request.getEndDate() != null ?
            request.getEndDate().atTime(LocalTime.MAX) :
            LocalDateTime.now();

        var movements = stockLedgerRepository.findByDateRange(
            startDateTime, endDateTime,
            org.springframework.data.domain.Pageable.unpaged());

        List<Map<String, Object>> reportData = new ArrayList<>();

        for (var movement : movements) {
            if (request.getLocationId() != null &&
                !movement.getLocation().getId().equals(request.getLocationId())) {
                continue;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("date", movement.getTransactionDate());
            row.put("location", movement.getLocation().getName());
            row.put("product", movement.getCylinderSize().getName());
            row.put("type", movement.getProductType());
            row.put("movement", movement.getMovementType());
            row.put("quantity", movement.getQuantity());
            row.put("balance", movement.getRunningBalance());
            row.put("reference", movement.getReferenceNumber());

            reportData.add(row);
        }

        return reportMapper.toStockReport(
            "Stock Movement Report",
            request.getLocationId() != null ? "Filtered by location" : "All Locations",
            reportData
        );
    }
}
