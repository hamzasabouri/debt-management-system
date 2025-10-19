package com.microservices.detteinterieur.controller;

import com.microservices.detteinterieur.dto.notification.*;
import com.microservices.detteinterieur.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for notification management and service monitoring
 * Provides endpoints for admin dashboard to monitor service connections
 */
@RestController
@RequestMapping("/api/dette-interieur/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "API pour la gestion des notifications et le monitoring des services")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Get all notifications as a list
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Récupérer toutes les notifications", 
               description = "Récupère la liste complète des notifications sans pagination")
    @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès")
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        try {
            NotificationFilterDto filter = NotificationFilterDto.builder()
                    .page(0)
                    .size(1000)  // Large enough to get all notifications
                    .sortBy("dateCreation")
                    .sortDirection("DESC")
                    .build();
            
            Page<NotificationDto> notificationsPage = notificationService.getNotifications(filter);
            List<NotificationDto> notifications = notificationsPage.getContent();
            
            log.info("Retrieved {} notifications as list", notifications.size());
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            log.error("Error retrieving notifications list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get notifications with filters and pagination for admin dashboard
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Récupérer les notifications avec filtres", 
               description = "Récupère la liste des notifications avec pagination et filtres pour le tableau de bord admin")
    @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès")
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @Parameter(description = "Module (dette_tresor, meda, dette_interieur, etc.)")
            @RequestParam(required = false) String module,
            
            @Parameter(description = "Type de notification")
            @RequestParam(required = false) String typeNotification,
            
            @Parameter(description = "Statut (non_lu, lu, traite)")
            @RequestParam(required = false) String statut,
            
            @Parameter(description = "Destinataire")
            @RequestParam(required = false) String destinataire,
            
            @Parameter(description = "Service source")
            @RequestParam(required = false) String serviceSource,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Tri par champ")
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            
            @Parameter(description = "Direction du tri")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        try {
            NotificationFilterDto filter = NotificationFilterDto.builder()
                    .module(module != null ? parseModuleType(module) : null)
                    .typeNotification(typeNotification != null ? parseNotificationType(typeNotification) : null)
                    .statut(statut != null ? parseStatutNotification(statut) : null)
                    .destinataire(destinataire)
                    .serviceSource(serviceSource)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            
            Page<NotificationDto> notifications = notificationService.getNotifications(filter);
            
            log.info("Retrieved {} notifications for admin dashboard", notifications.getTotalElements());
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            log.error("Error retrieving notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get notification statistics for admin dashboard
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Statistiques des notifications", 
               description = "Récupère les statistiques des notifications pour le tableau de bord admin")
    @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès")
    public ResponseEntity<NotificationStatsDto> getNotificationStats() {
        try {
            NotificationStatsDto stats = notificationService.getNotificationStats();
            log.info("Retrieved notification statistics for admin dashboard");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error retrieving notification stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get service health status for monitoring
     */
    @GetMapping("/service-health")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "État de santé des services", 
               description = "Récupère l'état de santé des services connectés pour le monitoring")
    @ApiResponse(responseCode = "200", description = "État de santé récupéré avec succès")
    public ResponseEntity<List<ServiceHealthDto>> getServiceHealth() {
        try {
            List<ServiceHealthDto> serviceHealth = notificationService.getServiceHealthStatus();
            log.info("Retrieved service health status for {} services", serviceHealth.size());
            return ResponseEntity.ok(serviceHealth);
            
        } catch (Exception e) {
            log.error("Error retrieving service health: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create a new notification
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Créer une notification", 
               description = "Crée une nouvelle notification dans le système")
    @ApiResponse(responseCode = "201", description = "Notification créée avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public ResponseEntity<Map<String, String>> createNotification(@Valid @RequestBody CreateNotificationDto dto) {
        try {
            notificationService.createNotification(dto);
            log.info("Created notification: module={}, type={}", dto.getModule(), dto.getTypeNotification());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Notification créée avec succès"));
            
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création de la notification"));
        }
    }
    
    /**
     * Create service connection notification
     */
    @PostMapping("/service-connection")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Notifier connexion service", 
               description = "Crée une notification de connexion/déconnexion de service")
    @ApiResponse(responseCode = "201", description = "Notification de service créée avec succès")
    public ResponseEntity<Map<String, String>> createServiceConnectionNotification(
            @Valid @RequestBody ServiceConnectionNotificationDto dto) {
        try {
            notificationService.createServiceConnectionNotification(
                    dto.getServiceName(), 
                    dto.getConnectionStatus(), 
                    dto.getDetails()
            );
            
            log.info("Created service connection notification: service={}, status={}", 
                    dto.getServiceName(), dto.getConnectionStatus());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Notification de service créée avec succès"));
            
        } catch (Exception e) {
            log.error("Error creating service connection notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création de la notification de service"));
        }
    }
    
    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Récupérer une notification par ID", 
               description = "Récupère les détails d'une notification spécifique")
    @ApiResponse(responseCode = "200", description = "Notification trouvée")
    @ApiResponse(responseCode = "404", description = "Notification non trouvée")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        try {
            return notificationService.getNotificationById(id)
                    .map(notification -> ResponseEntity.ok(new NotificationDto(notification)))
                    .orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            log.error("Error retrieving notification by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark notifications as read
     */
    @PutMapping("/mark-read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Marquer comme lues", 
               description = "Marque les notifications spécifiées comme lues")
    @ApiResponse(responseCode = "200", description = "Notifications marquées comme lues")
    public ResponseEntity<Map<String, String>> markNotificationsAsRead(@Valid @RequestBody BulkNotificationDto dto) {
        try {
            notificationService.markNotificationsAsRead(dto.getNotificationIds());
            log.info("Marked {} notifications as read", dto.getNotificationIds().size());
            
            return ResponseEntity.ok(Map.of("message", 
                    String.format("%d notifications marquées comme lues", dto.getNotificationIds().size())));
            
        } catch (Exception e) {
            log.error("Error marking notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du marquage des notifications"));
        }
    }
    
    /**
     * Mark all notifications as read for current user
     */
    @PutMapping("/mark-all-read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Marquer toutes comme lues", 
               description = "Marque toutes les notifications comme lues pour l'utilisateur courant")
    @ApiResponse(responseCode = "200", description = "Toutes les notifications marquées comme lues")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @Parameter(description = "Destinataire (ADMIN par défaut)")
            @RequestParam(defaultValue = "ADMIN") String destinataire) {
        try {
            notificationService.markAllAsReadForUser(destinataire);
            log.info("Marked all notifications as read for user: {}", destinataire);
            
            return ResponseEntity.ok(Map.of("message", "Toutes les notifications marquées comme lues"));
            
        } catch (Exception e) {
            log.error("Error marking all notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du marquage des notifications"));
        }
    }
    
    /**
     * Get unread notifications count
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Nombre de notifications non lues", 
               description = "Récupère le nombre de notifications non lues")
    @ApiResponse(responseCode = "200", description = "Nombre récupéré avec succès")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        try {
            NotificationStatsDto stats = notificationService.getNotificationStats();
            return ResponseEntity.ok(Map.of("unreadCount", stats.getUnreadNotifications()));
            
        } catch (Exception e) {
            log.error("Error getting unread count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper methods for parsing enum values
    
    private com.microservices.detteinterieur.entity.Notification.ModuleType parseModuleType(String module) {
        try {
            return com.microservices.detteinterieur.entity.Notification.ModuleType.valueOf(module.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid module type: {}", module);
            return null;
        }
    }
    
    private com.microservices.detteinterieur.entity.Notification.TypeNotification parseNotificationType(String type) {
        try {
            return com.microservices.detteinterieur.entity.Notification.TypeNotification.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid notification type: {}", type);
            return null;
        }
    }
    
    private com.microservices.detteinterieur.entity.Notification.StatutNotification parseStatutNotification(String statut) {
        try {
            return com.microservices.detteinterieur.entity.Notification.StatutNotification.valueOf(statut.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid notification status: {}", statut);
            return null;
        }
    }
}