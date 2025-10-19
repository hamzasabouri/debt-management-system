package com.microservices.detteinterieur.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RoleValidationTest {

    private JwtUtils jwtUtils;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @BeforeEach
    public void setUp() {
        jwtUtils = new JwtUtils();
        // Use reflection to set the private fields
        try {
            java.lang.reflect.Field secretField = JwtUtils.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            secretField.set(jwtUtils, jwtSecret);
            
            java.lang.reflect.Field expirationField = JwtUtils.class.getDeclaredField("jwtExpiration");
            expirationField.setAccessible(true);
            expirationField.set(jwtUtils, 86400000); // 24 hours
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }
    
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    @Test
    public void testAdminRoleValidation() {
        // Generate a token with ADMIN role
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 1L)
                .claim("roles", Arrays.asList("ADMIN"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSignKey(), SignatureAlgorithm.HS512)
                .compact();
        
        // Test role validation
        assertTrue(jwtUtils.canAccessDetteInterieur(token), "ADMIN user should be able to access Dette Intérieur module");
        assertTrue(jwtUtils.isAdmin(token), "User with ADMIN role should be identified as admin");
        assertTrue(jwtUtils.hasRole(token, "ADMIN"), "User should have ADMIN role");
    }
    
    @Test
    public void testDetteInterieurRoleValidation() {
        // Generate a token with DETTE_INTERIEUR role
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 2L)
                .claim("roles", Arrays.asList("DETTE_INTERIEUR"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSignKey(), SignatureAlgorithm.HS512)
                .compact();
        
        // Test role validation
        assertTrue(jwtUtils.canAccessDetteInterieur(token), "DETTE_INTERIEUR user should be able to access Dette Intérieur module");
        assertFalse(jwtUtils.isAdmin(token), "User with DETTE_INTERIEUR role should not be identified as admin");
        assertTrue(jwtUtils.isDetteInterieurUser(token), "User should be identified as DETTE_INTERIEUR user");
        assertTrue(jwtUtils.hasRole(token, "DETTE_INTERIEUR"), "User should have DETTE_INTERIEUR role");
    }
    
    @Test
    public void testMultipleRolesValidation() {
        // Generate a token with multiple roles including ADMIN
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 3L)
                .claim("roles", Arrays.asList("USER", "ADMIN", "DETTE_INTERIEUR"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSignKey(), SignatureAlgorithm.HS512)
                .compact();
        
        // Test role validation
        assertTrue(jwtUtils.canAccessDetteInterieur(token), "User with ADMIN role should be able to access Dette Intérieur module");
        assertTrue(jwtUtils.isAdmin(token), "User with ADMIN role should be identified as admin");
        assertTrue(jwtUtils.isDetteInterieurUser(token), "User with DETTE_INTERIEUR role should be identified as dette interieur user");
    }
    
    @Test
    public void testRoleExtraction() {
        // Generate a token with various role formats
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 4L)
                .claim("roles", "ADMIN,DETTE_INTERIEUR")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSignKey(), SignatureAlgorithm.HS512)
                .compact();
        
        List<String> roles = jwtUtils.extractRoles(token);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("DETTE_INTERIEUR"));
    }
}