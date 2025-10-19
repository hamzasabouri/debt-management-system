package com.microservices.detteinterieur.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for module statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleStatsDto {
    
    @JsonProperty("module")
    private String module;
    
    @JsonProperty("module_display_name")
    private String moduleDisplayName;
    
    @JsonProperty("total")
    private Long total;
    
    @JsonProperty("unread")
    private Long unread;
    
    @JsonProperty("errors")
    private Long errors;
    
    @JsonProperty("last_activity")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivity;
    
    // Explicit getters for Lombok
    public String getModule() {
        return module;
    }
    
    public String getModuleDisplayName() {
        return moduleDisplayName;
    }
    
    public Long getTotal() {
        return total;
    }
    
    public Long getUnread() {
        return unread;
    }
    
    public Long getErrors() {
        return errors;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    // Explicit builder method
    public static ModuleStatsDtoBuilder builder() {
        return new ModuleStatsDtoBuilder();
    }
    
    public static class ModuleStatsDtoBuilder {
        private String module;
        private String moduleDisplayName;
        private Long total;
        private Long unread;
        private Long errors;
        private LocalDateTime lastActivity;
        
        public ModuleStatsDtoBuilder module(String module) {
            this.module = module;
            return this;
        }
        
        public ModuleStatsDtoBuilder moduleDisplayName(String moduleDisplayName) {
            this.moduleDisplayName = moduleDisplayName;
            return this;
        }
        
        public ModuleStatsDtoBuilder total(Long total) {
            this.total = total;
            return this;
        }
        
        public ModuleStatsDtoBuilder unread(Long unread) {
            this.unread = unread;
            return this;
        }
        
        public ModuleStatsDtoBuilder errors(Long errors) {
            this.errors = errors;
            return this;
        }
        
        public ModuleStatsDtoBuilder lastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
            return this;
        }
        
        public ModuleStatsDto build() {
            return new ModuleStatsDto(module, moduleDisplayName, total, unread, errors, lastActivity);
        }
    }
}