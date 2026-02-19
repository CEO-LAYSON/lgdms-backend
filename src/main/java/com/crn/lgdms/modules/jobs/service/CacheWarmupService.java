package com.crn.lgdms.modules.jobs.service;

import com.crn.lgdms.modules.dashboard.service.DashboardService;
import com.crn.lgdms.modules.locations.service.LocationService;
import com.crn.lgdms.modules.masterdata.service.CylinderSizeService;
import com.crn.lgdms.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheWarmupService {

    private final LocationService locationService;
    private final CylinderSizeService cylinderSizeService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final CacheManager cacheManager;

    public void warmupLocationCache() {
        log.info("Warming up location cache");

        // Load all active locations into cache
        locationService.getAllLocations(null, true);

        // Load specific location types
        locationService.getLocationsByType(com.crn.lgdms.common.enums.LocationType.HQ);
        locationService.getLocationsByType(com.crn.lgdms.common.enums.LocationType.BRANCH);
        locationService.getLocationsByType(com.crn.lgdms.common.enums.LocationType.VEHICLE);
    }

    public void warmupProductCache() {
        log.info("Warming up product cache");

        // Load all cylinder sizes
        cylinderSizeService.getAllCylinderSizes(false);
    }

    public void warmupUserCache() {
        log.info("Warming up user cache");

        // Load first page of users
        userService.getAllUsers(PageRequest.of(0, 20));
    }

    public void warmupDashboardCache() {
        log.info("Warming up dashboard cache");

        // Load dashboard KPIs
        dashboardService.getDashboardKpis();
    }

    public void refreshAlertData() {
        log.info("Refreshing alert data in cache");

        // Evict dashboard cache to force refresh
        cacheManager.getCache("dashboard").clear();

        // Reload with fresh data
        dashboardService.getDashboardKpis();
    }
}
