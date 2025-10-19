import axios from 'axios';
import { getCurrentUser } from '../authService';

// Base URL for the user service through the gateway
const USER_API_BASE_URL = '/api/users';
const ROLE_API_BASE_URL = '/api/roles';

// Create axios instances with base URLs
const userApi = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

const roleApi = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Set the authentication token for user API requests
 * @param {string} token - JWT token
 */
export const setUserAuthToken = (token) => {
  if (token) {
    console.log('Setting authentication token for admin service');
    // Ensure token doesn't already have Bearer prefix
    const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
    userApi.defaults.headers.common['Authorization'] = `Bearer ${cleanToken}`;
    roleApi.defaults.headers.common['Authorization'] = `Bearer ${cleanToken}`;
    
    // Get current user to check for admin role
    const user = getCurrentUser();
    console.log('Current user for admin service:', user);
    
    if (user && user.roles) {
      // Check if user has admin role (using flexible role checking)
      const isAdmin = user.roles.some(role => 
        typeof role === 'string' && (
          role.trim().toLowerCase() === 'admin' || 
          role.trim().toLowerCase().includes('admin')
        )
      );
      
      // Add role-enforcement headers for admin users
      if (isAdmin) {
        console.log('Setting admin role enforcement headers for admin service');
        userApi.defaults.headers.common['X-User-Roles'] = 'ROLE_ADMIN';
        userApi.defaults.headers.common['X-Admin-Access'] = 'true';
        userApi.defaults.headers.common['X-Admin-Override'] = 'true';
        roleApi.defaults.headers.common['X-User-Roles'] = 'ROLE_ADMIN';
        roleApi.defaults.headers.common['X-Admin-Access'] = 'true';
        roleApi.defaults.headers.common['X-Admin-Override'] = 'true';
      }
    }
  } else {
    delete userApi.defaults.headers.common['Authorization'];
    delete roleApi.defaults.headers.common['Authorization'];
    delete userApi.defaults.headers.common['X-User-Roles'];
    delete userApi.defaults.headers.common['X-Admin-Access'];
    delete userApi.defaults.headers.common['X-Admin-Override'];
    delete roleApi.defaults.headers.common['X-User-Roles'];
    delete roleApi.defaults.headers.common['X-Admin-Access'];
    delete roleApi.defaults.headers.common['X-Admin-Override'];
  }
  
  // Debug: Print all headers that will be sent
  console.log('Current headers for user service:', userApi.defaults.headers.common);
  console.log('Current headers for role service:', roleApi.defaults.headers.common);
};

/**
 * Handle API errors
 * @param {Object} error - Error object
 */
const handleError = (error) => {
  if (error.response) {
    // Server responded with error status
    if (error.response.status === 401) {
      console.error('Authentication failed for admin service: Token is invalid or expired');
      throw new Error('Access denied: Authentication required');
    } else if (error.response.status === 403) {
      console.error('Authorization failed for admin service: Insufficient permissions');
      throw new Error('Access denied: You do not have permission to access this resource');
    } else if (error.response.status === 404) {
      console.error('Resource not found for admin service:', error.config?.url);
      throw new Error('The requested data could not be found');
    } else if (error.response.status === 400) {
      console.error('Bad request for admin service:', error.response.data);
      throw new Error(error.response.data?.message || 'Invalid request parameters');
    }
    throw new Error(error.response.data?.message || 'API request failed');
  } else if (error.request) {
    // Request was made but no response received
    console.error('Network error for admin service, no response received');
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    console.error('Error during admin service request setup', error.message);
    throw new Error('An unexpected error occurred.');
  }
};

// User Service
class UserService {
  // Get all users
  async getAllUsers() {
    try {
      console.log('Making API call to fetch users...');
      const response = await userApi.get('/users');
      console.log('API response for users:', response);
      
      // Handle different response formats
      let userData = [];
      if (response && typeof response === 'object') {
        if (response.data) {
          // If response.data is an array, use it directly
          if (Array.isArray(response.data)) {
            userData = response.data;
          } 
          // If response.data is an object with a data property that's an array
          else if (response.data.data && Array.isArray(response.data.data)) {
            userData = response.data.data;
          }
          // If response.data is an object, try to find an array property
          else if (typeof response.data === 'object') {
            // Look for any array property in the response
            for (const key in response.data) {
              if (Array.isArray(response.data[key])) {
                userData = response.data[key];
                break;
              }
            }
          }
        } 
        // If response is directly an array
        else if (Array.isArray(response)) {
          userData = response;
        }
        // If response is an object with data property
        else if (response && typeof response === 'object') {
          // Look for any array property in the response
          for (const key in response) {
            if (Array.isArray(response[key])) {
              userData = response[key];
              break;
            }
          }
        }
      }
      
      console.log('Processed user data:', userData);
      return userData;
    } catch (error) {
      console.error('Error in getAllUsers:', error);
      handleError(error);
    }
  }

