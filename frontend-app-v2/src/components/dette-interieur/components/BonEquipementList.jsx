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
import { getAllBonEquipements, createBonEquipement, updateBonEquipement, deleteBonEquipement } from '../../../services/dette-interieur/bonEquipementService';
import { useAuth } from '../../../context/AuthContext';

const BonEquipementList = () => {
  const { user, hasRole } = useAuth();
  const [bonEquipements, setBonEquipements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentBonEquipement, setCurrentBonEquipement] = useState({
    numeroBon: '',
    dateSouscription: '',
    montant: '',
    statut: 'SOUSCRIT',
    dateEcheance: '',
    tauxInteret: '',
    souscripteur: '',
    commentaire: ''
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [bonEquipementToDelete, setBonEquipementToDelete] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchBonEquipements();
  }, []);

  const fetchBonEquipements = async () => {
    try {
      setLoading(true);
      const data = await getAllBonEquipements();
      // Ensure data is an array
      const bonArray = Array.isArray(data) ? data : [];
      setBonEquipements(bonArray);
      setError(null);
    } catch (err) {
      setError('Failed to fetch equipment bonds: ' + err.message);
      console.error('Error fetching equipment bonds:', err);
      // Set empty array on error to prevent filter issues
      setBonEquipements([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setCurrentBonEquipement({
      numeroBon: '',
      dateSouscription: '',
      montant: '',
      statut: 'SOUSCRIT',
      dateEcheance: '',
      tauxInteret: '',
      souscripteur: '',
      commentaire: ''
    });
    setOpenDialog(true);
  };

  const handleOpenEditDialog = (bonEquipement) => {
    setDialogMode('edit');
    setCurrentBonEquipement({ ...bonEquipement });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentBonEquipement(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async () => {
    try {
      if (dialogMode === 'create') {
        await createBonEquipement(currentBonEquipement);
      } else {
        await updateBonEquipement(currentBonEquipement.id, currentBonEquipement);
      }
      handleCloseDialog();
      fetchBonEquipements();
    } catch (err) {
      setError('Failed to save equipment bond: ' + err.message);
      console.error('Error saving equipment bond:', err);
    }
  };

  const handleOpenDeleteDialog = (bonEquipement) => {
    // Check if user has admin role before allowing delete
    if (!hasRole('admin')) {
      setError('Delete operation requires administrator privileges. Only administrators can delete equipment bonds.');
      return;
    }
    
    setBonEquipementToDelete(bonEquipement);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setBonEquipementToDelete(null);
  };

  const handleDelete = async () => {
    try {
      await deleteBonEquipement(bonEquipementToDelete.id);
      handleCloseDeleteDialog();
      fetchBonEquipements();
    } catch (err) {
      // Provide a more specific error message for permission issues
      if (err.message.includes('permission') || err.message.includes('Access denied') || err.message.includes('403')) {
        setError('Delete operation requires administrator privileges. Only administrators can delete equipment bonds.');
      } else {
        setError('Failed to delete equipment bond: ' + err.message);
      }
      console.error('Error deleting equipment bond:', err);
    }
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'SOUSCRIT':
        return '#2196f3';
      case 'REMBOURSE':
        return '#4caf50';
      case 'REJETE':
        return '#f44336';
      case 'EN_COURS':
        return '#ff9800';
      case 'EXPIRE':
        return '#9e9e9e';
      default:
        return '#9e9e9e';
    }
  };

  // Additional safety check to ensure we have an array
  const safeBonEquipements = Array.isArray(bonEquipements) ? bonEquipements : [];
  
  // Ensure bonEquipements is always an array before filtering
  const filteredBonEquipements = safeBonEquipements.filter(bonEquipement =>
    bonEquipement && 
    (bonEquipement.numeroBon?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    bonEquipement.statut?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (bonEquipement.souscripteur && bonEquipement.souscripteur.toLowerCase().includes(searchTerm.toLowerCase())))
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Additional safety check
  if (!Array.isArray(bonEquipements)) {
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
            Gestion des Bons d'Équipement
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
            Nouveau Bon d'Équipement
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
              onClick={() => fetchBonEquipements()}
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
          placeholder="Rechercher par numéro de bon, statut ou souscripteur..."
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>N° Bon</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date Souscription</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Souscripteur</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredBonEquipements.map((bonEquipement) => (
              <TableRow 
                key={bonEquipement.id}
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
                <TableCell>{bonEquipement.numeroBon}</TableCell>
                <TableCell>{bonEquipement.dateSouscription}</TableCell>
                <TableCell>{parseFloat(bonEquipement.montant).toLocaleString()} MAD</TableCell>
                <TableCell>{bonEquipement.souscripteur || 'N/A'}</TableCell>
                <TableCell>
                  <Box
                    sx={{
                      display: 'inline-block',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      bgcolor: getStatusColor(bonEquipement.statut),
                      color: 'white',
                      fontSize: '0.8rem',
                      fontWeight: 'bold'
                    }}
                  >
                    {bonEquipement.statut}
                  </Box>
                </TableCell>
                <TableCell>
                  <IconButton 
                    size="small" 
                    onClick={() => handleOpenEditDialog(bonEquipement)}
                    sx={{ 
                      mr: 1,
                      '&:hover': {
                        bgcolor: 'rgba(255, 107, 53, 0.1)'
                      }
                    }}
                  >
                    <EditIcon />
                  </IconButton>
                  {/* Only show delete button for users with admin role */}
                  {hasRole('admin') && (
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenDeleteDialog(bonEquipement)}
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
          {dialogMode === 'create' ? 'Nouveau Bon d\'Équipement' : 'Modifier Bon d\'Équipement'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations pour créer un nouveau bon d\'équipement.' 
              : 'Modifiez les informations du bon d\'équipement.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Numéro de Bon"
                name="numeroBon"
                value={currentBonEquipement.numeroBon}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date de Souscription"
                name="dateSouscription"
                type="date"
                value={currentBonEquipement.dateSouscription}
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
                label="Montant"
                name="montant"
                type="number"
                value={currentBonEquipement.montant}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date d'Échéance"
                name="dateEcheance"
                type="date"
                value={currentBonEquipement.dateEcheance}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Taux d'Intérêt"
                name="tauxInteret"
                type="number"
                value={currentBonEquipement.tauxInteret}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Souscripteur"
                name="souscripteur"
                value={currentBonEquipement.souscripteur}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Statut</InputLabel>
                <Select
                  name="statut"
                  value={currentBonEquipement.statut}
                  onChange={handleInputChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="SOUSCRIT">Souscrit</MenuItem>
                  <MenuItem value="REMBOURSE">Remboursé</MenuItem>
                  <MenuItem value="REJETE">Rejeté</MenuItem>
                  <MenuItem value="EN_COURS">En Cours</MenuItem>
                  <MenuItem value="EXPIRE">Expiré</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Commentaire"
                name="commentaire"
                value={currentBonEquipement.commentaire}
                onChange={handleInputChange}
                multiline
                rows={3}
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
            Êtes-vous sûr de vouloir supprimer le bon d'équipement "{bonEquipementToDelete?.numeroBon}" ? 
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

export default BonEquipementList;