package com.microservices.meda.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        log.debug("=== AUTH DEBUG INFO ===");
        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("Authorization header: {}", request.getHeader("Authorization"));
        log.debug("X-Admin-Access header: {}", request.getHeader("X-Admin-Access"));
        log.debug("X-Admin-Override header: {}", request.getHeader("X-Admin-Override"));
        log.debug("X-User-Roles header: {}", request.getHeader("X-User-Roles"));
        log.debug("JWT token extracted: {}", token);

        // Always check for X-User-Roles header as the primary source of authorities
        // This ensures that even if JWT token processing fails, we can still authenticate
        String userRolesHeader = request.getHeader("X-User-Roles");
        if (userRolesHeader != null && !userRolesHeader.isEmpty()) {
            log.debug("Processing roles from X-User-Roles header: {}", userRolesHeader);
            
            // Split roles by comma
            String[] roles = userRolesHeader.split(",");
            
            // Process roles from header
            List<SimpleGrantedAuthority> headerAuthorities = java.util.Arrays.stream(roles)
                    .map(role -> {
                        // Normalize role name: trim, convert to uppercase
                        String normalizedRole = role.trim().toUpperCase();
                        // Ensure ROLE_ prefix for standard Spring Security format
                        // But don't double-prefix if it already has ROLE_
                        if (!normalizedRole.startsWith("ROLE_")) {
                            log.debug("Adding ROLE_ prefix to header role: {}", normalizedRole);
                            return new SimpleGrantedAuthority("ROLE_" + normalizedRole);
                        }
                        log.debug("Header role already has ROLE_ prefix: {}", normalizedRole);
                        return new SimpleGrantedAuthority(normalizedRole);
                    })
                    .collect(Collectors.toList());
            
            // Use header-based authentication
            if (!headerAuthorities.isEmpty()) {
                // For header-based auth, we'll use "system" as username
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken("system", null, headerAuthorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set from header with authorities: {}", headerAuthorities);
            }
        } else {
            // Only process JWT token if no X-User-Roles header is present
            log.debug("No X-User-Roles header found, processing JWT token");
            
            if (token != null && validateToken(token)) {
                log.debug("JWT token is valid, extracting claims");
                try {
                    Claims claims = getClaimsFromToken(token);
                    String username = claims.getSubject();
                    
                    log.debug("Claims extracted from JWT: {}", claims);
                    
                    // Extract roles from JWT token - handle different possible formats
                    List<String> roles = extractRolesFromClaims(claims);
                    
                    log.debug("Roles extracted from JWT claims: {}", roles);
                    
                    // Process roles from JWT token
                    authorities.addAll(roles.stream()
                            .map(role -> {
                                // Normalize role name: trim, convert to uppercase
                                String normalizedRole = role.trim().toUpperCase();
                                // Ensure ROLE_ prefix for standard Spring Security format
                                if (!normalizedRole.startsWith("ROLE_")) {
                                    log.debug("Adding ROLE_ prefix to role: {}", normalizedRole);
                                    return new SimpleGrantedAuthority("ROLE_" + normalizedRole);
                                }
                                log.debug("Role already has ROLE_ prefix: {}", normalizedRole);
                                return new SimpleGrantedAuthority(normalizedRole);
                            })
                            .collect(Collectors.toList()));

                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authentication set for user: {} with roles: {}", username, roles);
                    log.debug("Authorities set for user: {} with authorities: {}", username, authorities);
                    
                } catch (Exception e) {
                    log.error("Error processing JWT token: {}", e.getMessage());
                    log.error("Stack trace: ", e);
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.debug("JWT token is null or invalid");
            }
        }

        log.debug("Final authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        log.debug("=== END AUTH DEBUG INFO ===");
        filterChain.doFilter(request, response);
    }

    /**
     * Extract roles from JWT claims - handles different possible formats
     * @param claims JWT claims
     * @return List of role strings
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromClaims(Claims claims) {
        List<String> roles = new ArrayList<>();
        
        log.debug("Extracting roles from JWT claims: {}", claims);
        
        try {
            // Try to get roles as List<String> directly
            Object rolesObj = claims.get("roles");
            log.debug("Roles object from claims: {} (type: {})", rolesObj, rolesObj != null ? rolesObj.getClass().getName() : "null");
            
            if (rolesObj instanceof List) {
                List<?> rolesList = (List<?>) rolesObj;
                log.debug("Roles list: {}", rolesList);
                for (Object role : rolesList) {
                    log.debug("Processing role: {} (type: {})", role, role != null ? role.getClass().getName() : "null");
                    if (role instanceof String) {
                        roles.add((String) role);
                    }
                }
            } else if (rolesObj instanceof String) {
                // If roles is a single string, split by comma
                String rolesStr = (String) rolesObj;
                log.debug("Roles string: {}", rolesStr);
                String[] rolesArray = rolesStr.split(",");
                for (String role : rolesArray) {
                    roles.add(role.trim());
                }
            } else {
                log.debug("Roles object is neither List nor String: {}", rolesObj);
            }
        } catch (Exception e) {
            log.error("Error extracting roles from JWT claims: {}", e.getMessage());
        }
        
        // If no roles extracted, try to get authorities
        if (roles.isEmpty()) {
            try {
                Object authoritiesObj = claims.get("authorities");
                log.debug("Authorities object from claims: {} (type: {})", authoritiesObj, authoritiesObj != null ? authoritiesObj.getClass().getName() : "null");
                
                if (authoritiesObj instanceof List) {
                    List<?> authoritiesList = (List<?>) authoritiesObj;
                    log.debug("Authorities list: {}", authoritiesList);
                    for (Object authority : authoritiesList) {
                        log.debug("Processing authority: {} (type: {})", authority, authority != null ? authority.getClass().getName() : "null");
                        if (authority instanceof String) {
                            roles.add((String) authority);
                        } else if (authority instanceof java.util.Map) {
                            // Handle Map format (e.g., {"authority": "ROLE_USER"})
                            java.util.Map<String, Object> authorityMap = (java.util.Map<String, Object>) authority;
                            log.debug("Authority map: {}", authorityMap);
                            Object authorityStr = authorityMap.get("authority");
                            log.debug("Authority string from map: {} (type: {})", authorityStr, authorityStr != null ? authorityStr.getClass().getName() : "null");
                            if (authorityStr instanceof String) {
                                roles.add((String) authorityStr);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error extracting authorities from JWT claims: {}", e.getMessage());
            }
        }
        
        log.debug("Extracted roles from JWT claims: {}", roles);
        return roles;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        SecretKey key = getSigningKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get signing key for JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || 
               path.startsWith("/swagger-ui/") || 
               path.startsWith("/api-docs/") ||
               path.startsWith("/v3/api-docs/") ||
               path.equals("/swagger-ui.html");
    }
}