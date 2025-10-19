package com.microservices.userservice.repository;

import com.microservices.userservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    /**
     * Find user-role relationship by user ID and role ID
     */
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);
    
    /**
     * Find all user-role relationships for a user
     */
    List<UserRole> findByUserId(Long userId);
    
    /**
     * Find all user-role relationships for a role
     */
    List<UserRole> findByRoleId(Long roleId);
    
    /**
     * Check if user has specific role
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    
    /**
     * Delete user-role relationship by user ID and role ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * Delete all roles for a user
     */
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
    
    /**
     * Delete all users for a role
     */
    @Modifying
    @Transactional
    void deleteByRoleId(Long roleId);
}