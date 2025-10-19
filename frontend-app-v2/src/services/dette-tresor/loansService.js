import axios from 'axios';

// Create axios instance with base URL for dette-tresor service
const apiClient = axios.create({
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
    // Ensure token doesn't already have Bearer prefix
    const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${cleanToken}`;
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
  }
};

/**
 * Get all loans
 * @returns {Promise} Promise object represents the loan list
 */
export const getAllLoans = async () => {
  try {
    const response = await apiClient.get('/prets');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get loan by ID
 * @param {number} id - Loan ID
 * @returns {Promise} Promise object represents the loan
 */
export const getLoanById = async (id) => {
  try {
    const response = await apiClient.get(`/prets/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new loan
 * @param {Object} loanData - Loan data
 * @returns {Promise} Promise object represents the created loan
 */
export const createLoan = async (loanData) => {
  try {
    const response = await apiClient.post('/prets', loanData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update an existing loan
 * @param {number} id - Loan ID
 * @param {Object} loanData - Updated loan data
 * @returns {Promise} Promise object represents the updated loan
 */
export const updateLoan = async (id, loanData) => {
  try {
    const response = await apiClient.put(`/prets/${id}`, loanData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Delete a loan
 * @param {number} id - Loan ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deleteLoan = async (id) => {
  try {
    const response = await apiClient.delete(`/prets/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Search loans by criteria
 * @param {Object} filters - Search filters
 * @returns {Promise} Promise object represents the search results
 */
export const searchLoans = async (filters) => {
  try {
    const response = await apiClient.get('/prets/search', { params: filters });
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
  getAllLoans,
  getLoanById,
  createLoan,
  updateLoan,
  deleteLoan,
  searchLoans,
};