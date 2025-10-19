import axios from 'axios';
import { getCurrentUser } from '../authService';

// Create axios instance with base URL for dette-tresor service
const detteTresorApi = axios.create({
  baseURL: '/api/dette-tresor', // This will be proxied to http://localhost:8080 via Vite
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
    console.log('Setting authentication token for echeanciers service');
    // Ensure token doesn't already have Bearer prefix
    const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
    // Set the JWT token in Authorization header with 'Bearer ' prefix
    detteTresorApi.defaults.headers.common['Authorization'] = `Bearer ${cleanToken}`;
    
    // Get current user to check for admin role
    const user = getCurrentUser();
    console.log('Current user for echeanciers:', user);
    
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
        console.log('Setting admin role enforcement headers for echeanciers service');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'ADMIN,DETTE_TRESOR';
        detteTresorApi.defaults.headers.common['X-Admin-Access'] = 'true';
        detteTresorApi.defaults.headers.common['X-Admin-Override'] = 'true';
      } else {
        // For non-admin users, always include dette-tresor role
        console.log('Setting dette-tresor role header for echeanciers');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
      }
    } else {
      // If no user info available, default to dette-tresor role
      console.log('Setting default dette-tresor role header for echeanciers');
      detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
    }
  } else {
    delete detteTresorApi.defaults.headers.common['Authorization'];
    delete detteTresorApi.defaults.headers.common['X-User-Roles'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Access'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Override'];
  }
  
  // Debug: Print all headers that will be sent
  console.log('Current headers for echeanciers service:', detteTresorApi.defaults.headers.common);
};

/**
 * Get all echeanciers
 * @returns {Promise} Promise object represents the echeancier list
 */
export const getAllEcheanciers = async () => {
  try {
    const response = await detteTresorApi.get('/echeanciers');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get echeancier by ID
 * @param {number} id - Echeancier ID
 * @returns {Promise} Promise object represents the echeancier
 */
export const getEcheancierById = async (id) => {
  try {
    const response = await detteTresorApi.get(`/echeanciers/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get echeanciers by loan ID
 * @param {number} loanId - Loan ID
 * @returns {Promise} Promise object represents the echeancier list for the loan
 */
export const getEcheanciersByLoanId = async (loanId) => {
  try {
    const response = await detteTresorApi.get(`/echeanciers/pret/${loanId}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new echeancier
 * @param {Object} echeancierData - Echeancier data
 * @returns {Promise} Promise object represents the created echeancier
 */
export const createEcheancier = async (echeancierData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.post('/echeanciers', echeancierData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update an existing echeancier
 * @param {number} id - Echeancier ID
 * @param {Object} echeancierData - Echeancier data
 * @returns {Promise} Promise object represents the updated echeancier
 */
export const updateEcheancier = async (id, echeancierData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.put(`/echeanciers/${id}`, echeancierData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Delete an echeancier
 * @param {number} id - Echeancier ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deleteEcheancier = async (id) => {
  try {
    const response = await detteTresorApi.delete(`/echeanciers/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Search echeanciers by filters
 * @param {Object} filters - Search filters
 * @returns {Promise} Promise object represents the filtered echeancier list
 */
export const searchEcheanciers = async (filters) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    // Convert filters to query parameters
    const params = {};
    if (filters.numeroEcheance) {
      params.numeroEcheance = filters.numeroEcheance;
    }
    if (filters.pretId) {
      params.pretId = filters.pretId;
    }
    if (filters.statut) {
      params.statut = filters.statut;
    }
    
    const response = await detteTresorApi.get('/echeanciers/search', { params });
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
  if (error.response) {
    // Server responded with error status
    throw new Error(error.response.data.message || 'API request failed');
  } else if (error.request) {
    // Request was made but no response received
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    throw new Error('An unexpected error occurred.');
  }
};

export default {
  setAuthToken,
  getAllEcheanciers,
  getEcheancierById,
  getEcheanciersByLoanId,
  createEcheancier,
  updateEcheancier,
  deleteEcheancier,
  searchEcheanciers,
};