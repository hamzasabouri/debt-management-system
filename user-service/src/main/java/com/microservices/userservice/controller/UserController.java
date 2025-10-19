package com.microservices.userservice.controller;

import com.microservices.userservice.dto.RoleDTO;
import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.dto.UserRoleAssignmentDTO;
import com.microservices.userservice.entity.Role;
import com.microservices.userservice.entity.User;
import com.microservices.userservice.repository.RoleRepository;
import com.microservices.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    private final RoleRepository roleRepository;
    
    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("Creating new user with username: {}", userDTO.getUsername());
        UserDTO createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Retrieving user with ID: {}", id);
        UserDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        log.info("Retrieving user with username: {}", username);
        UserDTO user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.info("Retrieving user with email: {}", email);
        UserDTO user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Retrieving all users");
        List<UserDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get active users only
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        log.info("Retrieving active users");
        List<UserDTO> users = userService.findActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Search users
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String searchText) {
        log.info("Searching users with text: {}", searchText);
        List<UserDTO> users = userService.searchUsers(searchText);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Activate user
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<UserDTO> activateUser(@PathVariable Long id) {
        log.info("Activating user with ID: {}", id);
        UserDTO user = userService.activateUser(id);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/auth/username/{username}")
    public ResponseEntity<Map<String, String>> getUserForAuth(@PathVariable String username) {
        User user = userService.getUserEntityByUsername(username);
        List<Role> roles = roleRepository.findByUserId(user.getId());
        String rolesString = roles.stream()
                .map(role -> role.getNomRole())
                .collect(Collectors.joining(","));
        
        Map<String, String> authInfo = Map.of(
                "id", user.getId().toString(),
                "username", user.getUsername(),
                "passwordHash", user.getPasswordHash(),
                "statut", user.getStatut().name(),
                "roles", rolesString
        );
        return ResponseEntity.ok(authInfo);
    }

    /**
     * Deactivate user
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<UserDTO> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user with ID: {}", id);
        UserDTO user = userService.deactivateUser(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Assign roles to user
     */
    @PostMapping("/assign-roles")
    public ResponseEntity<UserDTO> assignRolesToUser(
            @Valid @RequestBody UserRoleAssignmentDTO assignmentDTO) {
        log.info("Assigning roles to user with ID: {}", assignmentDTO.getUserId());
        UserDTO user = userService.assignRolesToUser(assignmentDTO);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Remove role from user
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserDTO> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        UserDTO user = userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Check if username exists
     */
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * Check if email exists
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}