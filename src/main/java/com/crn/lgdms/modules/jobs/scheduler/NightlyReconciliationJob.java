package com.crn.lgdms.modules.jobs.scheduler;

import com.crn.lgdms.modules.credit.repository.CreditAccountRepository;
import com.crn.lgdms.modules.inventory.service.ReconciliationService;
import com.crn.lgdms.modules.jobs.service.ReconciliationJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NightlyReconciliationJob {

    private final ReconciliationJobService reconciliationJobService;
    private final ReconciliationService reconciliationService;
    private final CreditAccountRepository creditAccountRepository;

    /**
     * Run every day at 2:00 AM
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runNightlyReconciliation() {
        log.info("Starting nightly reconciliation job");

        try {
            // Run stock reconciliation
            reconciliationJobService.reconcileStock();

            // Run empty cylinder reconciliation
            reconciliationJobService.reconcileEmptyCylinders();

            // Check for accounts over credit limit
            reconciliationJobService.checkCreditLimits();

            // Generate daily snapshots
            reconciliationJobService.generateDailySnapshots();

            log.info("Nightly reconciliation job completed successfully");
        } catch (Exception e) {
            log.error("Nightly reconciliation job failed", e);
        }
    }
}
