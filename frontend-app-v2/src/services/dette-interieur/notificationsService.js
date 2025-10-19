import axios from 'axios';
import { getCurrentUser } from '../authService';
import { createAuthenticatedApi } from '../baseService';

// Create axios instance with base URL for dette-interieur service using the authenticated API creator
const detteInterieurApi = createAuthenticatedApi('/api/dette-interieur');

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
 * Get notifications with filters and pagination
 * @param {Object} filters - Filter parameters
 * @returns {Promise} Promise object represents the notifications list
 */
export const getNotifications = async (filters = {}) => {
  try {
    console.log('Making API call to fetch notifications...');
    const response = await detteInterieurApi.get('/notifications/list', { params: filters });
    console.log('API response for notifications:', response);
    
    // Handle different response formats
    let data = response.data;
    
    // If response.data is an object with a 'data' property, use that
    if (response.data && response.data.data && Array.isArray(response.data.data)) {
      data = response.data.data;
    }
    // If response.data is directly an array, use that
    else if (Array.isArray(response.data)) {
      data = response.data;
    }
    // If response.data is an object with other properties, try to find an array
    else if (response.data && typeof response.data === 'object') {
      // Look for any array property in the response
      for (const key in response.data) {
        if (Array.isArray(response.data[key])) {
          data = response.data[key];
          break;
        }
      }
    }
    
    console.log('Processed notifications data:', data);
    return data || [];
  } catch (error) {
    console.error('Error in getNotifications:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getNotifications:', handledError);
    }
    // Return empty array as fallback to prevent filter errors
    return [];
  }
};

export const getNotificationStats = async () => {
  try {
    const response = await detteInterieurApi.get('/notifications/stats');
    return response.data;
  } catch (error) {
    console.error('Error in getNotificationStats:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getNotificationStats:', handledError);
    }
    return null;
  }
};

export const getServiceHealth = async () => {
  try {
    const response = await detteInterieurApi.get('/notifications/service-health');
    return response.data;
  } catch (error) {
    console.error('Error in getServiceHealth:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in getServiceHealth:', handledError);
    }
    return null;
  }
};

export const createNotification = async (notificationData) => {
  try {
    const response = await detteInterieurApi.post('/notifications', notificationData);
    return response.data;
  } catch (error) {
    console.error('Error in createNotification:', error);
    // Handle 401 Unauthorized errors specifically
    if (error.response && error.response.status === 401) {
      console.log('401 error, handled by base service interceptor');
      handleError(error);
    }
    try {
      handleError(error);
    } catch (handledError) {
      // If handleError threw an error, we still need to return a safe value
      console.error('Handled error in createNotification:', handledError);
    }
    return null;
  }
};

/**
 * Update an existing notification (placeholder - not supported by backend)
 * @param {number} id - Notification ID
 * @param {Object} notificationData - Updated notification data
 * @returns {Promise} Promise object represents the update result
 */
export const updateNotification = async (id, notificationData) => {
  // Placeholder function - backend doesn't support updating notifications
  throw new Error('Update operation not supported for notifications');
};

/**
 * Delete a notification (placeholder - not supported by backend)
 * @param {number} id - Notification ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deleteNotification = async (id) => {
  // Placeholder function - backend doesn't support deleting notifications
  throw new Error('Delete operation not supported for notifications');
};

/**
 * Handle API errors
 * @param {Object} error - Error object
 */
const handleError = (error) => {
  if (error.response) {
    // Server responded with error status
    if (error.response.status === 401) {
      console.error('Authentication failed for notifications: Token is invalid or expired');
      throw new Error('Access denied: Authentication required for Dette Intérieur Service');
    } else if (error.response.status === 403) {
      console.error('Authorization failed for notifications: Insufficient permissions');
      throw new Error('Access denied: You do not have permission to access this resource');
    } else if (error.response.status === 404) {
      console.error('Resource not found for notifications:', error.config?.url);
      throw new Error('The requested data could not be found');
    } else if (error.response.status === 400) {
      console.error('Bad request for notifications:', error.response.data);
      throw new Error(error.response.data?.message || 'Invalid request parameters');
    }
    throw new Error(error.response.data?.message || 'API request failed');
  } else if (error.request) {
    // Request was made but no response received
    console.error('Network error for notifications, no response received');
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    console.error('Error during notifications request setup', error.message);
    throw new Error('An unexpected error occurred.');
  }
};

export default {
  setAuthToken,
  getNotifications,
  getNotificationStats,
  getServiceHealth,
  createNotification,
  updateNotification,
  deleteNotification,
};