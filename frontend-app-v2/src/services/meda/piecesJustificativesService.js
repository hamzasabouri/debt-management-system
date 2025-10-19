import { handle403Error } from '../../utils/authUtils';
import { createAuthenticatedApi } from '../baseService';

// Create axios instance with base URL for meda service using the authenticated API creator
const medaApi = createAuthenticatedApi('/api/meda');

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
 * Get all supporting documents
 * @returns {Promise} Promise object represents the document list
 */
export const getAllPiecesJustificatives = async () => {
  try {
    // Make sure we're using the correct URL path
    const response = await medaApi.get('/pieces-justificatives');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get supporting document by ID
 * @param {number} id - Document ID
 * @returns {Promise} Promise object represents the document
 */
export const getPieceJustificativeById = async (id) => {
  try {
    const response = await medaApi.get(`/pieces-justificatives/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Create a new supporting document
 * @param {Object} pieceData - Document data
 * @returns {Promise} Promise object represents the created document
 */
export const createPieceJustificative = async (pieceData) => {
  try {
    console.log('Creating piece justificative with data:', pieceData);
    console.log('medaApi defaults:', medaApi.defaults);
    const response = await medaApi.post('/pieces-justificatives', pieceData);
    console.log('Response:', response);
    return response.data;
  } catch (error) {
    console.error('Error creating piece justificative:', error);
    handleError(error);
  }
};

/**
 * Update an existing supporting document
 * @param {number} id - Document ID
 * @param {Object} pieceData - Updated document data
 * @returns {Promise} Promise object represents the updated document
 */
export const updatePieceJustificative = async (id, pieceData) => {
  try {
    console.log('Updating piece justificative with ID:', id, 'and data:', pieceData);
    console.log('medaApi defaults:', medaApi.defaults);
    const response = await medaApi.put(`/pieces-justificatives/${id}`, pieceData);
    console.log('Response:', response);
    return response.data;
  } catch (error) {
    console.error('Error updating piece justificative:', error);
    handleError(error);
  }
};

/**
 * Validate a supporting document
 * @param {number} id - Document ID
 * @param {string} commentaire - Validation comment
 * @returns {Promise} Promise object represents the validated document
 */
export const validatePieceJustificative = async (id, commentaire) => {
  try {
    const response = await medaApi.put(`/pieces-justificatives/${id}/validate`, null, { 
      params: { commentaire } 
    });
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Reject a supporting document
 * @param {number} id - Document ID
 * @param {string} commentaire - Rejection comment
 * @returns {Promise} Promise object represents the rejected document
 */
export const rejectPieceJustificative = async (id, commentaire) => {
  try {
    const response = await medaApi.put(`/pieces-justificatives/${id}/reject`, null, { 
      params: { commentaire } 
    });
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Delete a supporting document
 * @param {number} id - Document ID
 * @returns {Promise} Promise object represents the deletion result
 */
export const deletePieceJustificative = async (id) => {
  try {
    const response = await medaApi.delete(`/pieces-justificatives/${id}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get supporting documents by project ID
 * @param {number} projetId - Project ID
 * @returns {Promise} Promise object represents the document list for the project
 */
export const getPiecesJustificativesByProjet = async (projetId) => {
  try {
    const response = await medaApi.get(`/pieces-justificatives/project/${projetId}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Calculate available funds for a project
 * @param {number} projetId - Project ID
 * @returns {Promise} Promise object represents the available funds
 */
export const calculateAvailableFunds = async (projetId) => {
  try {
    const response = await medaApi.get(`/pieces-justificatives/project/${projetId}/available-funds`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get supporting documents by type
 * @param {string} typePiece - Document type
 * @returns {Promise} Promise object represents the document list by type
 */
export const getPiecesJustificativesByType = async (typePiece) => {
  try {
    const response = await medaApi.get(`/pieces-justificatives/type/${typePiece}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get supporting documents by status
 * @param {string} statut - Document status
 * @returns {Promise} Promise object represents the document list by status
 */
export const getPiecesJustificativesByStatus = async (statut) => {
  try {
    const response = await medaApi.get(`/pieces-justificatives/status/${statut}`);
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get supporting documents pending validation
 * @returns {Promise} Promise object represents the document list pending validation
 */
export const getPiecesJustificativesPendingValidation = async () => {
  try {
    const response = await medaApi.get('/pieces-justificatives/pending-validation');
    return response.data;
  } catch (error) {
    handleError(error);
  }
};

/**
 * Get rejected supporting documents
 * @returns {Promise} Promise object represents the rejected document list
 */
export const getRejectedPiecesJustificatives = async () => {
  try {
    const response = await medaApi.get('/pieces-justificatives/rejected');
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
  console.error('API Error:', error);
  
  if (error.response) {
    // Server responded with error status
    const status = error.response.status;
    console.log('Error response details:', {
      status,
      statusText: error.response.statusText,
      data: error.response.data,
      headers: error.response.headers
    });
    
    if (status === 401) {
      throw new Error('Session expirée. Veuillez vous reconnecter.');
    } else if (status === 403) {
      // Try to handle 403 error with emergency fixes
      if (handle403Error()) {
        // If handler returns true, page will reload, so just return
        return;
      }
      
      // Don't redirect, just throw the error for the component to handle
      throw new Error('Accès non autorisé. Erreur 403: Le serveur a refusé votre accès malgré les en-têtes d\'autorisation. Veuillez vérifier que votre compte possède le rôle MEDA.');
    } else if (status === 404) {
      throw new Error('Ressource non trouvée.');
    } else {
      throw new Error(error.response.data?.message || `Erreur API: ${status} - ${error.response.statusText}`);
    }
  } else if (error.request) {
    // Request was made but no response received
    if (error.code === 'ECONNABORTED') {
      throw new Error('La requête a expiré. Le service MEDA ne répond pas.');
    }
    throw new Error('Network error. Please check your connection.');
  } else {
    // Something else happened
    throw new Error(`An unexpected error occurred: ${error.message}`);
  }
};

export default {
  setAuthToken,
  getAllPiecesJustificatives,
  getPieceJustificativeById,
  createPieceJustificative,
  updatePieceJustificative,
  validatePieceJustificative,
  rejectPieceJustificative,
  deletePieceJustificative,
  getPiecesJustificativesByProjet,
  calculateAvailableFunds,
  getPiecesJustificativesByType,
  getPiecesJustificativesByStatus,
  getPiecesJustificativesPendingValidation,
  getRejectedPiecesJustificatives,
};