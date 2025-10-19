package com.microservices.detteinterieur.service.notification;

import com.microservices.detteinterieur.dto.notification.*;
import com.microservices.detteinterieur.entity.Notification;
import com.microservices.detteinterieur.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing notifications and service monitoring
 * Handles notification creation, updates, and admin dashboard queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * Create a new notification
     */
    @Async
    public void createNotification(CreateNotificationDto dto) {
        try {
            Notification notification = Notification.builder()
                    .module(dto.getModule())
                    .referenceId(dto.getReferenceId())
                    .typeNotification(dto.getTypeNotification())
                    .message(dto.getMessage())
                    .destinataire(dto.getDestinataire())
                    .priorite(dto.getPriorite())
                    .serviceSource(dto.getServiceSource())
                    .serviceCible(dto.getServiceCible())
                    .metadata(dto.getMetadata())
                    .statut(Notification.StatutNotification.NON_LU)
                    .dateCreation(LocalDateTime.now())
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("Notification created: module={}, type={}, destinataire={}", 
                    dto.getModule(), dto.getTypeNotification(), dto.getDestinataire());
            
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create service connection notification
     */
    @Async
    public void createServiceConnectionNotification(String serviceName, String status, String details) {
        try {
            Notification.TypeNotification type = determineNotificationType(status);
            Notification.PrioriteNotification priorite = determinePriority(status);
            
            String message = String.format("Service %s: %s. %s", serviceName, status, 
                    details != null ? details : "");
            
            CreateNotificationDto dto = CreateNotificationDto.builder()
                    .module(determineModuleFromService(serviceName))
                    .typeNotification(type)
                    .message(message)
                    .destinataire("ADMIN")
                    .priorite(priorite)
                    .serviceSource(serviceName)
                    .metadata(String.format("{\"status\":\"%s\",\"timestamp\":\"%s\"}", 
                            status, LocalDateTime.now()))
                    .build();
            
            createNotification(dto);
            
        } catch (Exception e) {
            log.error("Error creating service connection notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get notifications with filters and pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(NotificationFilterDto filter) {
        try {
            Pageable pageable = createPageable(filter);
            
            Page<Notification> notifications;
            
            if (hasFilters(filter)) {
                notifications = notificationRepository.findWithFilters(
                        filter.getModule(),
                        filter.getTypeNotification(),
                        filter.getStatut(),
                        filter.getDestinataire(),
                        filter.getServiceSource(),
                        pageable
                );
            } else {
                notifications = notificationRepository.findAllForAdmin(pageable);
            }
            
            return notifications.map(NotificationDto::fromEntity);
            
        } catch (Exception e) {
            log.error("Error getting notifications: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving notifications", e);
        }
    }
    
    /**
     * Get notification statistics for admin dashboard
     */
    @Transactional(readOnly = true)
    public NotificationStatsDto getNotificationStats() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime last24Hours = LocalDateTime.now().minusDays(1);
            
            // Basic counts
            Long totalNotifications = notificationRepository.count();
            Long unreadNotifications = notificationRepository.countByStatut(Notification.StatutNotification.NON_LU);
            
            // Error notifications count
            Page<Notification> errorNotifications = notificationRepository.findByTypeNotification(
                    Notification.TypeNotification.ERREUR, PageRequest.of(0, 1));
            Long errorCount = errorNotifications.getTotalElements();
            
            // High priority unread notifications
            List<Notification> highPriorityNotifications = notificationRepository.findHighPriorityUnreadNotifications();
            Long highPriorityCount = (long) highPriorityNotifications.size();
            
            // Module statistics
            List<Object[]> moduleStatsData = notificationRepository.getModuleActivitySummary(last24Hours);
            List<ModuleStatsDto> moduleStats = moduleStatsData.stream()
                    .map(this::mapToModuleStats)
                    .collect(Collectors.toList());
            
            // Recent notifications
            List<Notification> recentNotificationsList = notificationRepository.findRecentNotifications(
                    PageRequest.of(0, 10));
            List<NotificationDto> recentNotifications = recentNotificationsList.stream()
                    .map(NotificationDto::fromEntity)
                    .collect(Collectors.toList());
            
            // Service health
            List<ServiceHealthDto> serviceHealth = getServiceHealthStatus();
            
            return NotificationStatsDto.builder()
                    .totalNotifications(totalNotifications)
                    .unreadNotifications(unreadNotifications)
                    .errorNotifications(errorCount)
                    .highPriorityNotifications(highPriorityCount)
                    .moduleStats(moduleStats)
                    .recentNotifications(recentNotifications)
                    .serviceHealth(serviceHealth)
                    .build();
            
        } catch (Exception e) {
            log.error("Error getting notification stats: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving notification statistics", e);
        }
    }
    
    /**
     * Mark notifications as read
     */
    public void markNotificationsAsRead(List<Long> notificationIds) {
        try {
            notificationRepository.markAsRead(notificationIds, LocalDateTime.now());
            log.info("Marked {} notifications as read", notificationIds.size());
        } catch (Exception e) {
            log.error("Error marking notifications as read: {}", e.getMessage(), e);
            throw new RuntimeException("Error marking notifications as read", e);
        }
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsReadForUser(String destinataire) {
        try {
            notificationRepository.markAllAsReadForUser(destinataire, LocalDateTime.now());
            log.info("Marked all notifications as read for user: {}", destinataire);
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user: {}", e.getMessage(), e);
            throw new RuntimeException("Error marking notifications as read", e);
        }
    }
    
    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public Optional<Notification> getNotificationById(Long id) {
        try {
            return notificationRepository.findById(id);
        } catch (Exception e) {
            log.error("Error getting notification by ID: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Cleanup old processed notifications
     */
    public void cleanupOldNotifications(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            notificationRepository.deleteOldProcessedNotifications(cutoffDate);
            log.info("Cleaned up processed notifications older than {} days", daysToKeep);
        } catch (Exception e) {
            log.error("Error cleaning up old notifications: {}", e.getMessage(), e);
            throw new RuntimeException("Error cleaning up notifications", e);
        }
    }
    
    /**
     * Create pageable from filter
     */
    private Pageable createPageable(NotificationFilterDto filter) {
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
    
    /**
     * Check if filter has any criteria
     */
    private boolean hasFilters(NotificationFilterDto filter) {
        return filter.getModule() != null || 
               filter.getTypeNotification() != null || 
               filter.getStatut() != null || 
               filter.getDestinataire() != null || 
               filter.getServiceSource() != null;
    }
    
    /**
     * Map Object[] to ModuleStatsDto
     */
    private ModuleStatsDto mapToModuleStats(Object[] data) {
        return ModuleStatsDto.builder()
                .module((String) data[0])
                .moduleDisplayName((String) data[0]) // Would need to map to display name
                .total(((Number) data[1]).longValue())
                .unread(((Number) data[2]).longValue())
                .errors(((Number) data[3]).longValue())
                .lastActivity(LocalDateTime.now()) // Would need to get actual last activity
                .build();
    }
    
    /**
     * Get service health status
     */
    public List<ServiceHealthDto> getServiceHealthStatus() {
        try {
            // Get recent notifications to determine service health
            List<Notification> recentNotifications = notificationRepository.findRecentNotifications(
                    PageRequest.of(0, 100));
            
            // Group by service source
            Map<String, List<Notification>> notificationsByService = recentNotifications
                    .stream()
                    .collect(Collectors.groupingBy(n -> n.getServiceSource() != null ? n.getServiceSource() : "Unknown"));
            
            // Calculate health status for each service
            return notificationsByService.entrySet().stream()
                    .map(entry -> {
                        String serviceName = entry.getKey();
                        List<Notification> serviceNotifications = entry.getValue();
                        
                        // Count errors
                        long errorCount = serviceNotifications.stream()
                                .filter(n -> Notification.TypeNotification.ERREUR.equals(n.getTypeNotification()))
                                .count();
                        
                        // Count connections
                        long connectionCount = serviceNotifications.stream()
                                .filter(n -> Notification.TypeNotification.CONNEXION.equals(n.getTypeNotification()))
                                .count();
                        
                        // Determine status
                        String status;
                        if (errorCount > 0) {
                            status = "ERROR";
                        } else if (connectionCount > 0) {
                            status = "CONNECTED";
                        } else {
                            status = "UNKNOWN";
                        }
                        
                        // Get last seen time (most recent notification)
                        Optional<LocalDateTime> lastSeen = serviceNotifications.stream()
                                .map(Notification::getDateCreation)
                                .max(LocalDateTime::compareTo);
                        
                        return ServiceHealthDto.builder()
                                .serviceName(serviceName)
                                .status(status)
                                .lastSeen(lastSeen.orElse(null))
                                .errorCount(errorCount)
                                .connectionCount(connectionCount)
                                .build();
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting service health status: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Determine notification type from service status
     */
    private Notification.TypeNotification determineNotificationType(String status) {
        return switch (status) {
            case "CONNECTED" -> Notification.TypeNotification.CONNEXION;
            case "DISCONNECTED" -> Notification.TypeNotification.DECONNEXION;
            case "ERROR" -> Notification.TypeNotification.ERREUR;
            default -> Notification.TypeNotification.AUTRE;
        };
    }
    
    /**
     * Determine priority from service status
     */
    private Notification.PrioriteNotification determinePriority(String status) {
        return switch (status) {
            case "ERROR" -> Notification.PrioriteNotification.HAUTE;
            case "DISCONNECTED" -> Notification.PrioriteNotification.NORMALE;
            default -> Notification.PrioriteNotification.BASSE;
        };
    }
    
    /**
     * Determine module from service name
     */
    private Notification.ModuleType determineModuleFromService(String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "dette-tresor-service" -> Notification.ModuleType.DETTE_TRESOR;
            case "meda-service" -> Notification.ModuleType.MEDA;
            case "dette-interieur-service" -> Notification.ModuleType.DETTE_INTERIEUR;
            case "config-server" -> Notification.ModuleType.SYSTEME;
            case "discovery-server" -> Notification.ModuleType.SYSTEME;
            case "api-gateway" -> Notification.ModuleType.SYSTEME;
            default -> Notification.ModuleType.SYSTEME;
        };
    }
}