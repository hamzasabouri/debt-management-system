package com.microservices.userservice.repository;

import com.microservices.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by status
     */
    List<User> findByStatut(User.UserStatus statut);
    
    /**
     * Find active users
     */
    @Query("SELECT u FROM User u WHERE u.statut = 'ACTIF'")
    List<User> findActiveUsers();
    
    /**
     * Find users by role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.nomRole = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Find users containing text in username or full name or email
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.nomComplet) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<User> searchUsers(@Param("searchText") String searchText);
}