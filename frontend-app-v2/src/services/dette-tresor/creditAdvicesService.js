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
    console.log('Setting authentication token for credit advices service');
    // Ensure token doesn't already have Bearer prefix
    const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
    // Set the JWT token in Authorization header with 'Bearer ' prefix
    detteTresorApi.defaults.headers.common['Authorization'] = `Bearer ${cleanToken}`;
    
    // Get current user to check for admin role
    const user = getCurrentUser();
    console.log('Current user for credit advices:', user);
    
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
        console.log('Setting admin role enforcement headers for credit advices service');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'ADMIN,DETTE_TRESOR';
        detteTresorApi.defaults.headers.common['X-Admin-Access'] = 'true';
        detteTresorApi.defaults.headers.common['X-Admin-Override'] = 'true';
      } else {
        // For non-admin users, always include dette-tresor role
        console.log('Setting dette-tresor role header for credit advices');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
      }
    } else {
      // If no user info available, default to dette-tresor role
      console.log('Setting default dette-tresor role header for credit advices');
      detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
    }
  } else {
    delete detteTresorApi.defaults.headers.common['Authorization'];
    delete detteTresorApi.defaults.headers.common['X-User-Roles'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Access'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Override'];
  }
  
  // Debug: Print all headers that will be sent
  console.log('Current headers for credit advices service:', detteTresorApi.defaults.headers.common);
};

/**
 * Get all credit advices
 * @returns {Promise} Promise object represents the credit advice list
 */
export const getAllCreditAdvices = async () => {
  try {
    const response = await detteTresorApi.get('/avis-credits');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get credit advice by ID
 * @param {number} id - Credit advice ID
 * @returns {Promise} Promise object represents the credit advice
 */
export const getCreditAdviceById = async (id) => {
  try {
    const response = await detteTresorApi.get(`/avis-credits/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new credit advice
 * @param {Object} creditAdviceData - Credit advice data
 * @returns {Promise} Promise object represents the created credit advice
 */
export const createCreditAdvice = async (creditAdviceData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.post('/avis-credits', creditAdviceData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update an existing credit advice
 * @param {number} id - Credit advice ID
 * @param {Object} creditAdviceData - Updated credit advice data
 * @returns {Promise} Promise object represents the updated credit advice
 */
export const updateCreditAdvice = async (id, creditAdviceData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.put(`/avis-credits/${id}`, creditAdviceData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Delete a credit advice
 * @param {number} id - Credit advice ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deleteCreditAdvice = async (id) => {
  try {
    const response = await detteTresorApi.delete(`/avis-credits/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get credit advice by advice number
 * @param {string} numeroAvis - Credit advice number
 * @returns {Promise} Promise object represents the credit advice
 */
export const getAvisCreditByNumero = async (numeroAvis) => {
  try {
    const response = await detteTresorApi.get(`/avis-credits/numero/${numeroAvis}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get all credit advices for a specific loan
 * @param {number} pretId - Loan ID
 * @returns {Promise} Promise object represents the credit advice list for the loan
 */
export const getAvisCreditsByPretId = async (pretId) => {
  try {
    const response = await detteTresorApi.get(`/avis-credits/pret/${pretId}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Search credit advices by specified field and term
 * @param {string} field - Field to search (numeroAvis, emetteur, typeAvis, etc.)
 * @param {string} term - Search term
 * @returns {Promise} Promise object represents the filtered credit advice list
 */
export const searchCreditAdvices = async (field, term) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    // Use the backend search endpoint with proper query parameters
    const params = { [field]: term };
    const response = await detteTresorApi.get('/avis-credits/search', { params });
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
  getAllCreditAdvices,
  getCreditAdviceById,
  getAvisCreditByNumero,
  getAvisCreditsByPretId,
  createCreditAdvice,
  updateCreditAdvice,
  deleteCreditAdvice,
  searchCreditAdvices,
};