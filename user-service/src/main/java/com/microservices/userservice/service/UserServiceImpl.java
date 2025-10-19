package com.microservices.userservice.service;

import com.microservices.userservice.dto.RoleDTO;
import com.microservices.userservice.dto.UserDTO;
import com.microservices.userservice.dto.UserRoleAssignmentDTO;
import com.microservices.userservice.entity.Role;
import com.microservices.userservice.entity.User;
import com.microservices.userservice.entity.UserRole;
import com.microservices.userservice.repository.RoleRepository;
import com.microservices.userservice.repository.UserRepository;
import com.microservices.userservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating user with username: {}", userDTO.getUsername());
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists: " + userDTO.getUsername());
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }
        
        // Encode password
        String rawPassword = userDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.debug("Raw password: {}", rawPassword);
        log.debug("Encoded password: {}", encodedPassword);
        
        // Create user entity
        User user = User.builder()
                .username(userDTO.getUsername())
                .passwordHash(encodedPassword)
                .nomComplet(userDTO.getNomComplet())
                .email(userDTO.getEmail())
                .statut(userDTO.getStatut() != null ? userDTO.getStatut() : User.UserStatus.ACTIF)
                .build();
        
        User savedUser = userRepository.save(user);
        
        log.info("User created successfully with ID: {}", savedUser.getId());
        return convertToDTO(savedUser);
    }
    
    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        // Check if username is being changed and if it already exists
        if (!existingUser.getUsername().equals(userDTO.getUsername()) && 
            userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists: " + userDTO.getUsername());
        }
        
        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }
        
        // Update user fields
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setNomComplet(userDTO.getNomComplet());
        existingUser.setEmail(userDTO.getEmail());
        
        // Update password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            String rawPassword = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(rawPassword);
            log.debug("Raw password: {}", rawPassword);
            log.debug("Encoded password: {}", encodedPassword);
            existingUser.setPasswordHash(encodedPassword);
        }
        
        if (userDTO.getStatut() != null) {
            existingUser.setStatut(userDTO.getStatut());
        }
        
        User updatedUser = userRepository.save(existingUser);
        
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return convertToDTO(updatedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return convertToDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return convertToDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return convertToDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO findByUsernameOrEmail(String identifier) {
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + identifier));
        return convertToDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findActiveUsers() {
        return userRepository.findActiveUsers()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String searchText) {
        return userRepository.searchUsers(searchText)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserDTO activateUser(Long id) {
        log.info("Activating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        user.setStatut(User.UserStatus.ACTIF);
        User updatedUser = userRepository.save(user);
        
        log.info("User activated successfully with ID: {}", id);
        return convertToDTO(updatedUser);
    }
    
    @Override
    public UserDTO deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        user.setStatut(User.UserStatus.INACTIF);
        User updatedUser = userRepository.save(user);
        
        log.info("User deactivated successfully with ID: {}", id);
        return convertToDTO(updatedUser);
    }
    
    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        
        // Delete user roles first
        userRoleRepository.deleteByUserId(id);
        
        // Delete user
        userRepository.deleteById(id);
        
        log.info("User deleted successfully with ID: {}", id);
    }
    
    @Override
    public UserDTO assignRolesToUser(UserRoleAssignmentDTO assignmentDTO) {
        log.info("Assigning roles to user with ID: {}", assignmentDTO.getUserId());
        
        User user = userRepository.findById(assignmentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + assignmentDTO.getUserId()));
        
        // Clear existing roles
        userRoleRepository.deleteByUserId(assignmentDTO.getUserId());
        
        // Assign new roles
        for (Long roleId : assignmentDTO.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
            
            if (!userRoleRepository.existsByUserIdAndRoleId(assignmentDTO.getUserId(), roleId)) {
                UserRole userRole = UserRole.builder()
                        .user(user)
                        .role(role)
                        .build();
                userRoleRepository.save(userRole);
            }
        }
        
        log.info("Roles assigned successfully to user with ID: {}", assignmentDTO.getUserId());
        return convertToDTO(userRepository.findById(assignmentDTO.getUserId()).get());
    }
    
    @Override
    public UserDTO removeRoleFromUser(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role not found with ID: " + roleId);
        }
        
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
        
        log.info("Role {} removed successfully from user {}", roleId, userId);
        return convertToDTO(userRepository.findById(userId).get());
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = UserDTO.fromEntity(user);



        List<RoleDTO> roles = roleRepository.findByUserId(user.getId())
                .stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());

        userDTO.setRoles(roles);
        return userDTO;
    }

}