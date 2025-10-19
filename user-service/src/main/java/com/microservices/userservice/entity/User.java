package com.microservices.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Column(name = "password_hash", nullable = false)
    @NotBlank(message = "Password is required")
    private String passwordHash;
    
    @Column(name = "nom_complet", length = 150)
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String nomComplet;
    
    @Column(name = "email", unique = true, nullable = false, length = 150)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20)
    @Builder.Default
    private UserStatus statut = UserStatus.ACTIF;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();
    
    public enum UserStatus {
        ACTIF, INACTIF
    }
    
    // Helper methods for roles management
    public void addRole(Role role) {
        UserRole userRole = UserRole.builder()
                .user(this)
                .role(role)
                .build();
        this.userRoles.add(userRole);
        role.getUserRoles().add(userRole);
    }
    
    public void removeRole(Role role) {
        UserRole userRole = this.userRoles.stream()
                .filter(ur -> ur.getUser().equals(this) && ur.getRole().equals(role))
                .findFirst()
                .orElse(null);
        
        if (userRole != null) {
            this.userRoles.remove(userRole);
            role.getUserRoles().remove(userRole);
        }
    }
}