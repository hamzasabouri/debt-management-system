import axios from 'axios';

/**
 * Create an axios instance with proper authentication handling
 * @param {string} baseURL - The base URL for the API
 * @returns {Object} Configured axios instance
 */
export const createAuthenticatedApi = (baseURL) => {
  const api = axios.create({
    baseURL: baseURL || '/api',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Add request interceptor to set authentication headers
  api.interceptors.request.use(
    (config) => {
      console.log('Making request to:', config.baseURL, config.url);
      console.log('Request config:', config);
      
      // Get token from localStorage
      const token = localStorage.getItem('token');
      if (token) {
        // Ensure token doesn't already have Bearer prefix
        const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
        config.headers['Authorization'] = `Bearer ${cleanToken}`;
        console.log('Authorization header set');
      } else {
        console.log('No token found');
      }
      
      // Set appropriate role headers based on the service being accessed
      // Always set the user roles header first
      const userInfoStr = localStorage.getItem('userInfo');
      console.log('User info:', userInfoStr);
      
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          console.log('Parsed user info:', userInfo);
          
          if (userInfo.roles && Array.isArray(userInfo.roles)) {
            // Check if user is admin (more comprehensive check)
            const isAdmin = userInfo.roles.some(role => {
              const normalizedRole = role.trim().toLowerCase();
              return normalizedRole === 'admin' || 
                     normalizedRole.includes('admin') ||
                     normalizedRole === 'administrateur' ||
                     normalizedRole.includes('administrateur');
            });
            
            console.log('Is admin:', isAdmin);
            
            // Always set admin headers for admin users, regardless of the specific endpoint
            if (isAdmin) {
              config.headers['X-Admin-Access'] = 'true';
              config.headers['X-Admin-Override'] = 'true';
              // Add user roles header - use ROLE_ prefix for Spring Security
              if (baseURL && baseURL.includes('dette-interieur')) {
                config.headers['X-User-Roles'] = 'ADMIN,DETTE_INTERIEUR';
                console.log('Admin headers set for dette-interieur service');
              } else {
                config.headers['X-User-Roles'] = 'ROLE_ADMIN,ROLE_MEDA';
                console.log('Admin headers set for MEDA service');
              }
            } else {
              // For non-admin users, set the appropriate role based on service
              if (baseURL && baseURL.includes('dette-interieur')) {
                // For dette-interieur service, set the DETTE_INTERIEUR role
                config.headers['X-User-Roles'] = 'DETTE_INTERIEUR';
                console.log('X-User-Roles header set to DETTE_INTERIEUR for dette-interieur service');
              } else {
                // Always set the MEDA role header for other MEDA service calls
                config.headers['X-User-Roles'] = 'ROLE_MEDA';
                console.log('X-User-Roles header set to ROLE_MEDA');
              }
            }
          }
        } catch (e) {
          console.error('Error parsing user info:', e);
        }
      } else {
        // Fallback if no user info is available
        if (baseURL && baseURL.includes('dette-interieur')) {
          // For dette-interieur service, set the DETTE_INTERIEUR role
          config.headers['X-User-Roles'] = 'DETTE_INTERIEUR';
          console.log('X-User-Roles header set to DETTE_INTERIEUR for dette-interieur service');
        } else {
          // Always set the MEDA role header for other MEDA service calls
          config.headers['X-User-Roles'] = 'ROLE_MEDA';
          console.log('X-User-Roles header set to ROLE_MEDA');
        }
      }
      
      console.log('Final headers:', config.headers);
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // Add response interceptor to handle common errors
  api.interceptors.response.use(
    (response) => {
      return response;
    },
    async (error) => {
      // Handle 401 Unauthorized
      if (error.response && error.response.status === 401) {
        // Only clear token if we're sure it's invalid
        // For now, let the calling code handle token refresh
        console.log('Authentication required. Please log in again.');
      }
      
      // Handle 403 Forbidden
      if (error.response && error.response.status === 403) {
        console.error('Access forbidden:', error.response.data);
        // Instead of redirecting, reject the promise so the calling code can handle it
        console.log('Access forbidden. Check your permissions.');
        return Promise.reject(error);
      }
      
      return Promise.reject(error);
    }
  );

  return api;
};

export default createAuthenticatedApi;