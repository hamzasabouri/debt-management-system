package com.microservices.detteinterieur.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservices.detteinterieur.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for notification responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("module")
    private String module;
    
    @JsonProperty("module_display_name")
    private String moduleDisplayName;
    
    @JsonProperty("reference_id")
    private Long referenceId;
    
    @JsonProperty("type_notification")
    private String typeNotification;
    
    @JsonProperty("type_display_name")
    private String typeDisplayName;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("destinataire")
    private String destinataire;
    
    @JsonProperty("statut")
    private String statut;
    
    @JsonProperty("statut_display_name")
    private String statutDisplayName;
    
    @JsonProperty("priorite")
    private String priorite;
    
    @JsonProperty("priorite_display_name")
    private String prioriteDisplayName;
    
    @JsonProperty("service_source")
    private String serviceSource;
    
    @JsonProperty("service_cible")
    private String serviceCible;
    
    @JsonProperty("metadata")
    private String metadata;
    
    @JsonProperty("date_creation")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreation;
    
    @JsonProperty("date_lecture")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateLecture;
    
    @JsonProperty("is_unread")
    private Boolean isUnread;
    
    @JsonProperty("is_high_priority")
    private Boolean isHighPriority;
    
    // Explicit getters for Lombok
    public Long getId() {
        return id;
    }
    
    public String getModule() {
        return module;
    }
    
    public String getModuleDisplayName() {
        return moduleDisplayName;
    }
    
    public Long getReferenceId() {
        return referenceId;
    }
    
    public String getTypeNotification() {
        return typeNotification;
    }
    
    public String getTypeDisplayName() {
        return typeDisplayName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getDestinataire() {
        return destinataire;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public String getStatutDisplayName() {
        return statutDisplayName;
    }
    
    public String getPriorite() {
        return priorite;
    }
    
    public String getPrioriteDisplayName() {
        return prioriteDisplayName;
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
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public LocalDateTime getDateLecture() {
        return dateLecture;
    }
    
    public Boolean getIsUnread() {
        return isUnread;
    }
    
    public Boolean getIsHighPriority() {
        return isHighPriority;
    }
    
    /**
     * Convert entity to DTO
     */
    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .module(notification.getModule().name())
                .moduleDisplayName(notification.getModule().getDisplayName())
                .referenceId(notification.getReferenceId())
                .typeNotification(notification.getTypeNotification().name())
                .typeDisplayName(notification.getTypeNotification().getDisplayName())
                .message(notification.getMessage())
                .destinataire(notification.getDestinataire())
                .statut(notification.getStatut().name())
                .statutDisplayName(notification.getStatut().getDisplayName())
                .priorite(notification.getPriorite().name())
                .prioriteDisplayName(notification.getPriorite().getDisplayName())
                .serviceSource(notification.getServiceSource())
                .serviceCible(notification.getServiceCible())
                .metadata(notification.getMetadata())
                .dateCreation(notification.getDateCreation())
                .dateLecture(notification.getDateLecture())
                .isUnread(notification.isNonLu())
                .isHighPriority(notification.isHighPriority())
                .build();
    }
    
    /**
     * Constructor from entity
     */
    public NotificationDto(Notification notification) {
        this.id = notification.getId();
        this.module = notification.getModule().name();
        this.moduleDisplayName = notification.getModule().getDisplayName();
        this.referenceId = notification.getReferenceId();
        this.typeNotification = notification.getTypeNotification().name();
        this.typeDisplayName = notification.getTypeNotification().getDisplayName();
        this.message = notification.getMessage();
        this.destinataire = notification.getDestinataire();
        this.statut = notification.getStatut().name();
        this.statutDisplayName = notification.getStatut().getDisplayName();
        this.priorite = notification.getPriorite().name();
        this.prioriteDisplayName = notification.getPriorite().getDisplayName();
        this.serviceSource = notification.getServiceSource();
        this.serviceCible = notification.getServiceCible();
        this.metadata = notification.getMetadata();
        this.dateCreation = notification.getDateCreation();
        this.dateLecture = notification.getDateLecture();
        this.isUnread = notification.isNonLu();
        this.isHighPriority = notification.isHighPriority();
    }
    
    // Explicit builder method
    public static NotificationDtoBuilder builder() {
        return new NotificationDtoBuilder();
    }
    
    public static class NotificationDtoBuilder {
        private Long id;
        private String module;
        private String moduleDisplayName;
        private Long referenceId;
        private String typeNotification;
        private String typeDisplayName;
        private String message;
        private String destinataire;
        private String statut;
        private String statutDisplayName;
        private String priorite;
        private String prioriteDisplayName;
        private String serviceSource;
        private String serviceCible;
        private String metadata;
        private LocalDateTime dateCreation;
        private LocalDateTime dateLecture;
        private Boolean isUnread;
        private Boolean isHighPriority;
        
        public NotificationDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public NotificationDtoBuilder module(String module) {
            this.module = module;
            return this;
        }
        
        public NotificationDtoBuilder moduleDisplayName(String moduleDisplayName) {
            this.moduleDisplayName = moduleDisplayName;
            return this;
        }
        
        public NotificationDtoBuilder referenceId(Long referenceId) {
            this.referenceId = referenceId;
            return this;
        }
        
        public NotificationDtoBuilder typeNotification(String typeNotification) {
            this.typeNotification = typeNotification;
            return this;
        }
        
        public NotificationDtoBuilder typeDisplayName(String typeDisplayName) {
            this.typeDisplayName = typeDisplayName;
            return this;
        }
        
        public NotificationDtoBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public NotificationDtoBuilder destinataire(String destinataire) {
            this.destinataire = destinataire;
            return this;
        }
        
        public NotificationDtoBuilder statut(String statut) {
            this.statut = statut;
            return this;
        }
        
        public NotificationDtoBuilder statutDisplayName(String statutDisplayName) {
            this.statutDisplayName = statutDisplayName;
            return this;
        }
        
        public NotificationDtoBuilder priorite(String priorite) {
            this.priorite = priorite;
            return this;
        }
        
        public NotificationDtoBuilder prioriteDisplayName(String prioriteDisplayName) {
            this.prioriteDisplayName = prioriteDisplayName;
            return this;
        }
        
        public NotificationDtoBuilder serviceSource(String serviceSource) {
            this.serviceSource = serviceSource;
            return this;
        }
        
        public NotificationDtoBuilder serviceCible(String serviceCible) {
            this.serviceCible = serviceCible;
            return this;
        }
        
        public NotificationDtoBuilder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public NotificationDtoBuilder dateCreation(LocalDateTime dateCreation) {
            this.dateCreation = dateCreation;
            return this;
        }
        
        public NotificationDtoBuilder dateLecture(LocalDateTime dateLecture) {
            this.dateLecture = dateLecture;
            return this;
        }
        
        public NotificationDtoBuilder isUnread(Boolean isUnread) {
            this.isUnread = isUnread;
            return this;
        }
        
        public NotificationDtoBuilder isHighPriority(Boolean isHighPriority) {
            this.isHighPriority = isHighPriority;
            return this;
        }
        
        public NotificationDto build() {
            return new NotificationDto(id, module, moduleDisplayName, referenceId, typeNotification, typeDisplayName, message, destinataire, statut, statutDisplayName, priorite, prioriteDisplayName, serviceSource, serviceCible, metadata, dateCreation, dateLecture, isUnread, isHighPriority);
        }
    }
}