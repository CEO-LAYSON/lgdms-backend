package com.crn.lgdms.modules.jobs.scheduler;

import com.crn.lgdms.modules.jobs.service.CleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupJob {

    private final CleanupService cleanupService;

    /**
     * Run every day at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldData() {
        log.info("Starting data cleanup job");

        try {
            // Clean up old audit logs (older than 90 days)
            cleanupService.cleanupAuditLogs(90);

            // Clean up expired price categories
            cleanupService.cleanupExpiredPrices();

            // Clean up old stock snapshots (keep last 365 days)
            cleanupService.cleanupOldSnapshots(365);

            // Clean up expired JWT tokens
            cleanupService.cleanupExpiredTokens();

            log.info("Data cleanup completed");
        } catch (Exception e) {
            log.error("Data cleanup failed", e);
        }
    }

    /**
     * Run every hour to clean up temporary files
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupTempFiles() {
        log.debug("Cleaning up temporary files");
        cleanupService.cleanupTempFiles();
    }
}
