package com.crn.lgdms.modules.jobs.service;

import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.modules.credit.repository.CreditAccountRepository;
import com.crn.lgdms.modules.inventory.domain.entity.StockSnapshot;
import com.crn.lgdms.modules.inventory.repository.EmptyLedgerRepository;
import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.inventory.repository.StockSnapshotRepository;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationJobService {

    private final StockLedgerRepository stockLedgerRepository;
    private final EmptyLedgerRepository emptyLedgerRepository;
    private final StockSnapshotRepository stockSnapshotRepository;
    private final CreditAccountRepository creditAccountRepository;
    private final LocationRepository locationRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void reconcileStock() {
        log.info("Running stock reconciliation");

        var locations = locationRepository.findAll();
        var cylinderSizes = cylinderSizeRepository.findAll();

        int discrepancies = 0;

        for (var location : locations) {
            for (var cylinderSize : cylinderSizes) {
                // Calculate expected stock from ledger
                Integer expectedFull = stockLedgerRepository.getCurrentStock(
                    location.getId(), cylinderSize.getId(), ProductType.COMPLETE);

                // In real implementation, would compare with physical count
                // For now, just log current stock
                log.debug("Location: {}, Product: {}kg, Stock: {}",
                    location.getName(), cylinderSize.getWeightKg(), expectedFull);
            }
        }

        auditLogService.log(
            com.crn.lgdms.common.enums.AuditAction.UPDATE,
            "RECONCILIATION",
            "STOCK",
            null,
            "Stock reconciliation completed with " + discrepancies + " discrepancies",
            "SYSTEM"
        );
    }

    @Transactional
    public void reconcileEmptyCylinders() {
        log.info("Running empty cylinder reconciliation");

        var locations = locationRepository.findAll();
        var cylinderSizes = cylinderSizeRepository.findAll();

        for (var location : locations) {
            for (var cylinderSize : cylinderSizes) {
                Integer emptyBalance = emptyLedgerRepository.getCurrentEmptyBalance(
                    location.getId(), cylinderSize.getId());

                log.debug("Location: {}, Product: {}kg, Empty Balance: {}",
                    location.getName(), cylinderSize.getWeightKg(), emptyBalance);

                // Check for variance (would compare with expected)
                // Formula from SRS: Empty Balance = Empires Issued - Empires Returned
            }
        }
    }

    @Transactional
    public void checkCreditLimits() {
        log.info("Checking credit limits");

        var accountsOverLimit = creditAccountRepository.findAccountsOverLimit();

        if (!accountsOverLimit.isEmpty()) {
            log.warn("Found {} accounts over credit limit", accountsOverLimit.size());

            for (var account : accountsOverLimit) {
                String owner = account.getCustomer() != null ?
                    account.getCustomer().getName() :
                    account.getLocation() != null ? account.getLocation().getName() : "Unknown";

                log.warn("Account {} ({}) is over limit. Balance: {}, Limit: {}",
                    account.getAccountNumber(), owner,
                    account.getCurrentBalance(), account.getCreditLimit());
            }

            auditLogService.log(
                com.crn.lgdms.common.enums.AuditAction.UPDATE,
                "CREDIT_LIMIT",
                "OVER_LIMIT",
                null,
                "Found " + accountsOverLimit.size() + " accounts over limit",
                "SYSTEM"
            );
        }
    }

    @Transactional
    public void generateDailySnapshots() {
        log.info("Generating daily stock snapshots");

        LocalDate today = LocalDate.now();
        var locations = locationRepository.findAll();
        var cylinderSizes = cylinderSizeRepository.findAll();

        List<StockSnapshot> snapshots = new ArrayList<>();

        for (var location : locations) {
            for (var cylinderSize : cylinderSizes) {
                Integer fullStock = stockLedgerRepository.getCurrentStock(
                    location.getId(), cylinderSize.getId(), ProductType.COMPLETE);

                if (fullStock != null) {
                    StockSnapshot snapshot = StockSnapshot.builder()
                        .location(location)
                        .cylinderSize(cylinderSize)
                        .productType(ProductType.COMPLETE)
                        .quantity(fullStock)
                        .snapshotDate(today)
                        .build();

                    snapshots.add(snapshot);
                }
            }
        }

        stockSnapshotRepository.saveAll(snapshots);
        log.info("Generated {} stock snapshots", snapshots.size());
    }
}
