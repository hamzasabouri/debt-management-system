import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  FormControl,
  Grid,
  IconButton,
  InputAdornment,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  Search as SearchIcon
} from '@mui/icons-material';
import { getAllCommissions, createCommission, updateCommission, deleteCommission } from '../../../services/dette-interieur/commissionsService';
import { setAuthToken } from '../../../services/dette-interieur/commissionsService';

const CommissionList = () => {
  const [commissions, setCommissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentCommission, setCurrentCommission] = useState({
    id: null,
    nom: '',
    name: '',
    description: '',
    desc: '',
    dateCreation: '',
    date_creation: '',
    dateCreated: '',
    createdDate: '',
    creationDate: '',
    statut: 'ACTIVE',
    status: '',
    membres: '',
    members: ''
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [commissionToDelete, setCommissionToDelete] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      fetchCommissions();
    }
  }, []);

  const fetchCommissions = async () => {
    try {
      setLoading(true);
      const data = await getAllCommissions();
      // Ensure data is an array
      const commissionsArray = Array.isArray(data) ? data : [];
      setCommissions(commissionsArray);
      setError(null);
    } catch (err) {
      setError('Failed to fetch commissions: ' + err.message);
      console.error('Error fetching commissions:', err);
      // Set empty array on error to prevent filter issues
      setCommissions([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setCurrentCommission({
      id: null,
      typeCommission: '',
      ordrePaiementId: null,
      lettreReglementId: null,
      avisDebitId: null,
      montant: 0,
      statut: 'EN_ATTENTE',
      numeroReference: '',
      description: '',
      commentaire: '',
      createdAt: new Date().toISOString().split('T')[0],
      datePaiement: null
    });
    setOpenDialog(true);
  };

  const handleOpenEditDialog = (commission) => {
    setDialogMode('edit');
    
    // Format date for date input field (YYYY-MM-DD)
    const formatDateForInput = (dateString) => {
      if (!dateString) return '';
      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '';
        return date.toISOString().split('T')[0];
      } catch (error) {
        return '';
      }
    };
    
    setCurrentCommission({
      id: commission.id,
      typeCommission: commission.typeCommission || commission.nom || '',
      ordrePaiementId: commission.ordrePaiementId || null,
      lettreReglementId: commission.lettreReglementId || null,
      avisDebitId: commission.avisDebitId || null,
      montant: commission.montant || 0,
      statut: commission.statut || 'EN_ATTENTE',
      numeroReference: commission.numeroReference || '',
      description: commission.description || '',
      commentaire: commission.commentaire || '',
      createdAt: formatDateForInput(commission.createdAt),
      datePaiement: formatDateForInput(commission.datePaiement)
    });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentCommission(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async () => {
    try {
      if (dialogMode === 'create') {
        await createCommission(currentCommission);
      } else {
        await updateCommission(currentCommission.id, currentCommission);
      }
      handleCloseDialog();
      fetchCommissions();
    } catch (err) {
      setError('Failed to save commission: ' + err.message);
      console.error('Error saving commission:', err);
    }
  };

  const handleOpenDeleteDialog = (commission) => {
    setCommissionToDelete(commission);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setCommissionToDelete(null);
  };

  const handleDelete = async () => {
    try {
      await deleteCommission(commissionToDelete.id);
      handleCloseDeleteDialog();
      fetchCommissions();
    } catch (err) {
      setError('Failed to delete commission: ' + err.message);
      console.error('Error deleting commission:', err);
    }
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'ACTIVE':
        return '#4caf50'; // Green
      case 'PAYEE':
        return '#4caf50'; // Green
      case 'INACTIVE':
        return '#9e9e9e'; // Gray
      case 'SUSPENDUE':
        return '#ff9800'; // Orange
      case 'EN_ATTENTE':
      case 'EN ATTENTE':
        return '#2196f3'; // Blue
      case 'REJETE':
      case 'REJETEE':
        return '#f44336'; // Red
      default:
        return '#9e9e9e'; // Gray for unknown status
    }
  };

  // Format status label for display
  const formatStatus = (status) => {
    if (!status) return 'N/A';
    
    // Replace underscores with spaces and capitalize
    return status.replace(/_/g, ' ');
  };

  // Additional safety check to ensure we have an array
  const safeCommissions = Array.isArray(commissions) ? commissions : [];
  
  // Ensure commissions is always an array before filtering
  const filteredCommissions = safeCommissions.filter(commission =>
    commission && 
    (
      searchTerm === '' || // Show all if no search term
      (commission.typeCommission && commission.typeCommission.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (commission.nom && commission.nom.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (commission.description && commission.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (commission.statut && commission.statut.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (commission.numeroReference && commission.numeroReference.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (commission.commentaire && commission.commentaire.toLowerCase().includes(searchTerm.toLowerCase())) ||
      // Check date fields
      (commission.createdAt && String(commission.createdAt).toLowerCase().includes(searchTerm.toLowerCase()))
    )
  );

  // Helper function to safely get property values with fallbacks for different naming conventions
  const getCommissionProperty = (commission, primaryProp, fallbackProps = []) => {
    // Try primary property first
    if (commission && commission[primaryProp] !== undefined && commission[primaryProp] !== null) {
      return commission[primaryProp];
    }
    
    // Try fallback properties
    for (const prop of fallbackProps) {
      if (commission && commission[prop] !== undefined && commission[prop] !== null) {
        return commission[prop];
      }
    }
    
    // Return N/A if no property found
    return 'N/A';
  };

  // Format date function to handle different date formats and ensure proper display
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    
    // Try to convert the date string to a Date object
    try {
      // Handle ISO format or other common formats
      const date = new Date(dateString);
      
      // Check if date is valid
      if (isNaN(date.getTime())) {
        return dateString; // Return original if parsing fails
      }
      
      // Format the date in a localized way - day/month/year
      return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch (error) {
      console.error('Error formatting date:', error);
      return dateString; // Return original string if error
    }
  };

  // Format currency function
  const formatCurrency = (amount) => {
    if (amount === null || amount === undefined) return 'N/A';
    
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'XOF',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Additional safety check
  if (!Array.isArray(commissions)) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <Typography color="error">Data format error. Please refresh the page.</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#333', fontWeight: 'bold' }}>
            Gestion des Commissions
          </Typography>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreateDialog}
            sx={{ 
              bgcolor: '#FF6B35',
              '&:hover': {
                bgcolor: '#e55a2b'
              },
              borderRadius: 2,
              boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)'
            }}
          >
            Nouvelle Commission
          </Button>
        </Grid>
      </Grid>

      {error && (
        <Box sx={{ mb: 3 }}>
          <Paper sx={{ p: 3, bgcolor: '#ffebee', borderRadius: 2, border: '1px solid #e0e0e0' }}>
            <Typography color="error" variant="h6">Erreur: API request failed</Typography>
            <Typography color="error">{error}</Typography>
            <Button 
              variant="outlined" 
              color="primary" 
              sx={{ mt: 2, borderRadius: 2 }}
              onClick={() => fetchCommissions()}
            >
              Réessayer
            </Button>
          </Paper>
        </Box>
      )}

      <Paper sx={{ p: 2, mb: 3, borderRadius: 2, border: '1px solid #e0e0e0' }}>
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Rechercher par nom, statut ou membres..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon sx={{ color: '#FF6B35' }} />
              </InputAdornment>
            ),
            sx: { borderRadius: 2 }
          }}
          sx={{ borderRadius: 2 }}
        />
      </Paper>

      <TableContainer 
        component={Paper} 
        sx={{ 
          borderRadius: 2, 
          border: '1px solid #e0e0e0',
          boxShadow: '0 2px 10px rgba(0,0,0,0.05)'
        }}
      >
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Nom</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Description</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Création</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Référence</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredCommissions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography variant="body1" sx={{ py: 3 }}>
                    {searchTerm ? 'Aucune commission ne correspond à votre recherche.' : 'Aucune commission disponible.'}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredCommissions.map((commission) => (
                <TableRow 
                  key={commission.id}
                  sx={{
                    '&:nth-of-type(odd)': {
                      bgcolor: 'rgba(0, 0, 0, 0.02)'
                    },
                    '&:hover': {
                      bgcolor: 'rgba(255, 107, 53, 0.05)',
                      transform: 'scale(1.01)',
                      transition: 'all 0.2s ease'
                    }
                  }}
                >
                  <TableCell>{getCommissionProperty(commission, 'nom', ['typeCommission', 'name', 'commission_name', 'commissionName'])}</TableCell>
                  <TableCell>{getCommissionProperty(commission, 'description', ['desc', 'commission_description']) || 'N/A'}</TableCell>
                  <TableCell>{formatDate(getCommissionProperty(commission, 'dateCreation', ['createdAt', 'created_at', 'dateCreated', 'creationDate']))}</TableCell>
                  <TableCell>{getCommissionProperty(commission, 'numeroReference', ['numero_reference', 'reference']) || 'N/A'}</TableCell>
                  <TableCell>{formatCurrency(getCommissionProperty(commission, 'montant', ['amount']))}</TableCell>
                  <TableCell>
                    <Box
                      sx={{
                        display: 'inline-block',
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor: getStatusColor(getCommissionProperty(commission, 'statut', ['status'])),
                        color: 'white',
                        fontSize: '0.8rem',
                        fontWeight: 'bold'
                      }}
                    >
                      {formatStatus(getCommissionProperty(commission, 'statut', ['status']))}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenEditDialog(commission)}
                      sx={{ 
                        mr: 1,
                        '&:hover': {
                          bgcolor: 'rgba(255, 107, 53, 0.1)'
                        }
                      }}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenDeleteDialog(commission)}
                      sx={{
                        '&:hover': {
                          bgcolor: 'rgba(244, 67, 54, 0.1)'
                        }
                      }}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
          {dialogMode === 'create' ? 'Nouvelle Commission' : 'Modifier Commission'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations pour créer une nouvelle commission.' 
              : 'Modifiez les informations de la commission.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Type de Commission"
                name="typeCommission"
                value={currentCommission.typeCommission}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Numéro de Référence"
                name="numeroReference"
                value={currentCommission.numeroReference}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Description"
                name="description"
                value={currentCommission.description}
                onChange={handleInputChange}
                multiline
                rows={2}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Commentaire"
                name="commentaire"
                value={currentCommission.commentaire}
                onChange={handleInputChange}
                multiline
                rows={2}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Montant"
                name="montant"
                type="number"
                value={currentCommission.montant}
                onChange={handleInputChange}
                InputProps={{ 
                  startAdornment: <InputAdornment position="start">XOF</InputAdornment>,
                  sx: { borderRadius: 2 } 
                }}
                sx={{ borderRadius: 2 }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Statut</InputLabel>
                <Select
                  name="statut"
                  value={currentCommission.statut}
                  onChange={handleInputChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="EN_ATTENTE">En Attente</MenuItem>
                  <MenuItem value="PAYEE">Payée</MenuItem>
                  <MenuItem value="REJETE">Rejetée</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date de Création"
                name="createdAt"
                type="date"
                value={currentCommission.createdAt}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date de Paiement"
                name="datePaiement"
                type="date"
                value={currentCommission.datePaiement || ''}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseDialog} sx={{ borderRadius: 2 }}>
            Annuler
          </Button>
          <Button 
            onClick={handleSubmit} 
            variant="contained"
            sx={{ 
              bgcolor: '#FF6B35',
              '&:hover': {
                bgcolor: '#e55a2b'
              },
              borderRadius: 2
            }}
          >
            {dialogMode === 'create' ? 'Créer' : 'Modifier'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle sx={{ bgcolor: '#f44336', color: 'white', fontWeight: 'bold' }}>
          Confirmer la suppression
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText>
            Êtes-vous sûr de vouloir supprimer la commission "{commissionToDelete?.nom}" ? 
            Cette action est irréversible.
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseDeleteDialog} sx={{ borderRadius: 2 }}>
            Annuler
          </Button>
          <Button 
            onClick={handleDelete} 
            variant="contained" 
            color="error"
            sx={{ borderRadius: 2 }}
          >
            Supprimer
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CommissionList;