package com.microservices.detteinterieur.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAspect {
    
    private final JwtUtils jwtUtils;
    
    /**
     * Log all sensitive operations before execution
     */
    @Before("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    public void logSecureOperation(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            // Get request details
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                
                log.info("Secure operation executed - User: {}, Method: {}.{}, IP: {}, UserAgent: {}", 
                        username, className, methodName, ipAddress, userAgent);
            } else {
                log.info("Secure operation executed - User: {}, Method: {}.{}", 
                        username, className, methodName);
            }
        }
    }
    
    /**
     * Log all administrative operations
     */
    @Before("execution(* com.microservices.detteinterieur.service.*Service.delete*(..))")
    public void logDeleteOperations(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();
            
            log.warn("DELETE operation - User: {}, Method: {}, Args: {}", 
                    username, methodName, args);
        }
    }
    
    /**
     * Log all update operations that change critical data
     */
    @Before("execution(* com.microservices.detteinterieur.service.*Service.update*(..)) || " +
            "execution(* com.microservices.detteinterieur.service.*Service.process*(..)) || " +
            "execution(* com.microservices.detteinterieur.service.*Service.account*(..)) || " +
            "execution(* com.microservices.detteinterieur.service.*Service.reject*(..)) || " +
            "execution(* com.microservices.detteinterieur.service.*Service.cancel*(..)) || " +
            "execution(* com.microservices.detteinterieur.service.*Service.transmit*(..))")
    public void logCriticalOperations(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();
            
            // Extract entity ID if available
            String entityId = "unknown";
            if (args.length > 0 && args[0] instanceof Long) {
                entityId = args[0].toString();
            }
            
            log.info("CRITICAL operation - User: {}, Method: {}, EntityID: {}", 
                    username, methodName, entityId);
        }
    }
    
    /**
     * Log all statistical and reporting operations
     */
    @Before("execution(* com.microservices.detteinterieur.service.*Service.get*Statistics*(..)) || " +
            "execution(* com.microservices.detteinterieur.service.*Service.get*Report*(..)) || " +
            "execution(* com.microservices.detteinterieur.service.*Service.getTotal*(..)) || " +
            "execution(* com.microservices.detteinterieur.service.*Service.getAverage*(..))")
    public void logReportingOperations(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            String methodName = joinPoint.getSignature().getName();
            
            log.info("REPORTING operation - User: {}, Method: {}", username, methodName);
        }
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Check if user has administrative privileges
     */
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
    
    /**
     * Check if user has dette_interieur privileges
     */
    private boolean isDetteInterieurUser(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DETTE_INTERIEUR"));
    }
}