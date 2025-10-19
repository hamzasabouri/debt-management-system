package com.microservices.detteinterieur.controller;

import com.microservices.detteinterieur.service.notification.IntegrationNotificationListener;
import com.microservices.detteinterieur.service.notification.NotificationScheduledService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin controller for testing and managing the notification system
 * Provides endpoints for administrators to test notification functionality
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Notifications", description = "API d'administration pour le système de notifications")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {
    
    private final NotificationScheduledService scheduledService;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Test the notification system
     */
    @PostMapping("/test")
    @Operation(summary = "Tester le système de notifications", 
               description = "Crée une notification de test pour vérifier le fonctionnement du système")
    public ResponseEntity<Map<String, String>> testNotificationSystem() {
        try {
            scheduledService.createTestNotification();
            log.info("Test notification created by admin");
            
            return ResponseEntity.ok(Map.of(
                    "message", "Notification de test créée avec succès",
                    "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("Error creating test notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de la création de la notification de test",
                    "status", "error"
            ));
        }
    }
    
    /**
     * Trigger manual service health check
     */
    @PostMapping("/health-check")
    @Operation(summary = "Vérification manuelle de la santé des services", 
               description = "Déclenche une vérification manuelle de l'état de tous les services")
    public ResponseEntity<Map<String, String>> triggerHealthCheck() {
        try {
            scheduledService.triggerHealthCheck();
            log.info("Manual health check triggered by admin");
            
            return ResponseEntity.ok(Map.of(
                    "message", "Vérification de santé des services déclenchée",
                    "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("Error triggering health check: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de la vérification de santé",
                    "status", "error"
            ));
        }
    }
    
    /**
     * Trigger manual cleanup of old notifications
     */
    @PostMapping("/cleanup")
    @Operation(summary = "Nettoyage manuel des notifications", 
               description = "Déclenche le nettoyage manuel des anciennes notifications")
    public ResponseEntity<Map<String, String>> triggerCleanup() {
        try {
            scheduledService.triggerCleanup();
            log.info("Manual cleanup triggered by admin");
            
            return ResponseEntity.ok(Map.of(
                    "message", "Nettoyage des notifications déclenché",
                    "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("Error triggering cleanup: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors du nettoyage",
                    "status", "error"
            ));
        }
    }
    
    /**
     * Simulate BAM integration event
     */
    @PostMapping("/simulate/bam")
    @Operation(summary = "Simuler un événement d'intégration BAM", 
               description = "Simule un événement d'intégration avec BAM pour tester les notifications")
    public ResponseEntity<Map<String, String>> simulateBAMEvent(
            @RequestParam(defaultValue = "send_credit_notice") String operation,
            @RequestParam(defaultValue = "success") String status,
            @RequestParam(defaultValue = "Test d'intégration BAM") String details,
            @RequestParam(required = false) Long referenceId) {
        try {
            IntegrationNotificationListener.BAMIntegrationEvent event = 
                    new IntegrationNotificationListener.BAMIntegrationEvent(operation, status, details, referenceId);
            
            eventPublisher.publishEvent(event);
            log.info("BAM integration event simulated: operation={}, status={}", operation, status);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Événement d'intégration BAM simulé avec succès",
                    "operation", operation,
                    "status", status
            ));
            
        } catch (Exception e) {
            log.error("Error simulating BAM event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de la simulation de l'événement BAM",
                    "status", "error"
            ));
        }
    }
    
    /**
     * Simulate Comptabilite integration event
     */
    @PostMapping("/simulate/comptabilite")
    @Operation(summary = "Simuler un événement d'intégration Comptabilité", 
               description = "Simule un événement d'intégration avec le système de comptabilité")
    public ResponseEntity<Map<String, String>> simulateComptabiliteEvent(
            @RequestParam(defaultValue = "validation") String operation,
            @RequestParam(defaultValue = "success") String status,
            @RequestParam(defaultValue = "Test d'intégration Comptabilité") String details,
            @RequestParam(required = false) Long referenceId) {
        try {
            IntegrationNotificationListener.ComptabiliteIntegrationEvent event = 
                    new IntegrationNotificationListener.ComptabiliteIntegrationEvent(operation, status, details, referenceId);
            
            eventPublisher.publishEvent(event);
            log.info("Comptabilite integration event simulated: operation={}, status={}", operation, status);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Événement d'intégration Comptabilité simulé avec succès",
                    "operation", operation,
                    "status", status
            ));
            
        } catch (Exception e) {
            log.error("Error simulating Comptabilite event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de la simulation de l'événement Comptabilité",
                    "status", "error"
            ));
        }
    }
    
    /**
     * Simulate DTFE integration event
     */
    @PostMapping("/simulate/dtfe")
    @Operation(summary = "Simuler un événement d'intégration DTFE", 
               description = "Simule un événement d'intégration avec DTFE")
    public ResponseEntity<Map<String, String>> simulateDTFEEvent(
            @RequestParam(defaultValue = "send_payment_order") String operation,
            @RequestParam(defaultValue = "success") String status,
            @RequestParam(defaultValue = "Test d'intégration DTFE") String details,
            @RequestParam(required = false) Long referenceId) {
        try {
            IntegrationNotificationListener.DTFEIntegrationEvent event = 
                    new IntegrationNotificationListener.DTFEIntegrationEvent(operation, status, details, referenceId);
            
            eventPublisher.publishEvent(event);
            log.info("DTFE integration event simulated: operation={}, status={}", operation, status);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Événement d'intégration DTFE simulé avec succès",
                    "operation", operation,
                    "status", status
            ));
            
        } catch (Exception e) {
            log.error("Error simulating DTFE event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de la simulation de l'événement DTFE",
                    "status", "error"
            ));
        }
    }
    
    /**
     * Simulate service connection event
     */
    @PostMapping("/simulate/service-connection")
    @Operation(summary = "Simuler un événement de connexion de service", 
               description = "Simule un événement de connexion/déconnexion de service")
    public ResponseEntity<Map<String, String>> simulateServiceConnectionEvent(
            @RequestParam String serviceName,
            @RequestParam(defaultValue = "CONNECTED") String status,
            @RequestParam(defaultValue = "Test de connexion service") String details,
            @RequestParam(required = false) String errorMessage) {
        try {
            IntegrationNotificationListener.ServiceConnectionEvent event = 
                    new IntegrationNotificationListener.ServiceConnectionEvent(serviceName, status, details, errorMessage);
            
            eventPublisher.publishEvent(event);
            log.info("Service connection event simulated: service={}, status={}", serviceName, status);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Événement de connexion de service simulé avec succès",
                    "service", serviceName,
                    "status", status
            ));
            
        } catch (Exception e) {
            log.error("Error simulating service connection event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de la simulation de l'événement de connexion",
                    "status", "error"
            ));
        }
    }
}