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
    console.log('Setting authentication token for payment orders service');
    // Ensure token doesn't already have Bearer prefix
    const cleanToken = token.startsWith('Bearer ') ? token.substring(7) : token;
    // Set the JWT token in Authorization header with 'Bearer ' prefix
    detteTresorApi.defaults.headers.common['Authorization'] = `Bearer ${cleanToken}`;
    
    // Get current user to check for admin role
    const user = getCurrentUser();
    console.log('Current user for payment orders:', user);
    
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
        console.log('Setting admin role enforcement headers for payment orders service');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'ADMIN,DETTE_TRESOR';
        detteTresorApi.defaults.headers.common['X-Admin-Access'] = 'true';
        detteTresorApi.defaults.headers.common['X-Admin-Override'] = 'true';
      } else {
        // For non-admin users, always include dette-tresor role
        console.log('Setting dette-tresor role header for payment orders');
        detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
      }
    } else {
      // If no user info available, default to dette-tresor role
      console.log('Setting default dette-tresor role header for payment orders');
      detteTresorApi.defaults.headers.common['X-User-Roles'] = 'DETTE_TRESOR';
    }
  } else {
    delete detteTresorApi.defaults.headers.common['Authorization'];
    delete detteTresorApi.defaults.headers.common['X-User-Roles'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Access'];
    delete detteTresorApi.defaults.headers.common['X-Admin-Override'];
  }
  
  // Debug: Print all headers that will be sent
  console.log('Current headers for payment orders service:', detteTresorApi.defaults.headers.common);
};

/**
 * Get all payment orders
 * @returns {Promise} Promise object represents the payment order list
 */
export const getAllPaymentOrders = async () => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.get('/ordres-paiement');
    return response.data;
  } catch (error) {
    console.error('Error in getAllPaymentOrders:', error);
    handleError(error);
  }
};

/**
 * Get payment order by ID
 * @param {number} id - Payment order ID
 * @returns {Promise} Promise object represents the payment order
 */
export const getPaymentOrderById = async (id) => {
  try {
    const response = await detteTresorApi.get(`/ordres-paiement/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new payment order
 * @param {Object} paymentOrderData - Payment order data
 * @returns {Promise} Promise object represents the created payment order
 */
export const createPaymentOrder = async (paymentOrderData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.post('/ordres-paiement', paymentOrderData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Update an existing payment order
 * @param {number} id - Payment order ID
 * @param {Object} paymentOrderData - Updated payment order data
 * @returns {Promise} Promise object represents the updated payment order
 */
export const updatePaymentOrder = async (id, paymentOrderData) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.put(`/ordres-paiement/${id}`, paymentOrderData);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Delete a payment order
 * @param {number} id - Payment order ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deletePaymentOrder = async (id) => {
  try {
    const response = await detteTresorApi.delete(`/ordres-paiement/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Search payment orders by criteria
 * @param {Object} filters - Search filters
 * @returns {Promise} Promise object represents the search results
 */
export const searchPaymentOrders = async (filters) => {
  try {
    // Ensure token is set before making request
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    }
    
    const response = await detteTresorApi.get('/ordres-paiement/search', { params: filters });
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
  getAllPaymentOrders,
  getPaymentOrderById,
  createPaymentOrder,
  updatePaymentOrder,
  deletePaymentOrder,
  searchPaymentOrders,
};