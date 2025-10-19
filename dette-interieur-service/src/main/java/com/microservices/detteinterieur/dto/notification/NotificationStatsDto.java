package com.microservices.detteinterieur.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStatsDto {
    
    @JsonProperty("total_notifications")
    private Long totalNotifications;
    
    @JsonProperty("unread_notifications")
    private Long unreadNotifications;
    
    @JsonProperty("error_notifications")
    private Long errorNotifications;
    
    @JsonProperty("high_priority_notifications")
    private Long highPriorityNotifications;
    
    @JsonProperty("module_stats")
    private java.util.List<ModuleStatsDto> moduleStats;
    
    @JsonProperty("recent_notifications")
    private java.util.List<NotificationDto> recentNotifications;
    
    @JsonProperty("service_health")
    private java.util.List<ServiceHealthDto> serviceHealth;
    
    // Explicit getters for Lombok
    public Long getTotalNotifications() {
        return totalNotifications;
    }
    
    public Long getUnreadNotifications() {
        return unreadNotifications;
    }
    
    public Long getErrorNotifications() {
        return errorNotifications;
    }
    
    public Long getHighPriorityNotifications() {
        return highPriorityNotifications;
    }
    
    public java.util.List<ModuleStatsDto> getModuleStats() {
        return moduleStats;
    }
    
    public java.util.List<NotificationDto> getRecentNotifications() {
        return recentNotifications;
    }
    
    public java.util.List<ServiceHealthDto> getServiceHealth() {
        return serviceHealth;
    }
    
    // Explicit builder method
    public static NotificationStatsDtoBuilder builder() {
        return new NotificationStatsDtoBuilder();
    }
    
    public static class NotificationStatsDtoBuilder {
        private Long totalNotifications;
        private Long unreadNotifications;
        private Long errorNotifications;
        private Long highPriorityNotifications;
        private java.util.List<ModuleStatsDto> moduleStats;
        private java.util.List<NotificationDto> recentNotifications;
        private java.util.List<ServiceHealthDto> serviceHealth;
        
        public NotificationStatsDtoBuilder totalNotifications(Long totalNotifications) {
            this.totalNotifications = totalNotifications;
            return this;
        }
        
        public NotificationStatsDtoBuilder unreadNotifications(Long unreadNotifications) {
            this.unreadNotifications = unreadNotifications;
            return this;
        }
        
        public NotificationStatsDtoBuilder errorNotifications(Long errorNotifications) {
            this.errorNotifications = errorNotifications;
            return this;
        }
        
        public NotificationStatsDtoBuilder highPriorityNotifications(Long highPriorityNotifications) {
            this.highPriorityNotifications = highPriorityNotifications;
            return this;
        }
        
        public NotificationStatsDtoBuilder moduleStats(java.util.List<ModuleStatsDto> moduleStats) {
            this.moduleStats = moduleStats;
            return this;
        }
        
        public NotificationStatsDtoBuilder recentNotifications(java.util.List<NotificationDto> recentNotifications) {
            this.recentNotifications = recentNotifications;
            return this;
        }
        
        public NotificationStatsDtoBuilder serviceHealth(java.util.List<ServiceHealthDto> serviceHealth) {
            this.serviceHealth = serviceHealth;
            return this;
        }
        
        public NotificationStatsDto build() {
            return new NotificationStatsDto(totalNotifications, unreadNotifications, errorNotifications, highPriorityNotifications, moduleStats, recentNotifications, serviceHealth);
        }
    }
}