package com.microservices.detteinterieur.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Notification entity for tracking service interactions and system events
 * Stores notifications for admin dashboard about connections with other services
 */
@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Module concerné : dette_tresor, meda, dette_interieur
     */
    @Column(name = "module", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ModuleType module;
    
    /**
     * Référence à l'entité concernée (ex : pret_id, avance_id, adjudication_id)
     */
    @Column(name = "reference_id", nullable = true)
    private Long referenceId;
    
    /**
     * Type : avis_credit, avis_debit, ordre_paiement, erreur, rappel, autre
     */
    @Column(name = "type_notification", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TypeNotification typeNotification;
    
    /**
     * Contenu de la notification
     */
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    /**
     * Utilisateur ou service destinataire
     */
    @Column(name = "destinataire", nullable = false, length = 150)
    private String destinataire;
    
    /**
     * Statut : non_lu, lu, traité
     */
    @Column(name = "statut", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatutNotification statut;
    
    /**
     * Date de création de la notification
     */
    @Column(name = "date_creation", nullable = false)
    @CreatedDate
    private LocalDateTime dateCreation;
    
    /**
     * Date de lecture (nullable)
     */
    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;
    
    /**
     * Priority level for notification display
     */
    @Column(name = "priorite")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PrioriteNotification priorite = PrioriteNotification.NORMALE;
    
    /**
     * Source service that generated this notification
     */
    @Column(name = "service_source", length = 100)
    private String serviceSource;
    
    /**
     * Target service for this notification
     */
    @Column(name = "service_cible", length = 100)
    private String serviceCible;
    
    /**
     * Additional metadata in JSON format
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Mark notification as read
     */
    public void marquerCommeLu() {
        this.statut = StatutNotification.LU;
        this.dateLecture = LocalDateTime.now();
    }
    
    /**
     * Mark notification as processed
     */
    public void marquerCommeTraite() {
        this.statut = StatutNotification.TRAITE;
        if (this.dateLecture == null) {
            this.dateLecture = LocalDateTime.now();
        }
    }
    
    /**
     * Check if notification is unread
     */
    public boolean isNonLu() {
        return StatutNotification.NON_LU.equals(this.statut);
    }
    
    /**
     * Check if notification is high priority
     */
    public boolean isHighPriority() {
        return PrioriteNotification.HAUTE.equals(this.priorite) || 
               PrioriteNotification.CRITIQUE.equals(this.priorite);
    }
    
    /**
     * Module types for the notification system
     */
    public enum ModuleType {
        DETTE_TRESOR("Dette Trésor"),
        MEDA("MEDA"),
        DETTE_INTERIEUR("Dette Intérieur"),
        BAM("Bank Al-Maghrib"),
        COMPTABILITE("Comptabilité"),
        DTFE("DTFE"),
        SYSTEME("Système");
        
        private final String displayName;
        
        ModuleType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Notification types
     */
    public enum TypeNotification {
        AVIS_CREDIT("Avis de Crédit"),
        AVIS_DEBIT("Avis de Débit"),
        ORDRE_PAIEMENT("Ordre de Paiement"),
        ERREUR("Erreur"),
        RAPPEL("Rappel"),
        CONNEXION("Connexion Service"),
        DECONNEXION("Déconnexion Service"),
        INTEGRATION("Intégration"),
        VALIDATION("Validation"),
        REJET("Rejet"),
        AUTRE("Autre");
        
        private final String displayName;
        
        TypeNotification(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Notification status
     */
    public enum StatutNotification {
        NON_LU("Non Lu"),
        LU("Lu"),
        TRAITE("Traité");
        
        private final String displayName;
        
        StatutNotification(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Priority levels
     */
    public enum PrioriteNotification {
        BASSE("Basse"),
        NORMALE("Normale"),
        HAUTE("Haute"),
        CRITIQUE("Critique");
        
        private final String displayName;
        
        PrioriteNotification(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Explicit getters for Lombok
    public Long getId() {
        return id;
    }
    
    public ModuleType getModule() {
        return module;
    }
    
    public Long getReferenceId() {
        return referenceId;
    }
    
    public TypeNotification getTypeNotification() {
        return typeNotification;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getDestinataire() {
        return destinataire;
    }
    
    public StatutNotification getStatut() {
        return statut;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public LocalDateTime getDateLecture() {
        return dateLecture;
    }
    
    public PrioriteNotification getPriorite() {
        return priorite;
    }
    
    public String getServiceSource() {
        return serviceSource;
    }
    
    public String getServiceCible() {
        return serviceCible;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    // Explicit builder method
    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }
    
    public static class NotificationBuilder {
        private Long id;
        private ModuleType module;
        private Long referenceId;
        private TypeNotification typeNotification;
        private String message;
        private String destinataire;
        private StatutNotification statut;
        private LocalDateTime dateCreation;
        private LocalDateTime dateLecture;
        private PrioriteNotification priorite = PrioriteNotification.NORMALE;
        private String serviceSource;
        private String serviceCible;
        private String metadata;
        
        public NotificationBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public NotificationBuilder module(ModuleType module) {
            this.module = module;
            return this;
        }
        
        public NotificationBuilder referenceId(Long referenceId) {
            this.referenceId = referenceId;
            return this;
        }
        
        public NotificationBuilder typeNotification(TypeNotification typeNotification) {
            this.typeNotification = typeNotification;
            return this;
        }
        
        public NotificationBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public NotificationBuilder destinataire(String destinataire) {
            this.destinataire = destinataire;
            return this;
        }
        
        public NotificationBuilder statut(StatutNotification statut) {
            this.statut = statut;
            return this;
        }
        
        public NotificationBuilder dateCreation(LocalDateTime dateCreation) {
            this.dateCreation = dateCreation;
            return this;
        }
        
        public NotificationBuilder dateLecture(LocalDateTime dateLecture) {
            this.dateLecture = dateLecture;
            return this;
        }
        
        public NotificationBuilder priorite(PrioriteNotification priorite) {
            this.priorite = priorite;
            return this;
        }
        
        public NotificationBuilder serviceSource(String serviceSource) {
            this.serviceSource = serviceSource;
            return this;
        }
        
        public NotificationBuilder serviceCible(String serviceCible) {
            this.serviceCible = serviceCible;
            return this;
        }
        
        public NotificationBuilder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Notification build() {
            Notification notification = new Notification();
            notification.id = this.id;
            notification.module = this.module;
            notification.referenceId = this.referenceId;
            notification.typeNotification = this.typeNotification;
            notification.message = this.message;
            notification.destinataire = this.destinataire;
            notification.statut = this.statut;
            notification.dateCreation = this.dateCreation;
            notification.dateLecture = this.dateLecture;
            notification.priorite = this.priorite;
            notification.serviceSource = this.serviceSource;
            notification.serviceCible = this.serviceCible;
            notification.metadata = this.metadata;
            return notification;
        }
    }
}