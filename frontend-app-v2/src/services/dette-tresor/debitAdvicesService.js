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
    console.log('Setting authentication token for debit advices service');
    // Ensure token doesn't already have Bearer prefix
    const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
    // Set the JWT token in Authorization header with 'Bearer ' prefix
    detteTresorApi.defaults.headers.common['Authorization'] = `Bearer ${cleanToken}`;
    
    // Get current user to check for admin role
    const user = getCurrentUser();
    console.log('Current user for debit advices:', user);
    
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
        console.log('Setting admin role enforcement headers for debit advices service');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'ADMIN,DETTE_TRESOR';
        detteTresorApi.defaults.headers.common['X-Admin-Access'] = 'true';
        detteTresorApi.defaults.headers.common['X-Admin-Override'] = 'true';
      } else {
        // For non-admin users, always include dette-tresor role
        console.log('Setting dette-tresor role header for debit advices');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
      }
    } else {
      // If no user info available, default to dette-tresor role
      console.log('Setting default dette-tresor role header for debit advices');
      detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
    }
  } else {
    delete detteTresorApi.defaults.headers.common['Authorization'];
    delete detteTresorApi.defaults.headers.common['X-User-Roles'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Access'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Override'];
  }
  
  // Debug: Print all headers that will be sent
  console.log('Current headers for debit advices service:', detteTresorApi.defaults.headers.common);
};

/**
 * Get all debit advices
 * @returns {Promise} Promise object represents the debit advice list
 */
export const getAllDebitAdvices = async () => {
  try {
    const response = await detteTresorApi.get('/avis-debits');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get debit advice by ID
 * @param {number} id - Debit advice ID
 * @returns {Promise} Promise object represents the debit advice
 */
export const getDebitAdviceById = async (id) => {
  try {
    const response = await detteTresorApi.get(`/avis-debits/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new debit advice
 * @param {Object} debitAdviceData - Debit advice data
 * @returns {Promise} Promise object represents the created debit advice
 */
export const createDebitAdvice = async (debitAdviceData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.post('/avis-debits', debitAdviceData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update an existing debit advice
 * @param {number} id - Debit advice ID
 * @param {Object} debitAdviceData - Updated debit advice data
 * @returns {Promise} Promise object represents the updated debit advice
 */
export const updateDebitAdvice = async (id, debitAdviceData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.put(`/avis-debits/${id}`, debitAdviceData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Delete a debit advice
 * @param {number} id - Debit advice ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deleteDebitAdvice = async (id) => {
  try {
    const response = await detteTresorApi.delete(`/avis-debits/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Search debit advices by specified field and term
 * @param {string} field - Field to search (numeroAvis, motif, devise, etc.)
 * @param {string} term - Search term
 * @returns {Promise} Promise object represents the filtered debit advice list
 */
export const searchDebitAdvices = async (field, term) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    // Use the backend search endpoint with proper query parameters
    const params = { [field]: term };
    const response = await detteTresorApi.get('/avis-debits/search', { params });
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
  getAllDebitAdvices,
  getDebitAdviceById,
  createDebitAdvice,
  updateDebitAdvice,
  deleteDebitAdvice,
  searchDebitAdvices,
};
