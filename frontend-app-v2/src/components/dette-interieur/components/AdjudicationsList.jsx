import React, { useState, useEffect } from 'react';
import { useAuth } from '../../../context/AuthContext';
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
import { getAllAdjudications, createAdjudication, updateAdjudication, deleteAdjudication } from '../../../services/dette-interieur/adjudicationsService';

const AdjudicationsList = () => {
  const { user, hasRole } = useAuth();
  const [adjudications, setAdjudications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentAdjudication, setCurrentAdjudication] = useState({
    numeroAdjud: '',
    dateAdjud: '',
    montantTotal: '',
    statut: 'EN_COURS'
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [adjudicationToDelete, setAdjudicationToDelete] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    // Remove the manual token setting since it's handled by the base service
    // The base service will automatically add the token and headers
    fetchAdjudications();
  }, []);

  const fetchAdjudications = async () => {
    try {
      setLoading(true);
      setError(null); // Clear any previous errors
      
      const data = await getAllAdjudications();
      
      // Check if data is valid (defensive programming)
      if (!data) {
        setAdjudications([]);
        setError('No data received from server');
      } else if (Array.isArray(data)) {
        setAdjudications(data);
      } else if (data.content && Array.isArray(data.content)) {
        // Handle paginated response
        setAdjudications(data.content);
      } else {
        // Fallback for other response structures
        setAdjudications(Array.isArray(data) ? data : (data.data || []));
      }
    } catch (err) {
      console.error('Error fetching adjudications:', err);
      
      // Provide user-friendly error messages
      if (err.message.includes('Authentication failed')) {
        setError('Authentication error: Please log in again');
      } else if (err.message.includes('permission')) {
        setError('Permission denied: You do not have access to this resource');
      } else {
        setError('Failed to fetch adjudications: ' + err.message);
      }
      
      // Always set empty array to prevent null reference errors
      setAdjudications([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setCurrentAdjudication({
      numeroAdjud: '',
      dateAdjud: '',
      montantTotal: '',
      statut: 'EN_COURS'
    });
    setOpenDialog(true);
  };

  const handleOpenEditDialog = (adjudication) => {
    setDialogMode('edit');
    setCurrentAdjudication({ ...adjudication });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentAdjudication(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async () => {
    try {
      if (dialogMode === 'create') {
        await createAdjudication(currentAdjudication);
      } else {
        await updateAdjudication(currentAdjudication.id, currentAdjudication);
      }
      handleCloseDialog();
      fetchAdjudications();
    } catch (err) {
      setError('Failed to save adjudication: ' + err.message);
      console.error('Error saving adjudication:', err);
    }
  };

  const handleOpenDeleteDialog = (adjudication) => {
    // Check if user has admin or dette_interieur role before allowing delete
    if (!hasRole('admin') && !hasRole('dette_interieur')) {
      setError('Delete operation requires administrator or dette interieur privileges. Only administrators can delete adjudications.');
      return;
    }
    
    setAdjudicationToDelete(adjudication);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setAdjudicationToDelete(null);
  };

  const handleDelete = async () => {
    try {
      await deleteAdjudication(adjudicationToDelete.id);
      handleCloseDeleteDialog();
      fetchAdjudications();
    } catch (err) {
      // Provide a more specific error message for permission issues
      if (err.message.includes('permission') || err.message.includes('Access denied') || err.message.includes('403')) {
        setError('Delete operation requires administrator privileges. Only administrators can delete adjudications.');
      } else {
        setError('Failed to delete adjudication: ' + err.message);
      }
      console.error('Error deleting adjudication:', err);
    }
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'EN_COURS':
        return '#ff9800';
      case 'CLOTUREE':
        return '#4caf50';
      case 'ANNULEE':
        return '#f44336';
      default:
        return '#9e9e9e';
    }
  };

  const filteredAdjudications = Array.isArray(adjudications) 
    ? adjudications.filter(adjudication =>
        adjudication && (
          adjudication.numeroAdjud.toLowerCase().includes(searchTerm.toLowerCase()) ||
          adjudication.statut.toLowerCase().includes(searchTerm.toLowerCase())
        )
      )
    : [];

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#333', fontWeight: 'bold' }}>
            Gestion des Adjudications
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
            Nouvelle Adjudication
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
              onClick={() => fetchAdjudications()}
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
          placeholder="Rechercher par numéro d'adjudication ou statut..."
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>N° Adjudication</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant Total</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredAdjudications.map((adjudication) => (
              <TableRow 
                key={adjudication.id}
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
                <TableCell>{adjudication.numeroAdjud}</TableCell>
                <TableCell>{adjudication.dateAdjud}</TableCell>
                <TableCell>{parseFloat(adjudication.montantTotal).toLocaleString()} MAD</TableCell>
                <TableCell>
                  <Box
                    sx={{
                      display: 'inline-block',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      bgcolor: getStatusColor(adjudication.statut),
                      color: 'white',
                      fontSize: '0.8rem',
                      fontWeight: 'bold'
                    }}
                  >
                    {adjudication.statut}
                  </Box>
                </TableCell>
                <TableCell>
                  <IconButton 
                    size="small" 
                    onClick={() => handleOpenEditDialog(adjudication)}
                    sx={{ 
                      mr: 1,
                      '&:hover': {
                        bgcolor: 'rgba(255, 107, 53, 0.1)'
                      }
                    }}
                  >
                    <EditIcon />
                  </IconButton>
                  {/* Only show delete button for users with admin or dette_interieur role */}
                  {(hasRole('admin') || hasRole('dette_interieur')) && (
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenDeleteDialog(adjudication)}
                      sx={{
                        '&:hover': {
                          bgcolor: 'rgba(244, 67, 54, 0.1)'
                        }
                      }}
                    >
                      <DeleteIcon />
                    </IconButton>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
          {dialogMode === 'create' ? 'Nouvelle Adjudication' : 'Modifier Adjudication'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations pour créer une nouvelle adjudication.' 
              : 'Modifiez les informations de l\'adjudication.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Numéro d'Adjudication"
                name="numeroAdjud"
                value={currentAdjudication.numeroAdjud}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date d'Adjudication"
                name="dateAdjud"
                type="date"
                value={currentAdjudication.dateAdjud}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Montant Total"
                name="montantTotal"
                type="number"
                value={currentAdjudication.montantTotal}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Statut</InputLabel>
                <Select
                  name="statut"
                  value={currentAdjudication.statut}
                  onChange={handleInputChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="EN_COURS">En Cours</MenuItem>
                  <MenuItem value="CLOTUREE">Clôturée</MenuItem>
                  <MenuItem value="ANNULEE">Annulée</MenuItem>
                </Select>
              </FormControl>
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
            Êtes-vous sûr de vouloir supprimer l'adjudication "{adjudicationToDelete?.numeroAdjud}" ? 
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

export default AdjudicationsList;