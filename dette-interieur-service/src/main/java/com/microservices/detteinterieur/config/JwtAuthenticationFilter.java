package com.microservices.detteinterieur.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Skip JWT validation for public endpoints
            if (isPublicEndpoint(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            String token = jwtUtils.extractBearerToken(authHeader);
            String username = null;
            
            // Validate and extract user information from token
            if (token != null) {
                try {
                    username = jwtUtils.extractUsername(token);
                    log.debug("Extracted username from token: {}", username);
                } catch (Exception e) {
                    log.warn("Invalid JWT token: {}", e.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Invalid JWT token\"}");
                    return;
                }
            }
            
            // Set authentication context if token is valid
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtils.validateToken(token)) {
                    // Debug: Log user roles
                    try {
                        List<String> userRoles = jwtUtils.extractRoles(token);
                        log.debug("User {} has roles: {}", username, userRoles);
                        
                        // Check if user has access to dette intérieur module
                        boolean canAccess = jwtUtils.canAccessDetteInterieur(token);
                        log.debug("User {} can access dette intérieur module: {}", username, canAccess);
                        
                        if (!canAccess) {
                            log.warn("User {} does not have access to dette intérieur module. User roles: {}", username, userRoles);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\":\"Access denied to dette intérieur module\"}");
                            return;
                        }
                    } catch (Exception e) {
                        log.error("Error checking user roles for user {}: {}", username, e.getMessage());
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Access denied to dette intérieur module\"}");
                        return;
                    }
                    
                    // Extract authorities and create authentication token
                    Collection<? extends GrantedAuthority> authorities = jwtUtils.extractAuthorities(token);
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Add custom claims to authentication details
                    Long userId = jwtUtils.extractUserId(token);
                    if (userId != null) {
                        request.setAttribute("userId", userId);
                    }
                    request.setAttribute("userRoles", jwtUtils.extractRoles(token));
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set for user: {} with authorities: {}", username, authorities);
                } else {
                    log.warn("Invalid or expired JWT token for user: {}", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Token expired or invalid\"}");
                    return;
                }
            } else if (token == null) {
                log.warn("No JWT token found in request to: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Authentication token required\"}");
                return;
            }
            
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Authentication error\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the request URI is a public endpoint that doesn't require authentication
     */
    private boolean isPublicEndpoint(String requestURI) {
        // Define public endpoints that don't require JWT authentication
        return requestURI.startsWith("/actuator/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.equals("/swagger-ui.html") ||
               requestURI.equals("/") ||
               requestURI.equals("/health") ||
               requestURI.equals("/info");
    }
    
    /**
     * Check if request should not be filtered (OPTIONS requests for CORS)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}