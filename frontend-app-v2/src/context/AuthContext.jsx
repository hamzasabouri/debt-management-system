import React, { createContext, useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { login as authServiceLogin, logout as authServiceLogout, initAuth, getCurrentUser } from '../services/authService';

// Create the Authentication Context
const AuthContext = createContext(null);

// Auth Context Provider component
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // Initialize auth state on component mount
  useEffect(() => {
    const initializeAuth = async () => {
      setLoading(true);
      try {
        // Check if token exists in localStorage
        const isTokenInitialized = initAuth();
        
        if (isTokenInitialized) {
          // Get user info from localStorage
          const userInfo = getCurrentUser();
          setUser(userInfo);
        }
      } catch (error) {
        console.error('Authentication initialization failed:', error);
        authServiceLogout();
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  // Login handler
  const login = async (username, password) => {
    setLoading(true);
    try {
      const response = await authServiceLogin(username, password);
      setUser(response.userInfo);
      
      // Redirect based on user role
      if (response.userInfo && response.userInfo.roles) {
        // Check for 'admin' role (case insensitive and trimmed)
        const hasAdminRole = response.userInfo.roles.some(role => 
          role.trim().toLowerCase() === 'admin' ||
          role.trim().toLowerCase().includes('admin')
        );
        
        // Check for 'dette du tresor' role (case insensitive and trimmed)
        const hasDetteTresorRole = response.userInfo.roles.some(role => 
          role.trim().toLowerCase() === 'dette du tresor' || 
          role.trim().toLowerCase() === 'dette du trésor' ||
          role.trim().toLowerCase() === 'dette tresor' ||
          role.trim().toLowerCase() === 'dette trésor'
        );
        
        // Check for 'MEDA' role (case insensitive and trimmed)
        const hasMedaRole = response.userInfo.roles.some(role => {
          const normalizedRole = role.trim().toLowerCase();
          return normalizedRole === 'meda' ||
                 normalizedRole.includes('meda') ||
                 normalizedRole === 'utilisateur meda' ||
                 normalizedRole === 'meda_user' ||
                 normalizedRole === 'meda-service' ||
                 normalizedRole === 'meda service';
        });
        
        // Check for 'dette interieur' role (case insensitive and trimmed)
        const hasDetteInterieurRole = response.userInfo.roles.some(role => 
          role.trim().toLowerCase() === 'dette interieur' || 
          role.trim().toLowerCase() === 'dette intérieur'
        );
        
        if (hasAdminRole) {
          navigate('/admin');
        } else if (hasDetteTresorRole) {
          navigate('/dette-du-tresor');
        } else if (hasMedaRole) {
          navigate('/meda');
        } else if (hasDetteInterieurRole) {
          navigate('/dette-interieur');
        } else {
          navigate('/dashboard');
        }
      } else {
        navigate('/dashboard');
      }
      
      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  // Logout handler
  const logout = () => {
    authServiceLogout();
    setUser(null);
    navigate('/');
  };

  // Check if user has specific role with enhanced matching
  const hasRole = (roleName) => {
    if (!user || !user.roles || !Array.isArray(user.roles)) {
      return false;
    }
    
    // Special handling for admin role - admins can access everything
    if (roleName.trim().toLowerCase() === 'admin') {
      return user.roles.some(role => 
        role.trim().toLowerCase() === 'admin' || 
        role.trim().toLowerCase().includes('admin')
      );
    }
    
    // Special handling for MEDA role
    if (roleName.trim().toLowerCase() === 'meda') {
      return user.roles.some(role => {
        const normalizedRole = role.trim().toLowerCase();
        return normalizedRole === 'meda' || 
               normalizedRole.includes('meda') ||
               normalizedRole === 'meda_user' ||
               normalizedRole === 'utilisateur_meda' ||
               normalizedRole === 'meda-service' ||
               normalizedRole === 'meda service';
      });
    }
    
    // Special handling for DETTE_INTERIEUR role
    if (roleName.trim().toLowerCase() === 'dette_interieur' || roleName.trim().toLowerCase() === 'dette interieur') {
      return user.roles.some(role => {
        const normalizedRole = role.trim().toLowerCase();
        return normalizedRole === 'dette_interieur' || 
               normalizedRole === 'dette interieur' ||
               normalizedRole === 'dette_intérieur' ||
               normalizedRole === 'dette intérieur';
      });
    }
    
    // Create variations of the role name for flexible matching
    const variations = [
      roleName.trim().toLowerCase(),
      `role_${roleName.trim().toLowerCase()}`,
      `${roleName.trim().toLowerCase()}_role`,
      `${roleName.trim().toLowerCase()}_user`,
      `utilisateur_${roleName.trim().toLowerCase()}`,
      `user_${roleName.trim().toLowerCase()}`
    ];
    
    return user.roles.some(userRole => {
      const normalizedUserRole = userRole.trim().toLowerCase();
      return variations.some(variation => 
        normalizedUserRole === variation || normalizedUserRole.includes(variation)
      );
    });
  };

  // Context value
  const value = {
    user,
    loading,
    login,
    logout,
    hasRole,
    isAuthenticated: !!user
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Custom hook to use the auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;