import axios from 'axios';

// Create axios instance with base URL for dette-tresor service
const apiClient = axios.create({
  baseURL: '/api/dette-tresor/calculs', // This will be proxied to the appropriate service
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
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
  }
};

/**
 * Get total outstanding debt by currency
 * @returns {Promise} Promise object represents the debt statistics
 */
export const getTotalOutstandingDebtByCurrency = async () => {
  try {
    const response = await apiClient.get('/dette-encours-par-devise');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get active loans count
 * @returns {Promise} Promise object represents the active loans count
 */
export const getActiveLoansCount = async () => {
  try {
    const response = await apiClient.get('/prets-actifs');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get fully paid loans count
 * @returns {Promise} Promise object represents the fully paid loans count
 */
export const getFullyPaidLoansCount = async () => {
  try {
    const response = await apiClient.get('/prets-rembourses');
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
  getTotalOutstandingDebtByCurrency,
  getActiveLoansCount,
  getFullyPaidLoansCount,
};