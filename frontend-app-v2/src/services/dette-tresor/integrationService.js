import axios from 'axios';

// Create axios instance with base URL for dette-tresor service
const apiClient = axios.create({
  baseURL: '/api/integration', // This will be proxied to the appropriate service
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
 * Test all integrations
 * @returns {Promise} Promise object represents the integration status
 */
export const testAllIntegrations = async () => {
  try {
    const response = await apiClient.get('/health');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Test BAM integration
 * @returns {Promise} Promise object represents the BAM integration status
 */
export const testBAMIntegration = async () => {
  try {
    const response = await apiClient.get('/bam/health');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Test DTFE integration
 * @returns {Promise} Promise object represents the DTFE integration status
 */
export const testDTFEIntegration = async () => {
  try {
    const response = await apiClient.get('/dtfe/health');
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
  testAllIntegrations,
  testBAMIntegration,
  testDTFEIntegration,
};