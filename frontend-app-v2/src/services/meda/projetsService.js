import axios from 'axios';
import { getCurrentUser } from '../authService';
import { handle403Error } from '../../utils/authUtils';
import { createAuthenticatedApi } from '../baseService';

// Create axios instance with base URL for meda service using the authenticated API creator
export const medaApi = createAuthenticatedApi('/api/meda');

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
 * Get all projects with retry mechanism
 * @param {number} retries - Number of retry attempts
 * @returns {Promise} Promise object represents the project list
 */
export const getAllProjets = async (retries = 1) => {
  try {
    // Add timeout to prevent hanging requests
    const response = await medaApi.get('/projets/all', {
      timeout: 10000 // 10 seconds timeout
    });
    return response.data;
  } catch (error) {
    // Log detailed error information
    console.error('MEDA API error details:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      headers: error.response?.headers
    });
    
    // Don't retry on authentication errors to prevent repeated unauthorized requests
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      console.log('Authentication error - not retrying to prevent repeated unauthorized requests');
      handleError(error);
      return;
    }
    
    if (retries > 0 && (error.code === 'ECONNABORTED' || !error.response)) {
      // Only retry network errors or timeout errors
      console.log(`Retrying GET /projets/all (${retries} attempts left)`);
      // Wait 1 second before retrying
      await new Promise(resolve => setTimeout(resolve, 1000));
      return getAllProjets(retries - 1);
    }
    handleError(error);
  }
};

/**
 * Get project by ID
 * @param {number} id - Project ID
 * @returns {Promise} Promise object represents the project
 */
export const getProjetById = async (id) => {
  try {
    const response = await medaApi.get(`/projets/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get project financial details by ID
 * @param {number} id - Project ID
 * @returns {Promise} Promise object represents the project financial details
 */
export const getProjetFinancialDetails = async (id) => {
  try {
    const response = await medaApi.get(`/projets/${id}/financial`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new project
 * @param {Object} projetData - Project data
 * @returns {Promise} Promise object represents the created project
 */
export const createProjet = async (projetData) => {
  try {
    const response = await medaApi.post('/projets', projetData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update an existing project
 * @param {number} id - Project ID
 * @param {Object} projetData - Updated project data
 * @returns {Promise} Promise object represents the updated project
 */
export const updateProjet = async (id, projetData) => {
  try {
    const response = await medaApi.put(`/projets/${id}`, projetData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Delete a project
 * @param {number} id - Project ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deleteProjet = async (id) => {
  try {
    const response = await medaApi.delete(`/projets/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get active projects
 * @returns {Promise} Promise object represents the active project list
 */
export const getActiveProjets = async () => {
  try {
    const response = await medaApi.get('/projets/active');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

;

/**
 * Get completed projects
 * @returns {Promise} Promise object represents the completed project list
 */
export const getCompletedProjets = async () => {
  try {
    const response = await medaApi.get('/projets/completed');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Search projects by term
 * @param {string} searchTerm - Search term
 * @returns {Promise} Promise object represents the search results
 */
export const searchProjets = async (searchTerm) => {
  try {
    const response = await medaApi.get('/projets/search', { params: { searchTerm } });
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get project statistics
 * @returns {Promise} Promise object represents the project statistics
 */
export const getProjetStatistics = async () => {
  try {
    // Check if token is available
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('No authentication token found');
    }
    
    // Also check if user info is available
    const userInfoStr = localStorage.getItem('userInfo');
    if (!userInfoStr) {
      throw new Error('No user information found');
    }
    
    try {
      JSON.parse(userInfoStr);
    } catch (e) {
      throw new Error('Invalid user information format');
    }
    
    const response = await medaApi.get('/projets/statistics');
    return response.data;
  } catch (error) {
    console.error('Error fetching project statistics:', error);
    // Don't retry on authentication errors to prevent repeated unauthorized requests
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      // Handle authentication errors specifically
      console.log('Authentication error - not retrying to prevent repeated unauthorized requests');
      throw error;
    }
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
  getAllProjets,
  getProjetById,
  getProjetFinancialDetails,
  createProjet,
  updateProjet,
  deleteProjet,
  getActiveProjets,
  getCompletedProjets,
  searchProjets,
  getProjetStatistics,
};