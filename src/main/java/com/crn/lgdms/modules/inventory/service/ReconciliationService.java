package com.crn.lgdms.modules.inventory.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.inventory.domain.entity.StockSnapshot;
import com.crn.lgdms.modules.inventory.dto.request.ReconcileRequest;
import com.crn.lgdms.modules.inventory.dto.response.ReconciliationResponse;
import com.crn.lgdms.modules.inventory.dto.mapper.StockSnapshotMapper;
import com.crn.lgdms.modules.inventory.repository.StockSnapshotRepository;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final StockSnapshotRepository stockSnapshotRepository;
    private final LocationRepository locationRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final StockQueryService stockQueryService;
    private final EmptyBalanceService emptyBalanceService;
    private final StockSnapshotMapper stockSnapshotMapper;
    private final AuditLogService auditLogService;

    private static final int MAX_ALLOWED_VARIANCE = 5;

    @Transactional
    public ReconciliationResponse performReconciliation(ReconcileRequest request, String username) {
        log.info("Performing stock reconciliation for location: {}", request.getLocationId());

        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new NotFoundException("Location not found: " + request.getLocationId()));

        List<ReconciliationResponse.ReconciliationEntryResponse> entries = new ArrayList<>();
        int totalVariance = 0;
        int mismatches = 0;

        for (ReconcileRequest.ReconcileEntry entry : request.getEntries()) {
            CylinderSize cylinderSize = cylinderSizeRepository.findById(entry.getCylinderSizeId())
                .orElseThrow(() -> new NotFoundException("Cylinder size not found: " + entry.getCylinderSizeId()));

            // Get system quantity
            Integer systemQuantity = stockQueryService.getCurrentStock(
                request.getLocationId(),
                entry.getCylinderSizeId(),
                entry.getProductType()
            );

            if (systemQuantity == null) systemQuantity = 0;

            // Calculate variance
            int variance = entry.getPhysicalCount() - systemQuantity;
            totalVariance += Math.abs(variance);

            boolean isMatch = variance == 0;
            if (!isMatch) mismatches++;

            // CRITICAL: Check if variance exceeds threshold
            if (Math.abs(variance) > MAX_ALLOWED_VARIANCE) {
                log.warn("Large variance detected: {} units for {} {} at {}",
                    variance, cylinderSize.getName(), entry.getProductType(), location.getName());
            }

            // Create snapshot
            StockSnapshot snapshot = StockSnapshot.builder()
                .location(location)
                .cylinderSize(cylinderSize)
                .productType(entry.getProductType())
                .quantity(entry.getPhysicalCount())
                .snapshotDate(request.getReconciliationDate())
                .build();

            stockSnapshotRepository.save(snapshot);

            // Create entry response
            ReconciliationResponse.ReconciliationEntryResponse entryResponse =
                ReconciliationResponse.ReconciliationEntryResponse.builder()
                    .cylinderSizeId(cylinderSize.getId())
                    .cylinderSizeName(cylinderSize.getName())
                    .productType(entry.getProductType())
                    .systemQuantity(systemQuantity)
                    .physicalQuantity(entry.getPhysicalCount())
                    .variance(variance)
                    .status(isMatch ? "MATCH" : "MISMATCH")
                    .notes(entry.getNotes())
                    .build();

            entries.add(entryResponse);
        }

        // Determine overall status
        String reconciliationStatus = mismatches == 0 ? "PASSED" :
            mismatches <= 2 ? "WARNING" : "FAILED";

        ReconciliationResponse.Summary summary = ReconciliationResponse.Summary.builder()
            .totalItems(entries.size())
            .matchingItems(entries.size() - mismatches)
            .mismatchingItems(mismatches)
            .totalVariance(totalVariance)
            .reconciliationStatus(reconciliationStatus)
            .build();

        ReconciliationResponse response = ReconciliationResponse.builder()
            .locationId(location.getId())
            .locationName(location.getName())
            .reconciliationDate(request.getReconciliationDate())
            .reconciledBy(username)
            .reconciledAt(LocalDateTime.now())
            .entries(entries)
            .summary(summary)
            .build();

        auditLogService.log(
            AuditAction.CREATE,
            "Reconciliation",
            location.getId(),
            null,
            String.format("Reconciliation completed at %s: %s", location.getName(), reconciliationStatus),
            username
        );

        log.info("Reconciliation completed for {}: {} items, {} mismatches",
            location.getName(), entries.size(), mismatches);

        return response;
    }

    @Transactional(readOnly = true)
    public List<ReconciliationResponse> getReconciliationHistory(String locationId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting reconciliation history for location: {}", locationId);

        List<StockSnapshot> snapshots = stockSnapshotRepository.findByLocationIdAndDateRange(
            locationId, startDate, endDate);

        // Group by date
        return snapshots.stream()
            .collect(Collectors.groupingBy(StockSnapshot::getSnapshotDate))
            .entrySet().stream()
            .map(entry -> {
                List<ReconciliationResponse.ReconciliationEntryResponse> entries =
                    entry.getValue().stream()
                        .map(stockSnapshotMapper::toEntryResponse)
                        .collect(Collectors.toList());

                int totalVariance = entries.stream()
                    .mapToInt(ReconciliationResponse.ReconciliationEntryResponse::getVariance)
                    .sum();

                long mismatches = entries.stream()
                    .filter(e -> !"MATCH".equals(e.getStatus()))
                    .count();

                ReconciliationResponse.Summary summary = ReconciliationResponse.Summary.builder()
                    .totalItems(entries.size())
                    .matchingItems((int) (entries.size() - mismatches))
                    .mismatchingItems((int) mismatches)
                    .totalVariance(Math.abs(totalVariance))
                    .reconciliationStatus(mismatches == 0 ? "PASSED" : "FAILED")
                    .build();

                Location location = locationRepository.findById(locationId).orElse(null);

                return ReconciliationResponse.builder()
                    .locationId(locationId)
                    .locationName(location != null ? location.getName() : null)
                    .reconciliationDate(entry.getKey())
                    .entries(entries)
                    .summary(summary)
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReconciliationResponse> getLocationsNeedingReconciliation() {
        log.debug("Finding locations that need reconciliation");

        List<Location> locations = locationRepository.findAll();
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        List<ReconciliationResponse> needingReconciliation = new ArrayList<>();

        for (Location location : locations) {
            List<StockSnapshot> latestSnapshots = stockSnapshotRepository.findLatestByLocation(location.getId());

            if (latestSnapshots.isEmpty()) {
                // Never reconciled
                needingReconciliation.add(createNeedsReconciliationResponse(location, "Never reconciled"));
                continue;
            }

            LocalDate latestDate = latestSnapshots.get(0).getSnapshotDate();
            if (latestDate.isBefore(thirtyDaysAgo)) {
                // Last reconciliation was more than 30 days ago
                needingReconciliation.add(createNeedsReconciliationResponse(
                    location, "Last reconciliation: " + latestDate));
            }
        }

        return needingReconciliation;
    }

    private ReconciliationResponse createNeedsReconciliationResponse(Location location, String reason) {
        return ReconciliationResponse.builder()
            .locationId(location.getId())
            .locationName(location.getName())
            .summary(ReconciliationResponse.Summary.builder()
                .reconciliationStatus("REQUIRED")
                .build())
            .build();
    }
}
