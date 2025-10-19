import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * ProtectedRoute component - Wraps routes that require authentication
 * Redirects to login page if user is not authenticated
 * 
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child components to render
 * @param {string[]} props.requiredRoles - Optional array of roles required to access this route
 * @returns {React.ReactNode} The protected component or redirect
 */
const ProtectedRoute = ({ children, requiredRoles = [] }) => {
  const { user, loading, isAuthenticated } = useAuth();
  
  // Show loading state while checking authentication
  if (loading) {
    return <div className="loading-screen">Loading...</div>;
  }
  
  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  // Check if user has required roles (if any)
  if (requiredRoles.length > 0 && user && user.roles) {
    // First check if the user has admin role - admins can access everything
    const isAdmin = user.roles.some(role => 
      role.trim().toLowerCase() === 'admin' || 
      role.trim().toLowerCase().includes('admin')
    );
    
    // If user is admin, allow access regardless of other required roles
    if (isAdmin) {
      return children;
    }
    
    // Otherwise, check for the specific required roles
    // Create a helper function for more flexible role matching
    const roleMatches = (userRole, requiredRole) => {
      const normalizedUserRole = userRole.trim().toLowerCase();
      const normalizedRequiredRole = requiredRole.trim().toLowerCase();
      
      // Check direct match first
      if (normalizedUserRole === normalizedRequiredRole) {
        return true;
      }
      
      // Check if user role contains the required role
      if (normalizedUserRole.includes(normalizedRequiredRole)) {
        return true;
      }
      
      // Special case for MEDA role - also check for variations
      if (normalizedRequiredRole === 'meda') {
        const medaVariations = [
          'meda',
          'meda_user',
          'utilisateur_meda',
          'meda-service',
          'meda service'
        ];
        
        return medaVariations.some(variation => 
          normalizedUserRole === variation || normalizedUserRole.includes(variation)
        );
      }
      
      // Check common variations of role names
      const variations = [
        `role_${normalizedRequiredRole}`,
        `${normalizedRequiredRole}_role`,
        `${normalizedRequiredRole}_user`,
        `utilisateur_${normalizedRequiredRole}`,
        `user_${normalizedRequiredRole}`
      ];
      
      return variations.some(variation => normalizedUserRole === variation || normalizedUserRole.includes(variation));
    };
    
    const hasRequiredRole = requiredRoles.some(requiredRole => 
      user.roles.some(userRole => roleMatches(userRole, requiredRole))
    );
    
    if (!hasRequiredRole) {
      console.warn('Access denied: User does not have the required roles', {
        userRoles: user.roles,
        requiredRoles: requiredRoles
      });
      // Redirect to dashboard or unauthorized page if user doesn't have required role
      return <Navigate to="/dashboard" replace />;
    }
  }
  
  // Render the protected content
  return children;
};

export default ProtectedRoute;