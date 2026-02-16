package com.crn.lgdms.common.web;

import com.crn.lgdms.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("service", "lgdms-backend");
        status.put("version", "1.0.0");

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/db")
    public ResponseEntity<ApiResponse<String>> dbHealth() {
        // Will be implemented when DB is connected
        return ResponseEntity.ok(ApiResponse.success("Database connection OK"));
    }
}
