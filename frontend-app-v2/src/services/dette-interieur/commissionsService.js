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
 * Get all commissions
 * @returns {Promise} Promise object represents the commissions list
 */
export const getAllCommissions = async () => {
  try {
    console.log('Making API call to fetch commissions...');
    const response = await detteInterieurApi.get('/commissions');
    console.log('API response for commissions:', response);
    
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
    // If response.data.content exists and is an array (Spring Data pagination)
    else if (response.data && response.data.content && Array.isArray(response.data.content)) {
      data = response.data.content;
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
    
    // Normalize commission properties to handle different naming conventions
    if (Array.isArray(data)) {
      data = data.map(commission => {
        // Based on the actual API response, map fields correctly
        return {
          ...commission,
          // Map fields based on the provided API response
          id: commission.id,
          nom: commission.typeCommission || commission.nom || commission.name || '',
          description: commission.description || commission.desc || '',
          dateCreation: commission.createdAt || commission.dateCreation || commission.created_at || '',
          datePaiement: commission.datePaiement || commission.date_paiement || '',
          montant: commission.montant || commission.amount || 0,
          statut: commission.statut || commission.status || 'ACTIVE',
          numeroReference: commission.numeroReference || commission.numero_reference || commission.reference || '',
          commentaire: commission.commentaire || commission.comment || '',
          // Create a membres field from related fields if needed
          membres: 'Réf: ' + (commission.numeroReference || 'N/A')
        };
      });
    }
    
    console.log('Processed commissions data:', data);
    return data || [];
  } catch (error) {
    console.error('Error in getAllCommissions:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getAllCommissions:', handledError);
    }
    // Return empty array as fallback to prevent filter issues
    return [];
  }
};

export const getCommissionById = async (id) => {
  try {
    const response = await detteInterieurApi.get(`/commissions/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error in getCommissionById:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getCommissionById:', handledError);
    }
    return null;
  }
};

export const createCommission = async (commissionData) => {
  try {
    const response = await detteInterieurApi.post('/commissions', commissionData);
    return response.data;
  } catch (error) {
    console.error('Error in createCommission:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in createCommission:', handledError);
    }
    return null;
  }
};

export const updateCommission = async (id, commissionData) => {
  try {
    const response = await detteInterieurApi.put(`/commissions/${id}`, commissionData);
    return response.data;
  } catch (error) {
    console.error('Error in updateCommission:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in updateCommission:', handledError);
    }
    return null;
  }
};

export const deleteCommission = async (id) => {
  try {
    const response = await detteInterieurApi.delete(`/commissions/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error in deleteCommission:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    // Handle 403 Forbidden errors specifically (only admins can delete)
    else if (error.response && error.response.status === 403) {
      console.log('403 error, authorization failed for delete operation');
      throw new Error('Delete operation requires administrator privileges. Only administrators can delete commissions.');
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in deleteCommission:', handledError);
    }
    return null;
  }
};

export const searchCommissions = async (filters) => {
  try {
    const response = await detteInterieurApi.get('/commissions/search', { params: filters });
    return response.data;
  } catch (error) {
    console.error('Error in searchCommissions:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in searchCommissions:', handledError);
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
      console.error('Authentication failed for commissions: Token is invalid or expired');
      throw new Error('Access denied: Authentication required for Dette Intérieur Service');
    } else if (error.response.status === 403) {
      console.error('Authorization failed for commissions: Insufficient permissions');
      throw new Error('Access denied: You do not have permission to access this resource');
    } else if (error.response.status === 404) {
      console.error('Resource not found for commissions:', error.config?.url);
      throw new Error('The requested data could not be found');
    } else if (error.response.status === 400) {
      console.error('Bad request for commissions:', error.response.data);
      throw new Error(error.response.data?.message || 'Invalid request parameters');
    }
    throw new Error(error.response.data?.message || 'API request failed');
  } else if (error.request) {
    // Request was made but no response received
    console.error('Network error for commissions, no response received');
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    console.error('Error during commissions request setup', error.message);
    throw new Error('An unexpected error occurred.');
  }
};

export default {
  setAuthToken,
  getAllCommissions,
  getCommissionById,
  createCommission,
  updateCommission,
  deleteCommission,
  searchCommissions,
};