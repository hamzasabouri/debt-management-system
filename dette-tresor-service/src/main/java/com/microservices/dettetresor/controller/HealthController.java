package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.service.integration.BamIntegrationService;
import com.microservices.dettetresor.service.integration.DtfeIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Service health and status endpoints")
public class HealthController {
    
    private final BamIntegrationService bamIntegrationService;
    private final DtfeIntegrationService dtfeIntegrationService;
    
    @GetMapping
    @Operation(summary = "Service health check", 
               description = "Returns the health status of the Dette du Trésor Service")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "Dette du Trésor Service");
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/integrations")
    @Operation(summary = "Integration health check", 
               description = "Returns the health status of external integrations")
    public ResponseEntity<Map<String, Object>> integrationHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            boolean bamHealth = bamIntegrationService.testBamConnectivity();
            boolean dtfeHealth = dtfeIntegrationService.testDtfeConnectivity();
            
            health.put("service", "Dette du Trésor Service - Integrations");
            health.put("timestamp", LocalDateTime.now());
            health.put("bam", Map.of("status", bamHealth ? "UP" : "DOWN"));
            health.put("dtfe", Map.of("status", dtfeHealth ? "UP" : "DOWN"));
            health.put("overall", (bamHealth && dtfeHealth) ? "UP" : "DEGRADED");
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("service", "Dette du Trésor Service - Integrations");
            health.put("timestamp", LocalDateTime.now());
            health.put("overall", "DOWN");
            health.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(health);
        }
    }
}