  // Get all users with their roles
  async getAllUsersWithRoles() {
    try {
      console.log('Making API call to fetch users with roles...');
      // Try different endpoints that might include roles
      const endpoints = [
        '/users?expand=roles',
        '/users?include=roles',
        '/users?withRoles=true',
        '/users-with-roles',
        '/users'
      ];
      
      for (const endpoint of endpoints) {
        try {
          const response = await userApi.get(endpoint);
          console.log(`API response for users with roles from ${endpoint}:`, response);
          
          // Handle different response formats
          let userData = [];
          if (response && typeof response === 'object') {
            if (response.data) {
              // If response.data is an array, use it directly
              if (Array.isArray(response.data)) {
                userData = response.data;
              } 
              // If response.data is an object with a data property that's an array
              else if (response.data.data && Array.isArray(response.data.data)) {
                userData = response.data.data;
              }
              // If response.data is an object, try to find an array property
              else if (typeof response.data === 'object') {
                // Look for any array property in the response
                for (const key in response.data) {
                  if (Array.isArray(response.data[key])) {
                    userData = response.data[key];
                    break;
                  }
                }
              }
            } 
            // If response is directly an array
            else if (Array.isArray(response)) {
              userData = response;
            }
            // If response is an object with data property
            else if (response && typeof response === 'object') {
              // Look for any array property in the response
              for (const key in response) {
                if (Array.isArray(response[key])) {
                  userData = response[key];
                  break;
                }
              }
            }
          }
          
          // Check if any user has roles - if so, this is the correct endpoint
          if (userData.length > 0) {
            const hasRoles = userData.some(user => user.userRoles || user.roles);
            if (hasRoles || endpoint === endpoints[endpoints.length - 1]) {
              console.log('Processed user data with roles:', userData);
              return userData;
            }
          } else if (endpoint === endpoints[endpoints.length - 1]) {
            // If we've tried all endpoints and none work, return the last result
            console.log('Processed user data with roles (fallback):', userData);
            return userData;
          }
        } catch (error) {
          console.log(`Endpoint ${endpoint} failed, trying next one...`, error.message);
          if (endpoint === endpoints[endpoints.length - 1]) {
            throw error; // If this is the last endpoint, rethrow the error
          }
        }
      }
    } catch (error) {
      console.error('Error in getAllUsersWithRoles:', error);
      handleError(error);
    }
  }

