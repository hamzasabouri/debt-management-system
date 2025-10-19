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
 * Get all equipment bond notices
 * @returns {Promise} Promise object represents the equipment bond notices list
 */
export const getAllAvisBonEquipements = async () => {
  try {
    console.log('Making API call to fetch avis bon equipements...');
    const response = await detteInterieurApi.get('/avis-bon-equipements');
    console.log('API response for avis bon equipements:', response);
    
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
    
    console.log('Processed avis bon equipements data:', data);
    return data || [];
  } catch (error) {
    console.error('Error in getAllAvisBonEquipements:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getAllAvisBonEquipements:', handledError);
    }
    // Return empty array as fallback to prevent filter errors
    return [];
  }
};

export const getAvisBonEquipementById = async (id) => {
  try {
    const response = await detteInterieurApi.get(`/avis-bon-equipements/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error in getAvisBonEquipementById:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getAvisBonEquipementById:', handledError);
    }
    return null;
  }
};

export const createAvisBonEquipement = async (avisBonEquipementData) => {
  try {
    const response = await detteInterieurApi.post('/avis-bon-equipements', avisBonEquipementData);
    return response.data;
  } catch (error) {
    console.error('Error in createAvisBonEquipement:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in createAvisBonEquipement:', handledError);
    }
    return null;
  }
};

export const updateAvisBonEquipement = async (id, avisBonEquipementData) => {
  try {
    const response = await detteInterieurApi.put(`/avis-bon-equipements/${id}`, avisBonEquipementData);
    return response.data;
  } catch (error) {
    console.error('Error in updateAvisBonEquipement:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in updateAvisBonEquipement:', handledError);
    }
    return null;
  }
};

export const deleteAvisBonEquipement = async (id) => {
  try {
    const response = await detteInterieurApi.delete(`/avis-bon-equipements/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error in deleteAvisBonEquipement:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in deleteAvisBonEquipement:', handledError);
    }
    return null;
  }
};

export const searchAvisBonEquipements = async (filters) => {
  try {
    const response = await detteInterieurApi.get('/avis-bon-equipements/search', { params: filters });
    return response.data;
  } catch (error) {
    console.error('Error in searchAvisBonEquipements:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in searchAvisBonEquipements:', handledError);
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
      console.error('Authentication failed for avis-bon-equipements: Token is invalid or expired');
      throw new Error('Access denied: Authentication required for Dette Intérieur Service');
    } else if (error.response.status === 403) {
      console.error('Authorization failed for avis-bon-equipements: Insufficient permissions');
      throw new Error('Access denied: You do not have permission to access this resource');
    } else if (error.response.status === 404) {
      console.error('Resource not found for avis-bon-equipements:', error.config?.url);
      throw new Error('The requested data could not be found');
    } else if (error.response.status === 400) {
      console.error('Bad request for avis-bon-equipements:', error.response.data);
      throw new Error(error.response.data?.message || 'Invalid request parameters');
    }
    throw new Error(error.response.data?.message || 'API request failed');
  } else if (error.request) {
    // Request was made but no response received
    console.error('Network error for avis-bon-equipements, no response received');
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    console.error('Error during avis-bon-equipements request setup', error.message);
    throw new Error('An unexpected error occurred.');
  }
};

export default {
  setAuthToken,
  getAllAvisBonEquipements,
  getAvisBonEquipementById,
  createAvisBonEquipement,
  updateAvisBonEquipement,
  deleteAvisBonEquipement,
  searchAvisBonEquipements,
};