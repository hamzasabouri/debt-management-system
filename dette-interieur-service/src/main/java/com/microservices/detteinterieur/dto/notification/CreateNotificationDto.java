package com.microservices.detteinterieur.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservices.detteinterieur.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating new notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationDto {
    
    @JsonProperty("module")
    @NotNull(message = "Module is required")
    private Notification.ModuleType module;
    
    @JsonProperty("reference_id")
    private Long referenceId;
    
    @JsonProperty("type_notification")
    @NotNull(message = "Notification type is required")
    private Notification.TypeNotification typeNotification;
    
    @JsonProperty("message")
    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;
    
    @JsonProperty("destinataire")
    @NotBlank(message = "Destinataire is required")
    @Size(max = 150, message = "Destinataire must not exceed 150 characters")
    private String destinataire;
    
    @JsonProperty("priorite")
    @Builder.Default
    private Notification.PrioriteNotification priorite = Notification.PrioriteNotification.NORMALE;
    
    @JsonProperty("service_source")
    @Size(max = 100, message = "Service source must not exceed 100 characters")
    private String serviceSource;
    
    @JsonProperty("service_cible")
    @Size(max = 100, message = "Service cible must not exceed 100 characters")
    private String serviceCible;
    
    @JsonProperty("metadata")
    private String metadata;
    
    // Explicit getters for Lombok
    public Notification.ModuleType getModule() {
        return module;
    }
    
    public Long getReferenceId() {
        return referenceId;
    }
    
    public Notification.TypeNotification getTypeNotification() {
        return typeNotification;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getDestinataire() {
        return destinataire;
    }
    
    public Notification.PrioriteNotification getPriorite() {
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
    public static CreateNotificationDtoBuilder builder() {
        return new CreateNotificationDtoBuilder();
    }
    
    public static class CreateNotificationDtoBuilder {
        private Notification.ModuleType module;
        private Long referenceId;
        private Notification.TypeNotification typeNotification;
        private String message;
        private String destinataire;
        private Notification.PrioriteNotification priorite = Notification.PrioriteNotification.NORMALE;
        private String serviceSource;
        private String serviceCible;
        private String metadata;
        
        public CreateNotificationDtoBuilder module(Notification.ModuleType module) {
            this.module = module;
            return this;
        }
        
        public CreateNotificationDtoBuilder referenceId(Long referenceId) {
            this.referenceId = referenceId;
            return this;
        }
        
        public CreateNotificationDtoBuilder typeNotification(Notification.TypeNotification typeNotification) {
            this.typeNotification = typeNotification;
            return this;
        }
        
        public CreateNotificationDtoBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public CreateNotificationDtoBuilder destinataire(String destinataire) {
            this.destinataire = destinataire;
            return this;
        }
        
        public CreateNotificationDtoBuilder priorite(Notification.PrioriteNotification priorite) {
            this.priorite = priorite;
            return this;
        }
        
        public CreateNotificationDtoBuilder serviceSource(String serviceSource) {
            this.serviceSource = serviceSource;
            return this;
        }
        
        public CreateNotificationDtoBuilder serviceCible(String serviceCible) {
            this.serviceCible = serviceCible;
            return this;
        }
        
        public CreateNotificationDtoBuilder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public CreateNotificationDto build() {
            return new CreateNotificationDto(module, referenceId, typeNotification, message, destinataire, priorite, serviceSource, serviceCible, metadata);
        }
    }
}