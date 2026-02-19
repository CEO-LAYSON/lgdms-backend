package com.crn.lgdms.modules.jobs.scheduler;

import com.crn.lgdms.modules.jobs.service.CacheWarmupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmupJob {

    private final CacheWarmupService cacheWarmupService;

    /**
     * Run every hour to keep cache warm
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void warmupCache() {
        log.info("Starting cache warmup job");

        try {
            cacheWarmupService.warmupLocationCache();
            cacheWarmupService.warmupProductCache();
            cacheWarmupService.warmupUserCache();
            cacheWarmupService.warmupDashboardCache();

            log.info("Cache warmup completed");
        } catch (Exception e) {
            log.error("Cache warmup failed", e);
        }
    }

    /**
     * Run every 5 minutes to refresh critical data
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void refreshCriticalCache() {
        log.debug("Refreshing critical cache data");
        cacheWarmupService.refreshAlertData();
    }
}
