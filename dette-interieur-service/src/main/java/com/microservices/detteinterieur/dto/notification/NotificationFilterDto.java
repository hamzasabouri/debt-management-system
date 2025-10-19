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
 * DTO for notification filters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationFilterDto {
    
    @JsonProperty("module")
    private Notification.ModuleType module;
    
    @JsonProperty("type_notification")
    private Notification.TypeNotification typeNotification;
    
    @JsonProperty("statut")
    private Notification.StatutNotification statut;
    
    @JsonProperty("destinataire")
    private String destinataire;
    
    @JsonProperty("service_source")
    private String serviceSource;
    
    @JsonProperty("date_debut")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime dateDebut;
    
    @JsonProperty("date_fin")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime dateFin;
    
    @JsonProperty("priorite")
    private Notification.PrioriteNotification priorite;
    
    @JsonProperty("page")
    @Builder.Default
    private Integer page = 0;
    
    @JsonProperty("size")
    @Builder.Default
    private Integer size = 20;
    
    @JsonProperty("sort_by")
    @Builder.Default
    private String sortBy = "dateCreation";
    
    @JsonProperty("sort_direction")
    @Builder.Default
    private String sortDirection = "DESC";
    
    // Explicit getters for Lombok
    public Notification.ModuleType getModule() {
        return module;
    }
    
    public Notification.TypeNotification getTypeNotification() {
        return typeNotification;
    }
    
    public Notification.StatutNotification getStatut() {
        return statut;
    }
    
    public String getDestinataire() {
        return destinataire;
    }
    
    public String getServiceSource() {
        return serviceSource;
    }
    
    public LocalDateTime getDateDebut() {
        return dateDebut;
    }
    
    public LocalDateTime getDateFin() {
        return dateFin;
    }
    
    public Notification.PrioriteNotification getPriorite() {
        return priorite;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    // Explicit builder method
    public static NotificationFilterDtoBuilder builder() {
        return new NotificationFilterDtoBuilder();
    }
    
    public static class NotificationFilterDtoBuilder {
        private Notification.ModuleType module;
        private Notification.TypeNotification typeNotification;
        private Notification.StatutNotification statut;
        private String destinataire;
        private String serviceSource;
        private LocalDateTime dateDebut;
        private LocalDateTime dateFin;
        private Notification.PrioriteNotification priorite;
        private Integer page = 0;
        private Integer size = 20;
        private String sortBy = "dateCreation";
        private String sortDirection = "DESC";
        
        public NotificationFilterDtoBuilder module(Notification.ModuleType module) {
            this.module = module;
            return this;
        }
        
        public NotificationFilterDtoBuilder typeNotification(Notification.TypeNotification typeNotification) {
            this.typeNotification = typeNotification;
            return this;
        }
        
        public NotificationFilterDtoBuilder statut(Notification.StatutNotification statut) {
            this.statut = statut;
            return this;
        }
        
        public NotificationFilterDtoBuilder destinataire(String destinataire) {
            this.destinataire = destinataire;
            return this;
        }
        
        public NotificationFilterDtoBuilder serviceSource(String serviceSource) {
            this.serviceSource = serviceSource;
            return this;
        }
        
        public NotificationFilterDtoBuilder dateDebut(LocalDateTime dateDebut) {
            this.dateDebut = dateDebut;
            return this;
        }
        
        public NotificationFilterDtoBuilder dateFin(LocalDateTime dateFin) {
            this.dateFin = dateFin;
            return this;
        }
        
        public NotificationFilterDtoBuilder priorite(Notification.PrioriteNotification priorite) {
            this.priorite = priorite;
            return this;
        }
        
        public NotificationFilterDtoBuilder page(Integer page) {
            this.page = page;
            return this;
        }
        
        public NotificationFilterDtoBuilder size(Integer size) {
            this.size = size;
            return this;
        }
        
        public NotificationFilterDtoBuilder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }
        
        public NotificationFilterDtoBuilder sortDirection(String sortDirection) {
            this.sortDirection = sortDirection;
            return this;
        }
        
        public NotificationFilterDto build() {
            return new NotificationFilterDto(module, typeNotification, statut, destinataire, serviceSource, dateDebut, dateFin, priorite, page, size, sortBy, sortDirection);
        }
    }
}