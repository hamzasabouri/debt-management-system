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
    console.log('Setting authentication token for settlement letters service');
    // Set the JWT token in Authorization header with 'Bearer ' prefix
    detteTresorApi.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    
    // Get current user to check for admin role
    const user = getCurrentUser();
    console.log('Current user for settlement letters:', user);
    
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
        console.log('Setting admin role enforcement headers for settlement letters service');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'ADMIN,DETTE_TRESOR';
        detteTresorApi.defaults.headers.common['X-Admin-Access'] = 'true';
        detteTresorApi.defaults.headers.common['X-Admin-Override'] = 'true';
      } else {
        // For non-admin users, always include dette-tresor role
        console.log('Setting dette-tresor role header for settlement letters');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
      }
    } else {
      // If no user info available, default to dette-tresor role
      console.log('Setting default dette-tresor role header for settlement letters');
      detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
    }
  } else {
    delete detteTresorApi.defaults.headers.common['Authorization'];
    delete detteTresorApi.defaults.headers.common['X-User-Roles'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Access'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Override'];
  }
  
  // Debug: Print all headers that will be sent
  console.log('Current headers for settlement letters service:', detteTresorApi.defaults.headers.common);
};

/**
 * Get all settlement letters
 * @returns {Promise} Promise object represents the settlement letter list
 */
export const getAllSettlementLetters = async () => {
  try {
    const response = await detteTresorApi.get('/lettres-reglement');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get settlement letter by ID
 * @param {number} id - Settlement letter ID
 * @returns {Promise} Promise object represents the settlement letter
 */
export const getSettlementLetterById = async (id) => {
  try {
    const response = await detteTresorApi.get(`/lettres-reglement/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get settlement letters by loan ID
 * @param {number} loanId - Loan ID
 * @returns {Promise} Promise object represents the settlement letter list for the loan
 */
export const getSettlementLettersByLoanId = async (loanId) => {
  try {
    const response = await detteTresorApi.get(`/lettres-reglement/pret/${loanId}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get settlement letter by letter number
 * @param {string} letterNumber - Letter number
 * @returns {Promise} Promise object represents the settlement letter
 */
export const getSettlementLetterByNumero = async (letterNumber) => {
  try {
    const response = await detteTresorApi.get(`/lettres-reglement/numero/${letterNumber}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new settlement letter
 * @param {Object} settlementLetterData - Settlement letter data
 * @returns {Promise} Promise object represents the created settlement letter
 */
export const createSettlementLetter = async (settlementLetterData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.post('/lettres-reglement', settlementLetterData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update an existing settlement letter
 * @param {number} id - Settlement letter ID
 * @param {Object} settlementLetterData - Updated settlement letter data
 * @returns {Promise} Promise object represents the updated settlement letter
 */
export const updateSettlementLetter = async (id, settlementLetterData) => {
  try {
    console.log('Updating settlement letter with ID:', id, 'Data:', settlementLetterData);
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.put(`/lettres-reglement/${id}`, settlementLetterData);
    console.log('Settlement letter updated successfully:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error updating settlement letter:', error);
    handleError(error);
  }
};

/**
 * Delete a settlement letter
 * @param {number} id - Settlement letter ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deleteSettlementLetter = async (id) => {
  try {
    const response = await detteTresorApi.delete(`/lettres-reglement/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Generate PDF for a settlement letter
 * @param {number} id - Settlement letter ID
 * @param {boolean} viewMode - View mode (true for inline view, false for download)
 * @returns {Promise} Promise object represents the PDF data
 */
export const generateSettlementLetterPdf = async (id, viewMode = false) => {
  try {
    const response = await detteTresorApi.get(`/lettres-reglement/${id}/pdf`, {
      params: { viewMode },
      responseType: 'blob'
    });
    return response.data;
  } catch (error) {
    console.error('Error generating settlement letter PDF:', error);
    if (error.response) {
      // Server responded with error status
      if (error.response.status === 404) {
        // Try to get the error message from the response
        const errorMessage = error.response.data ? 
          await error.response.data.text().catch(() => 'Settlement letter not found or no associated payment order. Please associate a payment order with this letter before generating the PDF.') :
          'Settlement letter not found or no associated payment order. Please associate a payment order with this letter before generating the PDF.';
        throw new Error(errorMessage);
      } else if (error.response.status === 500) {
        // Try to get the error message from the response
        const errorMessage = error.response.data ? 
          await error.response.data.text().catch(() => 'Internal server error while generating PDF') :
          'Internal server error while generating PDF';
        throw new Error(`Server error: ${errorMessage}`);
      } else {
        throw new Error(error.response.data?.message || `API request failed with status ${error.response.status}`);
      }
    } else if (error.request) {
      // Request was made but no response received
      throw new Error('Network error. Please check your connection.');
    } else {
      // Something else happened
      throw new Error(`An unexpected error occurred: ${error.message}`);
    }
  }
};

/**
 * Search settlement letters by filters
 * @param {Object} filters - Search filters
 * @returns {Promise} Promise object represents the filtered settlement letter list
 */
export const searchSettlementLetters = async (filters) => {
  try {
    console.log('Searching settlement letters with filters:', filters);
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.get('/lettres-reglement/search', { params: filters });
    console.log('Search results:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error in searchSettlementLetters:', error);
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
  getAllSettlementLetters,
  getSettlementLetterById,
  getSettlementLettersByLoanId,
  getSettlementLetterByNumero,
  createSettlementLetter,
  updateSettlementLetter,
  deleteSettlementLetter,
  generateSettlementLetterPdf,
  searchSettlementLetters,
};
