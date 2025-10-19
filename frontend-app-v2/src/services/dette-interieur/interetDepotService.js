import axios from 'axios';
import { getCurrentUser } from '../authService';
import { createAuthenticatedApi } from '../baseService';

// Create axios instance with base URL for dette-interieur service using the authenticated API creator
const detteInterieurApi = createAuthenticatedApi('/api/dette-interieur');

/**
 * Set the authentication token for API requests
 * @param {string} token - JWT token
 */
export const setAuthToken = (token) => {
  // Authentication is now handled by the baseService interceptor
  // This function is kept for backward compatibility
  console.log('Authentication handled by baseService interceptor');
};

/**
 * Get all deposit interests
 * @returns {Promise} Promise object represents the deposit interests list
 */
export const getAllInteretDepots = async () => {
  try {
    console.log('Making API call to fetch interet depots...');
    const response = await detteInterieurApi.get('/interets-depot');
    console.log('API response for interet depots:', response);
    
    // Handle different response formats
    let data = response.data;
    
    // If response.data is an object with a 'data' property, use that
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      data = response.data.data;
    }
    // If response.data is directly an array, use that
    else if (Array.isArray(response.data)) {
      data = response.data;
    }
    // If response.data is an object with other properties, try to find an array
    else if (response.data && typeof response.data === 'object') {
      // Look for any array property in the response
      for (const key in response.data) {
        if (Array.isArray(response.data[key])) {
          data = response.data[key];
          break;
        }
      }
    }
    
    console.log('Processed interet depots data:', data);
    return data || [];
  } catch (error) {
    console.error('Error in getAllInteretDepots:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getAllInteretDepots:', handledError);
    }
    // Return empty array as fallback to prevent filter errors
    return [];
  }
};

export const getInteretDepotById = async (id) => {
  try {
    const response = await detteInterieurApi.get(`/interets-depot/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error in getInteretDepotById:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getInteretDepotById:', handledError);
    }
    return null;
  }
};

export const createInteretDepot = async (interetDepotData) => {
  try {
    const response = await detteInterieurApi.post('/interets-depot', interetDepotData);
    return response.data;
  } catch (error) {
    console.error('Error in createInteretDepot:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in createInteretDepot:', handledError);
    }
    return null;
  }
};

export const updateInteretDepot = async (id, interetDepotData) => {
  try {
    const response = await detteInterieurApi.put(`/interets-depot/${id}`, interetDepotData);
    return response.data;
  } catch (error) {
    console.error('Error in updateInteretDepot:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in updateInteretDepot:', handledError);
    }
    return null;
  }
};

export const deleteInteretDepot = async (id) => {
  try {
    const response = await detteInterieurApi.delete(`/interets-depot/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error in deleteInteretDepot:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in deleteInteretDepot:', handledError);
    }
    return null;
  }
};

export const searchInteretDepots = async (filters) => {
  try {
    const response = await detteInterieurApi.get('/interets-depot/search', { params: filters });
    return response.data;
  } catch (error) {
    console.error('Error in searchInteretDepots:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in searchInteretDepots:', handledError);
    }
    return [];
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
      console.error('Authentication failed for interets-depot: Token is invalid or expired');
      throw new Error('Access denied: Authentication required for Dette Intérieur Service');
    } else if (error.response.status === 403) {
      console.error('Authorization failed for interets-depot: Insufficient permissions');
      throw new Error('Access denied: You do not have permission to access this resource');
    } else if (error.response.status === 404) {
      console.error('Resource not found for interets-depot:', error.config?.url);
      throw new Error('The requested data could not be found');
    } else if (error.response.status === 400) {
      console.error('Bad request for interets-depot:', error.response.data);
      throw new Error(error.response.data?.message || 'Invalid request parameters');
    }
    throw new Error(error.response.data?.message || 'API request failed');
  } else if (error.request) {
    // Request was made but no response received
    console.error('Network error for interets-depot, no response received');
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    console.error('Error during interets-depot request setup', error.message);
    throw new Error('An unexpected error occurred.');
  }
};

export default {
  setAuthToken,
  getAllInteretDepots,
  getInteretDepotById,
  createInteretDepot,
  updateInteretDepot,
  deleteInteretDepot,
  searchInteretDepots,
};