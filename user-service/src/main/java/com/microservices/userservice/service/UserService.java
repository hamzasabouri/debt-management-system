package com.microservices.userservice.service;

import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.dto.UserRoleAssignmentDTO;
import com.microservices.userservice.entity.User;

import java.util.List;

public interface UserService {
    
    /**
     * Create a new user
     */
    UserDTO createUser(UserDTO userDTO);
    
    /**
     * Update an existing user
     */
    UserDTO updateUser(Long id, UserDTO userDTO);
    
    /**
     * Find user by ID
     */
    UserDTO findById(Long id);
    
    /**
     * Find user by username
     */
    UserDTO findByUsername(String username);
    
    /**
     * Find user by email
     */
    UserDTO findByEmail(String email);
    
    /**
     * Find user by username or email
     */
    UserDTO findByUsernameOrEmail(String identifier);
    
    /**
     * Find all users
     */
    List<UserDTO> findAllUsers();
    
    /**
     * Find active users
     */
    List<UserDTO> findActiveUsers();
    
    /**
     * Search users by text
     */
    List<UserDTO> searchUsers(String searchText);
    
    /**
     * Activate user
     */
    UserDTO activateUser(Long id);
    
    /**
     * Deactivate user
     */
    UserDTO deactivateUser(Long id);
    
    /**
     * Delete user
     */
    void deleteUser(Long id);
    
    /**
     * Assign roles to user
     */
    UserDTO assignRolesToUser(UserRoleAssignmentDTO assignmentDTO);
    
    /**
     * Remove role from user
     */
    UserDTO removeRoleFromUser(Long userId, Long roleId);
    
    /**
     * Get user entity by username (for authentication)
     */
    User getUserEntityByUsername(String username);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}