package com.jromax.bcisecurityuserservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Slf4j
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check requested");
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("service", "bci-security-user-service");
            response.put("message", "Service is running correctly");
            
            log.info("Health check completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during health check: {}", e.getMessage());
            throw e;
        }
    }
}