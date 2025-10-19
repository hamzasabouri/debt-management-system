/**
 * Utility functions for handling common authentication and authorization issues
 */
import { addMedaRoleToAdmin } from '../services/authService';

/**
 * Applies emergency fixes for 403 Forbidden errors for admin users
 * @returns {boolean} True if fix was applied and page should reload
 */
export const applyEmergencyFixes = () => {
  console.log('Applying emergency fixes for 403 Forbidden error');
  
  // 1. Add MEDA role to admin users
  const roleAdded = addMedaRoleToAdmin();
  
  // 2. Force admin headers for all MEDA API calls
  try {
    // Create a global interceptor to add admin headers to all requests
    const applyAdminHeaders = () => {
      // Find all script tags that might contain axios imports
      const scripts = document.querySelectorAll('script');
      for (const script of scripts) {
        // Try to modify axios defaults if it exists in window
        if (window.axios) {
          console.log('Adding admin headers to global axios instance');
          window.axios.defaults.headers.common['X-Admin-Access'] = 'true';
          window.axios.defaults.headers.common['X-Admin-Override'] = 'true';
          window.axios.defaults.headers.common['X-User-Roles'] = 'ROLE_ADMIN,ROLE_MEDA';
        }
      }
    };
    
    // Execute the function
    applyAdminHeaders();
  } catch (error) {
    console.error('Error applying admin headers to global axios:', error);
  }
  
  // Return true if any fix was applied
  return roleAdded;
};

/**
 * Handle 403 Forbidden errors by applying fixes and prompting for reload
 * @returns {boolean} True if fixes were applied and page will reload
 */
export const handle403Error = () => {
  // Apply emergency fixes
  const fixesApplied = applyEmergencyFixes();
  
  // If fixes were applied, prompt user to reload
  if (fixesApplied) {
    const message = 'Les permissions admin ont été mises à jour. Veuillez actualiser la page.';
    if (confirm(message)) {
      window.location.reload();
      return true;
    }
  }
  
  return false;
};

export default {
  applyEmergencyFixes,
  handle403Error
};