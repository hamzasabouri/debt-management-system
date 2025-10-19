package com.microservices.detteinterieur.service.notification;

import com.microservices.detteinterieur.dto.notification.CreateNotificationDto;
import com.microservices.detteinterieur.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for creating notifications based on integration events
 * Automatically generates notifications when services interact with external systems
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationNotificationListener {
    
    private final NotificationService notificationService;
    
    /**
     * Event class for BAM integration
     */
    public static class BAMIntegrationEvent {
        private final String operation;
        private final String status;
        private final String details;
        private final Long referenceId;
        
        public BAMIntegrationEvent(String operation, String status, String details, Long referenceId) {
            this.operation = operation;
            this.status = status;
            this.details = details;
            this.referenceId = referenceId;
        }
        
        // Getters
        public String getOperation() { return operation; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
        public Long getReferenceId() { return referenceId; }
    }
    
    /**
     * Event class for Comptabilite integration
     */
    public static class ComptabiliteIntegrationEvent {
        private final String operation;
        private final String status;
        private final String details;
        private final Long referenceId;
        
        public ComptabiliteIntegrationEvent(String operation, String status, String details, Long referenceId) {
            this.operation = operation;
            this.status = status;
            this.details = details;
            this.referenceId = referenceId;
        }
        
        // Getters
        public String getOperation() { return operation; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
        public Long getReferenceId() { return referenceId; }
    }
    
    /**
     * Event class for DTFE integration
     */
    public static class DTFEIntegrationEvent {
        private final String operation;
        private final String status;
        private final String details;
        private final Long referenceId;
        
        public DTFEIntegrationEvent(String operation, String status, String details, Long referenceId) {
            this.operation = operation;
            this.status = status;
            this.details = details;
            this.referenceId = referenceId;
        }
        
        // Getters
        public String getOperation() { return operation; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
        public Long getReferenceId() { return referenceId; }
    }
    
    /**
     * Event class for service connection status
     */
    public static class ServiceConnectionEvent {
        private final String serviceName;
        private final String status;
        private final String details;
        private final String errorMessage;
        
        public ServiceConnectionEvent(String serviceName, String status, String details, String errorMessage) {
            this.serviceName = serviceName;
            this.status = status;
            this.details = details;
            this.errorMessage = errorMessage;
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Handle BAM integration events
     */
    @EventListener
    @Async
    public void handleBAMIntegrationEvent(BAMIntegrationEvent event) {
        try {
            log.info("Processing BAM integration event: operation={}, status={}", 
                    event.getOperation(), event.getStatus());
            
            Notification.TypeNotification type = determineNotificationType(event.getOperation(), event.getStatus());
            Notification.PrioriteNotification priority = determinePriority(event.getStatus());
            
            String message = String.format("Intégration BAM - %s: %s. %s", 
                    event.getOperation(), event.getStatus(), 
                    event.getDetails() != null ? event.getDetails() : "");
            
            CreateNotificationDto dto = CreateNotificationDto.builder()
                    .module(Notification.ModuleType.BAM)
                    .referenceId(event.getReferenceId())
                    .typeNotification(type)
                    .message(message)
                    .destinataire("ADMIN")
                    .priorite(priority)
                    .serviceSource("dette-interieur-service")
                    .serviceCible("bam-service")
                    .metadata(String.format("{\"operation\":\"%s\",\"status\":\"%s\"}", 
                            event.getOperation(), event.getStatus()))
                    .build();
            
            notificationService.createNotification(dto);
            
        } catch (Exception e) {
            log.error("Error handling BAM integration event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle Comptabilite integration events
     */
    @EventListener
    @Async
    public void handleComptabiliteIntegrationEvent(ComptabiliteIntegrationEvent event) {
        try {
            log.info("Processing Comptabilite integration event: operation={}, status={}", 
                    event.getOperation(), event.getStatus());
            
            Notification.TypeNotification type = determineNotificationType(event.getOperation(), event.getStatus());
            Notification.PrioriteNotification priority = determinePriority(event.getStatus());
            
            String message = String.format("Intégration Comptabilité - %s: %s. %s", 
                    event.getOperation(), event.getStatus(), 
                    event.getDetails() != null ? event.getDetails() : "");
            
            CreateNotificationDto dto = CreateNotificationDto.builder()
                    .module(Notification.ModuleType.COMPTABILITE)
                    .referenceId(event.getReferenceId())
                    .typeNotification(type)
                    .message(message)
                    .destinataire("ADMIN")
                    .priorite(priority)
                    .serviceSource("dette-interieur-service")
                    .serviceCible("comptabilite-service")
                    .metadata(String.format("{\"operation\":\"%s\",\"status\":\"%s\"}", 
                            event.getOperation(), event.getStatus()))
                    .build();
            
            notificationService.createNotification(dto);
            
        } catch (Exception e) {
            log.error("Error handling Comptabilite integration event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle DTFE integration events
     */
    @EventListener
    @Async
    public void handleDTFEIntegrationEvent(DTFEIntegrationEvent event) {
        try {
            log.info("Processing DTFE integration event: operation={}, status={}", 
                    event.getOperation(), event.getStatus());
            
            Notification.TypeNotification type = determineNotificationType(event.getOperation(), event.getStatus());
            Notification.PrioriteNotification priority = determinePriority(event.getStatus());
            
            String message = String.format("Intégration DTFE - %s: %s. %s", 
                    event.getOperation(), event.getStatus(), 
                    event.getDetails() != null ? event.getDetails() : "");
            
            CreateNotificationDto dto = CreateNotificationDto.builder()
                    .module(Notification.ModuleType.DTFE)
                    .referenceId(event.getReferenceId())
                    .typeNotification(type)
                    .message(message)
                    .destinataire("ADMIN")
                    .priorite(priority)
                    .serviceSource("dette-interieur-service")
                    .serviceCible("dtfe-service")
                    .metadata(String.format("{\"operation\":\"%s\",\"status\":\"%s\"}", 
                            event.getOperation(), event.getStatus()))
                    .build();
            
            notificationService.createNotification(dto);
            
        } catch (Exception e) {
            log.error("Error handling DTFE integration event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle service connection events
     */
    @EventListener
    @Async
    public void handleServiceConnectionEvent(ServiceConnectionEvent event) {
        try {
            log.info("Processing service connection event: service={}, status={}", 
                    event.getServiceName(), event.getStatus());
            
            notificationService.createServiceConnectionNotification(
                    event.getServiceName(),
                    event.getStatus(),
                    event.getDetails()
            );
            
        } catch (Exception e) {
            log.error("Error handling service connection event: {}", e.getMessage(), e);
        }
    }
    
    // Helper methods
    
    private Notification.TypeNotification determineNotificationType(String operation, String status) {
        if ("ERROR".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
            return Notification.TypeNotification.ERREUR;
        }
        
        return switch (operation.toLowerCase()) {
            case "send_credit_notice", "credit_notice" -> Notification.TypeNotification.AVIS_CREDIT;
            case "send_debit_notice", "debit_notice" -> Notification.TypeNotification.AVIS_DEBIT;
            case "send_payment_order", "payment_order" -> Notification.TypeNotification.ORDRE_PAIEMENT;
            case "validate", "validation" -> Notification.TypeNotification.VALIDATION;
            case "reject", "rejection" -> Notification.TypeNotification.REJET;
            default -> Notification.TypeNotification.INTEGRATION;
        };
    }
    
    private Notification.PrioriteNotification determinePriority(String status) {
        return switch (status.toLowerCase()) {
            case "error", "failed", "critical" -> Notification.PrioriteNotification.HAUTE;
            case "warning", "timeout" -> Notification.PrioriteNotification.NORMALE;
            case "success", "completed", "validated" -> Notification.PrioriteNotification.BASSE;
            default -> Notification.PrioriteNotification.NORMALE;
        };
    }
}