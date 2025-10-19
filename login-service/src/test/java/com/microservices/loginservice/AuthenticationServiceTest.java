package com.microservices.loginservice;

import com.microservices.loginservice.client.UserServiceClient;
import com.microservices.loginservice.config.JwtUtil;
import com.microservices.loginservice.dto.LoginRequest;
import com.microservices.loginservice.dto.LoginResponse;
import com.microservices.loginservice.dto.UserDTO;
import com.microservices.loginservice.service.AuthenticationServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AuthenticationServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAuthenticateSuccess() {
        // Prepare test data
        String username = "testuser";
        String rawPassword = "testpassword";
        String encodedPassword = new BCryptPasswordEncoder(10).encode(rawPassword);
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(rawPassword);
        
        // Mock the user auth info response
        Map<String, String> userAuthInfo = new HashMap<>();
        userAuthInfo.put("id", "1");
        userAuthInfo.put("username", username);
        userAuthInfo.put("passwordHash", encodedPassword);
        userAuthInfo.put("statut", "ACTIF");
        
        // Mock the full user DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername(username);
        userDTO.setNomComplet("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setStatut("ACTIF");
        
        // Mock the JWT token
        String jwtToken = "mocked.jwt.token";
        
        // Set up mock behavior
        when(userServiceClient.getUserForAuth(username)).thenReturn(userAuthInfo);
        when(userServiceClient.getUserByUsername(username)).thenReturn(userDTO);
        when(jwtUtil.generateToken(1L, username, java.util.List.of())).thenReturn(jwtToken);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        
        // Perform the test
        LoginResponse response = authenticationService.authenticate(loginRequest);
        
        // Verify results
        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUserInfo());
        assertEquals(username, response.getUserInfo().getUsername());
    }

    @Test
    public void testAuthenticateInvalidCredentials() {
        // Prepare test data
        String username = "testuser";
        String rawPassword = "testpassword";
        String wrongPassword = "wrongpassword";
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(wrongPassword);
        
        // Mock the user auth info response with a different password
        String encodedPassword = new BCryptPasswordEncoder(10).encode(rawPassword);
        Map<String, String> userAuthInfo = new HashMap<>();
        userAuthInfo.put("id", "1");
        userAuthInfo.put("username", username);
        userAuthInfo.put("passwordHash", encodedPassword);
        userAuthInfo.put("statut", "ACTIF");
        
        // Set up mock behavior
        when(userServiceClient.getUserForAuth(username)).thenReturn(userAuthInfo);
        when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);
        
        // Perform the test and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(loginRequest);
        });
        
        assertEquals("Authentication failed: Invalid credentials", exception.getMessage());
    }

    @Test
    public void testAuthenticateInactiveUser() {
        // Prepare test data
        String username = "testuser";
        String rawPassword = "testpassword";
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(rawPassword);
        
        // Mock the user auth info response for inactive user
        Map<String, String> userAuthInfo = new HashMap<>();
        userAuthInfo.put("id", "1");
        userAuthInfo.put("username", username);
        userAuthInfo.put("passwordHash", new BCryptPasswordEncoder(10).encode(rawPassword));
        userAuthInfo.put("statut", "INACTIF");
        
        // Set up mock behavior
        when(userServiceClient.getUserForAuth(username)).thenReturn(userAuthInfo);
        
        // Perform the test and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(loginRequest);
        });
        
        assertEquals("Authentication failed: Account is inactive", exception.getMessage());
    }

    @Test
    public void testAuthenticateUserNotFound() {
        // Prepare test data
        String username = "nonexistentuser";
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword("password");
        
        // Set up mock behavior to throw FeignException.NotFound
        when(userServiceClient.getUserForAuth(username)).thenThrow(FeignException.NotFound.class);
        
        // Perform the test and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(loginRequest);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
}