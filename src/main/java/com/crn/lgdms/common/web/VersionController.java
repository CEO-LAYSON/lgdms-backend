package com.crn.lgdms.common.web;

import com.crn.lgdms.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/version")
public class VersionController {

    @Value("${spring.application.name:lgdms}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> version() {
        Map<String, String> versionInfo = new HashMap<>();
        versionInfo.put("name", appName);
        versionInfo.put("version", appVersion);
        versionInfo.put("javaVersion", System.getProperty("java.version"));

        return ResponseEntity.ok(ApiResponse.success(versionInfo));
    }
}
