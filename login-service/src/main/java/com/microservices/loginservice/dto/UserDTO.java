package com.microservices.loginservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Long id;
    private String username;
    private String password; // Changed from passwordHash to match user service
    private String nomComplet;
    private String email;
    private String statut;
    private LocalDateTime createdAt;
    private List<RoleDTO> roles;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleDTO {
        private Long id;
        private String nomRole;
        private String description;
    }
}