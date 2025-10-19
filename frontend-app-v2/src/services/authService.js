import axios from 'axios';

// Create axios instance with base URL
const apiClient = axios.create({
  baseURL: '/api/auth', // This will be proxied to gateway-server at http://localhost:8080
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Set the authentication token for API requests
 * @param {string} token - JWT token
 */
export const setAuthToken = (token) => {
  if (token) {
    // Ensure token doesn't already have Bearer prefix
    const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${cleanToken}`;
    localStorage.setItem('token', cleanToken);
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
    localStorage.removeItem('token');
  }
};

/**
 * Initialize authentication state from localStorage
 */
export const initAuth = () => {
  const token = localStorage.getItem('token');
  if (token) {
    setAuthToken(token);
    return true;
  }
  return false;
};

/**
 * Login user
 * @param {string} username - Username
 * @param {string} password - Password
 * @returns {Promise} Promise object represents the login response
 */
export const login = async (username, password) => {
  try {
    const response = await apiClient.post('/login', { username, password });
    
    if (response.data.token) {
      setAuthToken(response.data.token);
      
      if (response.data.userInfo) {
        localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo));
      }
      
      return response.data;
    } else {
      throw new Error('No token received from server');
    }
  } catch (error) {
    handleError(error);
  }
};

/**
 * Logout user
 */
export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  delete apiClient.defaults.headers.common['Authorization'];
  
  // Optional: Call logout endpoint if backend requires it
  // return apiClient.post('/logout');
};

/**
 * Verify token validity
 * @returns {Promise} Promise object represents the verification result
 */
export const verifyToken = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('No token found');
    }
    
    const response = await apiClient.post('/validate', {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    // If token verification fails, clear token
    logout();
    handleError(error);
  }
};

/**
 * Refresh authentication token
 * @returns {Promise} Promise object represents the refresh result
 */
export const refreshToken = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('No token found');
    }
    
    // For dette-interieur service, we'll handle token refresh at the service level
    // by letting the base service interceptor handle 401 errors
    // This function is kept for backward compatibility
    console.log('Token refresh requested but handled by base service interceptor');
    throw new Error('Token refresh required. Please log in again.');
  } catch (error) {
    // Don't logout here since it might have already been done
    throw error;
  }
};

/**
 * Get current user information
 * @returns {Object|null} User information or null if not logged in
 */
export const getCurrentUser = () => {
  const userInfo = localStorage.getItem('userInfo');
  return userInfo ? JSON.parse(userInfo) : null;
};

/**
 * Check if user has a specific role
 * @param {string} role - Role to check
 * @returns {boolean} True if user has role, false otherwise
 */
export const hasRole = (role) => {
  const user = getCurrentUser();
  if (!user || !user.roles) return false;
  
  // Special handling for admin role
  if (role.trim().toLowerCase() === 'admin') {
    return user.roles.some(userRole => 
      userRole.trim().toLowerCase() === 'admin' ||
      userRole.trim().toLowerCase().includes('admin')
    );
  }
  
  // Special handling for MEDA role
  if (role.trim().toLowerCase() === 'meda') {
    return user.roles.some(userRole => {
      const normalizedRole = userRole.trim().toLowerCase();
      return normalizedRole === 'meda' || 
             normalizedRole.includes('meda') ||
             normalizedRole === 'meda_user' ||
             normalizedRole === 'utilisateur_meda' ||
             normalizedRole === 'meda-service' ||
             normalizedRole === 'meda service';
    });
  }
  
  // Special handling for DETTE_INTERIEUR role
  if (role.trim().toLowerCase() === 'dette_interieur' || role.trim().toLowerCase() === 'dette interieur') {
    return user.roles.some(userRole => {
      const normalizedRole = userRole.trim().toLowerCase();
      return normalizedRole === 'dette_interieur' || 
             normalizedRole === 'dette interieur' ||
             normalizedRole === 'dette_intérieur' ||
             normalizedRole === 'dette intérieur';
    });
  }
  
  // Be more flexible with role matching for other roles
  return user.roles.some(userRole => 
    userRole.trim().toLowerCase() === role.trim().toLowerCase() ||
    userRole.trim().toLowerCase().includes(role.trim().toLowerCase())
  );
};

/**
 * Check if user has the 'dette du tresor' role
 * @returns {boolean} True if user has the role, false otherwise
 */
export const hasDetteTresorRole = () => {
  const user = getCurrentUser();
  if (!user || !user.roles) return false;
  
  // Check for 'dette du tresor' role (case insensitive and trimmed)
  return user.roles.some(role => 
    role.trim().toLowerCase() === 'dette du tresor' || 
    role.trim().toLowerCase() === 'dette du trésor' ||
    role.trim().toLowerCase() === 'dette tresor' ||
    role.trim().toLowerCase() === 'dette trésor'
  );
};

/**
 * Check if user has the 'dette interieur' role
 * @returns {boolean} True if user has the role, false otherwise
 */
export const hasDetteInterieurRole = () => {
  const user = getCurrentUser();
  if (!user || !user.roles) return false;
  
  // Check for 'dette interieur' role (case insensitive and trimmed)
  return user.roles.some(role => 
    role.trim().toLowerCase() === 'dette interieur' || 
    role.trim().toLowerCase() === 'dette intérieur'
  );
};

/**
 * Add MEDA role to admin users to fix permission issues
 * @returns {boolean} True if role was added, false otherwise
 */
export const addMedaRoleToAdmin = () => {
  const userInfoStr = localStorage.getItem('userInfo');
  if (!userInfoStr) return false;
  
  try {
    const userInfo = JSON.parse(userInfoStr);
    if (!userInfo.roles || !Array.isArray(userInfo.roles)) return false;
    
    // Check if user is admin
    const isAdmin = userInfo.roles.some(role => 
      role.trim().toLowerCase() === 'admin' || 
      role.trim().toLowerCase().includes('admin')
    );
    
    if (!isAdmin) return false;
    
    // Check if user already has MEDA role (check for both formats)
    const hasMedaRole = userInfo.roles.some(role => {
      const normalizedRole = role.trim().toLowerCase();
      return normalizedRole === 'meda' || 
             normalizedRole.includes('meda') ||
             normalizedRole === 'role_meda';
    });
    
    // If admin user doesn't have MEDA role, add it
    if (!hasMedaRole) {
      console.log('Adding MEDA role to admin user');
      // Use the proper format that matches backend expectations
      userInfo.roles.push('ROLE_MEDA');
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      return true;
    }
    
    return false;
  } catch (e) {
    console.error('Error updating user roles:', e);
    return false;
  }
};

/**
 * Handle API errors
 * @param {Object} error - Error object
 */
const handleError = (error) => {
  if (error.response) {
    // Server responded with error status
    if (error.response.status === 401) {
      // Unauthorized - clear token
      logout();
      throw new Error('Session expirée. Veuillez vous reconnecter.');
    }
    // Throw a more descriptive error message
    throw new Error(error.response.data.message || `Erreur API: ${error.response.status} - ${error.response.statusText}`);
  } else if (error.request) {
    // Request was made but no response received
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    throw new Error('An unexpected error occurred.');
  }
};

export default {
  login,
  logout,
  initAuth,
  setAuthToken,
  verifyToken,
  refreshToken,
  getCurrentUser,
  hasRole,
  hasDetteTresorRole,
  hasDetteInterieurRole,
  addMedaRoleToAdmin
};