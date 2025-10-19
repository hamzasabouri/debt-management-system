package com.microservices.dettetresor.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
                log.debug("Extracted username from token: {}", username);
            } catch (Exception e) {
                log.warn("Unable to get JWT Token or Token has expired: {}", e.getMessage());
            }
        } else if (requestTokenHeader != null) {
            log.warn("JWT Token does not begin with Bearer String");
        }
        
        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            if (jwtUtil.validateToken(jwtToken)) {
                // Extract roles and convert to authorities
                List<String> roles = jwtUtil.extractRoles(jwtToken);
                List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase().replace(" ", "_")))
                    .collect(Collectors.toList());
                
                // Extract user ID
                Long userId = jwtUtil.extractUserId(jwtToken);
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Add user ID to authentication details
                authToken.setDetails(new CustomAuthenticationDetails(request, userId));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("User {} authenticated with roles: {}", username, roles);
            } else {
                log.warn("JWT Token validation failed for user: {}", username);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Custom authentication details to include user ID
     */
    public static class CustomAuthenticationDetails extends WebAuthenticationDetailsSource {
        private final Long userId;
        
        public CustomAuthenticationDetails(HttpServletRequest request, Long userId) {
            this.userId = userId;
        }
        
        public Long getUserId() {
            return userId;
        }
    }
}