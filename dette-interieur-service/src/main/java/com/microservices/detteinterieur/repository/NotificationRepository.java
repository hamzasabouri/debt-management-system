package com.microservices.detteinterieur.repository;

import com.microservices.detteinterieur.entity.Notification;
import com.microservices.detteinterieur.entity.Notification.ModuleType;
import com.microservices.detteinterieur.entity.Notification.StatutNotification;
import com.microservices.detteinterieur.entity.Notification.TypeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity
 * Provides queries for admin dashboard and service monitoring
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for admin dashboard with pagination
     */
    @Query("SELECT n FROM Notification n ORDER BY n.dateCreation DESC")
    Page<Notification> findAllForAdmin(Pageable pageable);
    
    /**
     * Find unread notifications count for admin
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.statut = :statut")
    Long countByStatut(@Param("statut") StatutNotification statut);
    
    /**
     * Find notifications by module with pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.module = :module ORDER BY n.dateCreation DESC")
    Page<Notification> findByModule(@Param("module") ModuleType module, Pageable pageable);
    
    /**
     * Find notifications by type with pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.typeNotification = :type ORDER BY n.dateCreation DESC")
    Page<Notification> findByTypeNotification(@Param("type") TypeNotification type, Pageable pageable);
    
    /**
     * Find notifications by status with pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.statut = :statut ORDER BY n.dateCreation DESC")
    Page<Notification> findByStatut(@Param("statut") StatutNotification statut, Pageable pageable);
    
    /**
     * Find notifications by destinataire
     */
    @Query("SELECT n FROM Notification n WHERE n.destinataire = :destinataire ORDER BY n.dateCreation DESC")
    Page<Notification> findByDestinataire(@Param("destinataire") String destinataire, Pageable pageable);
    
    /**
     * Find notifications by date range
     */
    @Query("SELECT n FROM Notification n WHERE n.dateCreation >= :startDate AND n.dateCreation <= :endDate ORDER BY n.dateCreation DESC")
    Page<Notification> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate, 
                                       Pageable pageable);
    
    /**
     * Find service connection notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.typeNotification IN ('CONNEXION', 'DECONNEXION', 'INTEGRATION') ORDER BY n.dateCreation DESC")
    Page<Notification> findServiceConnectionNotifications(Pageable pageable);
    
    /**
     * Find error notifications for monitoring
     */
    @Query("SELECT n FROM Notification n WHERE n.typeNotification = 'ERREUR' AND n.statut = 'NON_LU' ORDER BY n.dateCreation DESC")
    List<Notification> findUnreadErrorNotifications();
    
    /**
     * Find high priority unread notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.statut = 'NON_LU' AND n.priorite IN ('HAUTE', 'CRITIQUE') ORDER BY n.dateCreation DESC")
    List<Notification> findHighPriorityUnreadNotifications();
    
    /**
     * Service connection statistics by module
     */
    @Query("SELECT n.module, n.typeNotification, COUNT(n) " +
           "FROM Notification n " +
           "WHERE n.typeNotification IN ('CONNEXION', 'DECONNEXION', 'INTEGRATION', 'ERREUR') " +
           "AND n.dateCreation >= :startDate " +
           "GROUP BY n.module, n.typeNotification " +
           "ORDER BY n.module, n.typeNotification")
    List<Object[]> getServiceConnectionStats(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Daily notification statistics
     */
    @Query("SELECT DATE(n.dateCreation) as date, n.module, n.typeNotification, COUNT(n) as count " +
           "FROM Notification n " +
           "WHERE n.dateCreation >= :startDate AND n.dateCreation <= :endDate " +
           "GROUP BY DATE(n.dateCreation), n.module, n.typeNotification " +
           "ORDER BY date DESC, n.module, n.typeNotification")
    List<Object[]> getDailyNotificationStats(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * Module activity summary
     */
    @Query("SELECT n.module, " +
           "COUNT(n) as total, " +
           "SUM(CASE WHEN n.statut = 'NON_LU' THEN 1 ELSE 0 END) as nonLu, " +
           "SUM(CASE WHEN n.typeNotification = 'ERREUR' THEN 1 ELSE 0 END) as erreurs " +
           "FROM Notification n " +
           "WHERE n.dateCreation >= :startDate " +
           "GROUP BY n.module " +
           "ORDER BY total DESC")
    List<Object[]> getModuleActivitySummary(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Mark notifications as read by IDs
     */
    @Modifying
    @Query("UPDATE Notification n SET n.statut = 'LU', n.dateLecture = :dateLecture WHERE n.id IN :ids")
    void markAsRead(@Param("ids") List<Long> ids, @Param("dateLecture") LocalDateTime dateLecture);
    
    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.statut = 'LU', n.dateLecture = :dateLecture WHERE n.destinataire = :destinataire AND n.statut = 'NON_LU'")
    void markAllAsReadForUser(@Param("destinataire") String destinataire, @Param("dateLecture") LocalDateTime dateLecture);
    
    /**
     * Delete old notifications (cleanup)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.dateCreation < :cutoffDate AND n.statut = 'TRAITE'")
    void deleteOldProcessedNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find notifications with filters
     */
    @Query("SELECT n FROM Notification n WHERE " +
           "(:module IS NULL OR n.module = :module) AND " +
           "(:type IS NULL OR n.typeNotification = :type) AND " +
           "(:statut IS NULL OR n.statut = :statut) AND " +
           "(:destinataire IS NULL OR n.destinataire LIKE %:destinataire%) AND " +
           "(:serviceSource IS NULL OR n.serviceSource LIKE %:serviceSource%) " +
           "ORDER BY n.dateCreation DESC")
    Page<Notification> findWithFilters(@Param("module") ModuleType module,
                                       @Param("type") TypeNotification type,
                                       @Param("statut") StatutNotification statut,
                                       @Param("destinataire") String destinataire,
                                       @Param("serviceSource") String serviceSource,
                                       Pageable pageable);
    
    /**
     * Recent notifications for dashboard widget
     */
    @Query("SELECT n FROM Notification n ORDER BY n.dateCreation DESC")
    List<Notification> findRecentNotifications(Pageable pageable);
    
    /**
     * Service health check notifications
     */
    @Query("SELECT n FROM Notification n WHERE " +
           "n.typeNotification IN ('CONNEXION', 'DECONNEXION', 'ERREUR') AND " +
           "n.dateCreation >= :since " +
           "ORDER BY n.dateCreation DESC")
    List<Notification> findServiceHealthNotifications(@Param("since") LocalDateTime since);
}