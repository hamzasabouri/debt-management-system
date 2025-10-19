import axios from 'axios';
import { getCurrentUser } from '../authService';
import { handle403Error } from '../../utils/authUtils';
import { createAuthenticatedApi } from '../baseService';

// Create axios instance with base URL for meda service using the authenticated API creator
const medaApi = createAuthenticatedApi('/api/meda');

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
 * Get all advances
 * @returns {Promise} Promise object represents the advance list
 */
export const getAllAvances = async () => {
  try {
    const response = await medaApi.get('/avances');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get advance by ID
 * @param {number} id - Advance ID
 * @returns {Promise} Promise object represents the advance
 */
export const getAvanceById = async (id) => {
  try {
    const response = await medaApi.get(`/avances/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new advance
 * @param {Object} avanceData - Advance data
 * @returns {Promise} Promise object represents the created advance
 */
export const createAvance = async (avanceData) => {
  try {
    // Ensure dateReception is properly formatted
    const formattedData = {
      ...avanceData,
      dateReception: avanceData.dateReception || null
    };
    
    // Remove empty fields that might cause issues
    if (!formattedData.dateReception) {
      delete formattedData.dateReception;
    }
    
    const response = await medaApi.post('/avances', formattedData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update an existing advance
 * @param {number} id - Advance ID
 * @param {Object} avanceData - Updated advance data
 * @returns {Promise} Promise object represents the updated advance
 */
export const updateAvance = async (id, avanceData) => {
  try {
    // Ensure dateReception is properly formatted
    const formattedData = {
      ...avanceData,
      dateReception: avanceData.dateReception || null
    };
    
    // Remove empty fields that might cause issues
    if (!formattedData.dateReception) {
      delete formattedData.dateReception;
    }
    
    const response = await medaApi.put(`/avances/${id}`, formattedData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update advance status
 * @param {number} id - Advance ID
 * @param {string} status - New status
 * @returns {Promise} Promise object represents the updated advance
 */
export const updateAvanceStatus = async (id, status) => {
  try {
    const response = await medaApi.put(`/avances/${id}/status`, null, { params: { status } });
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Delete an advance
 * @param {number} id - Advance ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deleteAvance = async (id) => {
  try {
    const response = await medaApi.delete(`/avances/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get advances by project ID
 * @param {number} projetId - Project ID
 * @returns {Promise} Promise object represents the advance list for the project
 */
export const getAvancesByProjet = async (projetId) => {
  try {
    const response = await medaApi.get(`/avances/project/${projetId}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get advances by type
 * @param {string} typeAvance - Advance type (DON/PRET)
 * @returns {Promise} Promise object represents the advance list by type
 */
export const getAvancesByType = async (typeAvance) => {
  try {
    const response = await medaApi.get(`/avances/type/${typeAvance}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get advances by status
 * @param {string} statut - Advance status
 * @returns {Promise} Promise object represents the advance list by status
 */
export const getAvancesByStatus = async (statut) => {
  try {
    const response = await medaApi.get(`/avances/status/${statut}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get advances pending accounting
 * @returns {Promise} Promise object represents the advance list pending accounting
 */
export const getAdvancesPendingAccounting = async () => {
  try {
    const response = await medaApi.get('/avances/pending-accounting');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Handle API errors
 * @param {Object} error - Error object
 */
const handleError = (error) => {
  console.error('API Error:', error);
  
  if (error.response) {
    // Server responded with error status
    const status = error.response.status;
    console.log('Error response details:', {
      status,
      statusText: error.response.statusText,
      data: error.response.data,
      headers: error.response.headers
    });
    
    if (status === 401) {
      throw new Error('Session expirée. Veuillez vous reconnecter.');
    } else if (status === 403) {
      // Try to handle 403 error with emergency fixes
      if (handle403Error()) {
        // If handler returns true, page will reload, so just return
        return;
      }
      
      // Don't redirect, just throw the error for the component to handle
      throw new Error('Accès non autorisé. Erreur 403: Le serveur a refusé votre accès malgré les en-têtes d\'autorisation. Veuillez vérifier que votre compte possède le rôle MEDA.');
    } else if (status === 404) {
      throw new Error('Ressource non trouvée.');
    } else {
      throw new Error(error.response.data?.message || `Erreur API: ${status} - ${error.response.statusText}`);
    }
  } else if (error.request) {
    // Request was made but no response received
    if (error.code === 'ECONNABORTED') {
      throw new Error('La requête a expiré. Le service MEDA ne répond pas.');
    }
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    throw new Error(`An unexpected error occurred: ${error.message}`);
  }
};

export default {
  setAuthToken,
  getAllAvances,
  getAvanceById,
  createAvance,
  updateAvance,
  updateAvanceStatus,
  deleteAvance,
  getAvancesByProjet,
  getAvancesByType,
  getAvancesByStatus,
  getAdvancesPendingAccounting,
};