  // Get user by ID
  async getUserById(id) {
    try {
      const response = await userApi.get(`/users/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error in getUserById:', error);
      handleError(error);
    }
  }

  // Create a new user
  async createUser(user) {
    try {
      const response = await userApi.post('/users', user);
      return response.data;
    } catch (error) {
      console.error('Error in createUser:', error);
      handleError(error);
    }
  }

  // Update user
  async updateUser(id, user) {
    try {
      const response = await userApi.put(`/users/${id}`, user);
      return response.data;
    } catch (error) {
      console.error('Error in updateUser:', error);
      handleError(error);
    }
  }

  // Delete user
  async deleteUser(id) {
    try {
      const response = await userApi.delete(`/users/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error in deleteUser:', error);
      handleError(error);
    }
  }

  // Search users
  async searchUsers(searchText) {
    try {
      const response = await userApi.get(`/users/search?searchText=${searchText}`);
      return response.data || [];
    } catch (error) {
      console.error('Error in searchUsers:', error);
      handleError(error);
    }
  }

  // Activate user
  async activateUser(id) {
    try {
      const response = await userApi.post(`/users/${id}/activate`);
      return response.data;
    } catch (error) {
      console.error('Error in activateUser:', error);
      handleError(error);
    }
  }

  // Deactivate user
  async deactivateUser(id) {
    try {
      const response = await userApi.post(`/users/${id}/deactivate`);
      return response.data;
    } catch (error) {
      console.error('Error in deactivateUser:', error);
      handleError(error);
    }
  }

  // Assign roles to user
  async assignRolesToUser(assignmentDTO) {
    try {
      const response = await userApi.post('/users/assign-roles', assignmentDTO);
      return response.data;
    } catch (error) {
      console.error('Error in assignRolesToUser:', error);
      handleError(error);
    }
  }

  // Remove role from user
  async removeRoleFromUser(userId, roleId) {
    try {
      const response = await userApi.delete(`/users/${userId}/roles/${roleId}`);
      return response.data;
    } catch (error) {
      console.error('Error in removeRoleFromUser:', error);
      handleError(error);
    }
  }

  // Check if username exists
  async checkUsernameExists(username) {
    try {
      const response = await userApi.get(`/users/exists/username/${username}`);
      return response.data;
    } catch (error) {
      console.error('Error in checkUsernameExists:', error);
      handleError(error);
    }
  }

  // Check if email exists
  async checkEmailExists(email) {
    try {
      const response = await userApi.get(`/users/exists/email/${email}`);
      return response.data;
    } catch (error) {
      console.error('Error in checkEmailExists:', error);
      handleError(error);
    }
  }
}

// Role Service
class RoleService {
  // Get all roles
  async getAllRoles() {
    try {
      console.log('Making API call to fetch roles...');
      const response = await roleApi.get('/roles');
      console.log('API response for roles:', response);
      
      // Handle different response formats
      let roleData = [];
      if (response && typeof response === 'object') {
        if (response.data) {
          // If response.data is an array, use it directly
          if (Array.isArray(response.data)) {
            roleData = response.data;
          } 
          // If response.data is an object with a data property that's an array
          else if (response.data.data && Array.isArray(response.data.data)) {
            roleData = response.data.data;
          }
          // If response.data is an object, try to find an array property
          else if (typeof response.data === 'object') {
            // Look for any array property in the response
            for (const key in response.data) {
              if (Array.isArray(response.data[key])) {
                roleData = response.data[key];
                break;
              }
            }
          }
        } 
        // If response is directly an array
        else if (Array.isArray(response)) {
          roleData = response;
        }
        // If response is an object with data property
        else if (response && typeof response === 'object') {
          // Look for any array property in the response
          for (const key in response) {
            if (Array.isArray(response[key])) {
              roleData = response[key];
              break;
            }
          }
        }
      }
      
      console.log('Processed role data:', roleData);
      return roleData;
    } catch (error) {
      console.error('Error in getAllRoles:', error);
      handleError(error);
    }
  }

  // Get role by ID
  async getRoleById(id) {
    try {
      const response = await roleApi.get(`/roles/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error in getRoleById:', error);
      handleError(error);
    }
  }

  // Get role by name
  async getRoleByName(name) {
    try {
      const response = await roleApi.get(`/roles/name/${name}`);
      return response.data;
    } catch (error) {
      console.error('Error in getRoleByName:', error);
      handleError(error);
    }
  }

  // Create a new role
  async createRole(role) {
    try {
      const response = await roleApi.post('/roles', role);
      return response.data;
    } catch (error) {
      console.error('Error in createRole:', error);
      handleError(error);
    }
  }

  // Update role
  async updateRole(id, role) {
    try {
      const response = await roleApi.put(`/roles/${id}`, role);
      return response.data;
    } catch (error) {
      console.error('Error in updateRole:', error);
      handleError(error);
    }
  }

  // Delete role
  async deleteRole(id) {
    try {
      const response = await roleApi.delete(`/roles/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error in deleteRole:', error);
      handleError(error);
    }
  }

  // Search roles
  async searchRoles(searchText) {
    try {
      const response = await roleApi.get(`/roles/search?searchText=${searchText}`);
      return response.data || [];
    } catch (error) {
      console.error('Error in searchRoles:', error);
      handleError(error);
    }
  }

  // Check if role exists
  async checkRoleExists(name) {
    try {
      const response = await roleApi.get(`/roles/exists/${name}`);
      return response.data;
    } catch (error) {
      console.error('Error in checkRoleExists:', error);
      handleError(error);
    }
  }
}

export default new UserService();
export const roleService = new RoleService();