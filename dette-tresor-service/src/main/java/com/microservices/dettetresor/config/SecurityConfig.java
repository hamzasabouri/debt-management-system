package com.microservices.dettetresor.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/health/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Admin only endpoints
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/calculations/balance/recalculate-all").hasRole("ADMIN")
                .requestMatchers("/api/integration/bam/loans").hasRole("ADMIN")
                
                // Dette du Tresor role endpoints
                .requestMatchers("/api/prets/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR")
                .requestMatchers("/api/echeanciers/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR")
                .requestMatchers("/api/avis-credit/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR")
                .requestMatchers("/api/ordres-paiement/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR")
                .requestMatchers("/api/lettres-reglement/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR")
                .requestMatchers("/api/avis-debit/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR")
                .requestMatchers("/api/calculations/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR")
                .requestMatchers("/api/integration/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR", "DETTE_INTERIEUR")
                .requestMatchers("/api/pdf/**").hasAnyRole("ADMIN", "DETTE_DU_TRESOR")
                
                // Read-only access for specific endpoints
                .requestMatchers(HttpMethod.GET, "/api/prets/*/statistics").hasAnyRole("ADMIN", "DETTE_DU_TRESOR", "DETTE_INTERIEUR")
                .requestMatchers(HttpMethod.GET, "/api/calculations/balance/statistics").hasAnyRole("ADMIN", "DETTE_DU_TRESOR", "DETTE_INTERIEUR")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );
        
        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}