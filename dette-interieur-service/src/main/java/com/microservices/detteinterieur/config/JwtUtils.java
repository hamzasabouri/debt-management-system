package com.microservices.detteinterieur.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtils {
    
    @Value("${jwt.secret:detteinterieurservicesecretkey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours
    private int jwtExpiration;
    
    /**
     * Extract username from JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract expiration date from JWT token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract authorities from JWT token
     */
    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = null;
        
        try {
            // Try to get roles as List<String> first (new format)
            roles = claims.get("roles", List.class);
        } catch (Exception e) {
            // If that fails, try to get as String (old format)
            try {
                String rolesStr = claims.get("roles", String.class);
                if (rolesStr != null && !rolesStr.isEmpty()) {
                    roles = Arrays.asList(rolesStr.split(","));
                }
            } catch (Exception e2) {
                log.error("Error extracting roles from token: {}", e2.getMessage());
            }
        }
        
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        
        return roles.stream()
                .map(role -> {
                    if (role instanceof String) {
                        // Normalize role name: trim, convert to uppercase, and replace spaces with underscores
                        String normalizedRole = ((String) role).trim().toUpperCase().replace(" ", "_");
                        // Ensure role has ROLE_ prefix as required by Spring Security
                        if (!normalizedRole.startsWith("ROLE_")) {
                            return new SimpleGrantedAuthority("ROLE_" + normalizedRole);
                        } else {
                            return new SimpleGrantedAuthority(normalizedRole);
                        }
                    } else {
                        // Normalize role name: trim, convert to uppercase, and replace spaces with underscores
                        String normalizedRole = role.toString().trim().toUpperCase().replace(" ", "_");
                        // Ensure role has ROLE_ prefix as required by Spring Security
                        if (!normalizedRole.startsWith("ROLE_")) {
                            return new SimpleGrantedAuthority("ROLE_" + normalizedRole);
                        } else {
                            return new SimpleGrantedAuthority(normalizedRole);
                        }
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Extract user ID from JWT token
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * Extract user roles from JWT token
     */
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = null;
        
        try {
            // Try to get roles as List<String> first (new format)
            roles = claims.get("roles", List.class);
        } catch (Exception e) {
            // If that fails, try to get as String (old format)
            try {
                String rolesStr = claims.get("roles", String.class);
                if (rolesStr != null && !rolesStr.isEmpty()) {
                    roles = Arrays.asList(rolesStr.split(","));
                }
            } catch (Exception e2) {
                log.error("Error extracting roles from token: {}", e2.getMessage());
            }
        }
        
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        
        return roles.stream()
                .map(role -> {
                    if (role instanceof String) {
                        return ((String) role).trim();
                    } else {
                        return role.toString().trim();
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Extract a specific claim from JWT token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from JWT token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
    
    /**
     * Check if JWT token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate JWT token without username check
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user has specific role
     */
    public Boolean hasRole(String token, String role) {
        try {
            List<String> roles = extractRoles(token);
            // Convert role to uppercase for comparison
            String roleUpper = role.toUpperCase();
            
            // Check if user has the specific role or is an admin
            boolean hasRole = roles.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .anyMatch(r -> {
                    // Handle different role formats (with space, underscore, etc.)
                    String normalizedRole = r.replace(" ", "_").replaceAll("[_\\s]+", "_");
                    String normalizedTargetRole = roleUpper.replace(" ", "_").replaceAll("[_\\s]+", "_");
                    return normalizedRole.equals(normalizedTargetRole);
                });
            
            // Check if user is admin (special case)
            boolean isAdmin = roles.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .anyMatch(r -> {
                    String normalizedRole = r.replace(" ", "_").replaceAll("[_\\s]+", "_");
                    return normalizedRole.equals("ADMIN");
                });
            
            return hasRole || isAdmin;
        } catch (Exception e) {
            log.error("Error checking user role: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user has admin role
     */
    public Boolean isAdmin(String token) {
        return hasRole(token, "ADMIN");
    }
    
    /**
     * Check if user has dette_interieur role
     */
    public Boolean isDetteInterieurUser(String token) {
        return hasRole(token, "DETTE_INTERIEUR");
    }
    
    /**
     * Check if user can access dette intérieur module
     */
    public Boolean canAccessDetteInterieur(String token) {
        return isAdmin(token) || isDetteInterieurUser(token);
    }
    
    /**
     * Get signing key for JWT
     */
    private Key getSignKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Error creating signing key: {}", e.getMessage());
            throw new RuntimeException("Error creating JWT signing key", e);
        }
    }

    
    /**
     * Generate JWT token with user details and roles
     */
    public String generateToken(String username, Long userId, List<String> roles) {
        String rolesStr = String.join(",", roles);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Refresh JWT token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            List<String> roles = null;
            
            try {
                // Try to get roles as List<String> first (new format)
                roles = claims.get("roles", List.class);
            } catch (Exception e) {
                // If that fails, try to get as String (old format)
                try {
                    String rolesStr = claims.get("roles", String.class);
                    if (rolesStr != null && !rolesStr.isEmpty()) {
                        roles = Arrays.stream(rolesStr.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());
                    }
                } catch (Exception e2) {
                    log.error("Error extracting roles from token: {}", e2.getMessage());
                }
            }
            
            if (roles == null) {
                roles = List.of();
            }
            
            return generateToken(username, userId, roles);
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new RuntimeException("Cannot refresh invalid token", e);
        }
    }
    
    /**
     * Extract bearer token from Authorization header
     */
    public String extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * Get token claims as readable string for logging
     */
    public String getTokenInfo(String token) {
        try {
            Claims claims = extractAllClaims(token);
            List<String> roles = null;
            
            try {
                // Try to get roles as List<String> first (new format)
                roles = claims.get("roles", List.class);
            } catch (Exception e) {
                // If that fails, try to get as String (old format)
                try {
                    String rolesStr = claims.get("roles", String.class);
                    if (rolesStr != null && !rolesStr.isEmpty()) {
                        roles = Arrays.asList(rolesStr.split(","));
                    }
                } catch (Exception e2) {
                    log.error("Error extracting roles from token: {}", e2.getMessage());
                }
            }
            
            String rolesStr = roles != null ? roles.toString() : "[]";
            
            return String.format("User: %s, Roles: %s, Expires: %s", 
                    claims.getSubject(),
                    rolesStr,
                    claims.getExpiration());
        } catch (Exception e) {
            return "Invalid token";
        }
    }
}