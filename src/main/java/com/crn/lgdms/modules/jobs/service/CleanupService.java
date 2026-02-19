package com.crn.lgdms.modules.jobs.service;

import com.crn.lgdms.common.security.Jwt.TokenBlacklistService;
import com.crn.lgdms.modules.inventory.repository.StockSnapshotRepository;
import com.crn.lgdms.modules.masterdata.service.PriceCategoryService;
import com.crn.lgdms.modules.users.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final AuditLogRepository auditLogRepository;
    private final PriceCategoryService priceCategoryService;
    private final StockSnapshotRepository stockSnapshotRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public void cleanupAuditLogs(int daysToKeep) {
        log.info("Cleaning up audit logs older than {} days", daysToKeep);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);

        // For large tables, would use batch deletion
        // This is simplified - in production use pagination
        var oldLogs = auditLogRepository.findByDateRange(
            LocalDateTime.MIN, cutoffDate, PageRequest.of(0, 1000));

        if (oldLogs.hasContent()) {
            auditLogRepository.deleteAll(oldLogs.getContent());
            log.info("Deleted {} old audit logs", oldLogs.getTotalElements());
        }
    }

    @Transactional
    public void cleanupExpiredPrices() {
        log.info("Cleaning up expired price categories");

        int count = priceCategoryService.deactivateExpiredPrices();
        log.info("Deactivated {} expired price categories", count);
    }

    @Transactional
    public void cleanupOldSnapshots(int daysToKeep) {
        log.info("Cleaning up stock snapshots older than {} days", daysToKeep);

        LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);

        // In production, would use batch deletion
        var oldSnapshots = stockSnapshotRepository.findBySnapshotDate(cutoffDate);

        if (!oldSnapshots.isEmpty()) {
            stockSnapshotRepository.deleteAll(oldSnapshots);
            log.info("Deleted {} old stock snapshots", oldSnapshots.size());
        }
    }

    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens");

        // Token cleanup is handled by Redis TTL
        // This method is just for logging
        log.debug("Expired tokens automatically cleaned by Redis");
    }

    public void cleanupTempFiles() {
        log.info("Cleaning up temporary files");

        String tmpDir = System.getProperty("java.io.tmpdir");
        File tempFolder = new File(tmpDir);

        if (tempFolder.exists() && tempFolder.isDirectory()) {
            File[] oldFiles = tempFolder.listFiles(file ->
                file.isFile() &&
                    file.getName().startsWith("lgdms-") &&
                    file.lastModified() < System.currentTimeMillis() - 86400000 // 24 hours
            );

            if (oldFiles != null) {
                for (File file : oldFiles) {
                    if (file.delete()) {
                        log.debug("Deleted temp file: {}", file.getName());
                    }
                }
            }
        }
    }
}
