package com.microservices.detteinterieur.service.notification;

import com.microservices.detteinterieur.dto.notification.CreateNotificationDto;
import com.microservices.detteinterieur.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Scheduled service for automatic notification management and service monitoring
 * Handles cleanup tasks and periodic service health checks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduledService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduledService.class);
    
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;
    
    @Value("${notification.cleanup.days:30}")
    private int cleanupDays;
    
    @Value("${notification.service-check.enabled:true}")
    private boolean serviceCheckEnabled;
    
    @Value("${eureka.client.service-url.defaultZone:http://localhost:8761/eureka}")
    private String eurekaUrl;
    
    // List of services to monitor
    private final List<String> monitoredServices = List.of(
            "dette-tresor-service",
            "meda-service", 
            "dette-interieur-service",
            "config-server",
            "discovery-server",
            "api-gateway"
    );
    
    /**
     * Cleanup old processed notifications daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldNotifications() {
        try {
            log.info("Starting cleanup of old notifications older than {} days", cleanupDays);
            notificationService.cleanupOldNotifications(cleanupDays);
            log.info("Completed cleanup of old notifications");
            
            // Create notification about cleanup
            CreateNotificationDto cleanupNotification = CreateNotificationDto.builder()
                    .module(Notification.ModuleType.SYSTEME)
                    .typeNotification(Notification.TypeNotification.AUTRE)
                    .message(String.format("Nettoyage automatique des notifications de plus de %d jours effectué", cleanupDays))
                    .destinataire("ADMIN")
                    .priorite(Notification.PrioriteNotification.BASSE)
                    .serviceSource("notification-scheduler")
                    .build();
            
            notificationService.createNotification(cleanupNotification);
            
        } catch (Exception e) {
            log.error("Error during notification cleanup: {}", e.getMessage(), e);
            createErrorNotification("Erreur lors du nettoyage des notifications", e.getMessage());
        }
    }
    
    /**
     * Check service health every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkServiceHealth() {
        if (!serviceCheckEnabled) {
            return;
        }
        
        try {
            log.debug("Starting service health check");
            
            for (String serviceName : monitoredServices) {
                checkIndividualService(serviceName);
            }
            
            log.debug("Completed service health check");
            
        } catch (Exception e) {
            log.error("Error during service health check: {}", e.getMessage(), e);
            createErrorNotification("Erreur lors de la vérification de santé des services", e.getMessage());
        }
    }
    
    /**
     * Generate daily summary report at 8 AM
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailySummary() {
        try {
            log.info("Generating daily notification summary");
            
            // Get yesterday's stats
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            
            // This would typically query the notification stats
            // For now, we'll create a simple summary notification
            CreateNotificationDto summaryNotification = CreateNotificationDto.builder()
                    .module(Notification.ModuleType.SYSTEME)
                    .typeNotification(Notification.TypeNotification.RAPPEL)
                    .message(String.format("Résumé quotidien des notifications du %s disponible dans le tableau de bord", 
                            yesterday.toLocalDate()))
                    .destinataire("ADMIN")
                    .priorite(Notification.PrioriteNotification.NORMALE)
                    .serviceSource("notification-scheduler")
                    .build();
            
            notificationService.createNotification(summaryNotification);
            
            log.info("Daily summary notification created");
            
        } catch (Exception e) {
            log.error("Error generating daily summary: {}", e.getMessage(), e);
            createErrorNotification("Erreur lors de la génération du résumé quotidien", e.getMessage());
        }
    }
    
    /**
     * Send reminders for unread high priority notifications every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void sendHighPriorityReminders() {
        try {
            // This would typically check for high priority unread notifications
            // and send reminders if they've been unread for too long
            
            log.debug("Checking for high priority notification reminders");
            
            // Implementation would go here to check unread notifications
            // and create reminder notifications if needed
            
        } catch (Exception e) {
            log.error("Error sending high priority reminders: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check individual service health
     */
    private void checkIndividualService(String serviceName) {
        try {
            // This is a simplified health check
            // In a real implementation, you might check specific health endpoints
            
            boolean isHealthy = checkServiceHealthEndpoint(serviceName);
            
            if (!isHealthy) {
                notificationService.createServiceConnectionNotification(
                        serviceName,
                        "ERROR",
                        "Service health check failed"
                );
                
                log.warn("Service {} failed health check", serviceName);
            } else {
                log.debug("Service {} passed health check", serviceName);
            }
            
        } catch (Exception e) {
            log.warn("Error checking health for service {}: {}", serviceName, e.getMessage());
            
            notificationService.createServiceConnectionNotification(
                    serviceName,
                    "ERROR",
                    "Health check error: " + e.getMessage()
            );
        }
    }
    
    /**
     * Check service health endpoint
     */
    private boolean checkServiceHealthEndpoint(String serviceName) {
        try {
            // Try to get service info from Eureka
            String eurekaAppsUrl = eurekaUrl.replace("/eureka", "/eureka/apps/" + serviceName.toUpperCase());
            
            restTemplate.getForObject(eurekaAppsUrl, String.class);
            return true;
            
        } catch (Exception e) {
            log.debug("Service {} not reachable via Eureka: {}", serviceName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Create error notification
     */
    private void createErrorNotification(String message, String details) {
        try {
            CreateNotificationDto errorNotification = CreateNotificationDto.builder()
                    .module(Notification.ModuleType.SYSTEME)
                    .typeNotification(Notification.TypeNotification.ERREUR)
                    .message(message + (details != null ? ": " + details : ""))
                    .destinataire("ADMIN")
                    .priorite(Notification.PrioriteNotification.HAUTE)
                    .serviceSource("notification-scheduler")
                    .build();
            
            notificationService.createNotification(errorNotification);
            
        } catch (Exception e) {
            log.error("Error creating error notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manual trigger for testing purposes
     */
    public void triggerHealthCheck() {
        log.info("Manual trigger for service health check");
        checkServiceHealth();
    }
    
    /**
     * Manual trigger for cleanup
     */
    public void triggerCleanup() {
        log.info("Manual trigger for notification cleanup");
        cleanupOldNotifications();
    }
    
    /**
     * Create test notification for monitoring system
     */
    public void createTestNotification() {
        try {
            CreateNotificationDto testNotification = CreateNotificationDto.builder()
                    .module(Notification.ModuleType.SYSTEME)
                    .typeNotification(Notification.TypeNotification.AUTRE)
                    .message("Notification de test du système - " + LocalDateTime.now())
                    .destinataire("ADMIN")
                    .priorite(Notification.PrioriteNotification.BASSE)
                    .serviceSource("notification-scheduler")
                    .metadata("{\"test\":true}")
                    .build();
            
            notificationService.createNotification(testNotification);
            log.info("Test notification created successfully");
            
        } catch (Exception e) {
            log.error("Error creating test notification: {}", e.getMessage(), e);
        }
    }
}