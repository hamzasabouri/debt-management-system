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
  Grid,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  Search as SearchIcon
} from '@mui/icons-material';
import { getAllAvisBonEquipements, createAvisBonEquipement, updateAvisBonEquipement, deleteAvisBonEquipement } from '../../../services/dette-interieur/avisBonEquipementService';
import { setAuthToken } from '../../../services/dette-interieur/avisBonEquipementService';

const AvisBonEquipementList = () => {
  const [avisBonEquipements, setAvisBonEquipements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentAvisBonEquipement, setCurrentAvisBonEquipement] = useState({
    bonEquipementId: '',
    typeAvis: 'CREDIT',
    numeroAvis: '',
    dateReception: '',
    montant: '',
    emetteur: '',
    statut: 'PRIS_EN_CHARGE',
    motifRejet: '',
    commentaire: ''
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [avisBonEquipementToDelete, setAvisBonEquipementToDelete] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      // Only fetch data after setting the token
      fetchAvisBonEquipements();
    } else {
      setError('Authentication required. Please log in.');
      setLoading(false);
    }
  }, []);

  const fetchAvisBonEquipements = async () => {
    try {
      setLoading(true);
      const data = await getAllAvisBonEquipements();
      // Ensure data is an array
      const avisArray = Array.isArray(data) ? data : [];
      setAvisBonEquipements(avisArray);
      setError(null);
    } catch (err) {
      setError('Failed to fetch equipment bond notices: ' + err.message);
      console.error('Error fetching equipment bond notices:', err);
      // Set empty array on error to prevent filter issues
      setAvisBonEquipements([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setCurrentAvisBonEquipement({
      bonEquipementId: '',
      typeAvis: 'CREDIT',
      numeroAvis: '',
      dateReception: '',
      montant: '',
      emetteur: '',
      statut: 'PRIS_EN_CHARGE',
      motifRejet: '',
      commentaire: ''
    });
    setOpenDialog(true);
  };

  const handleOpenEditDialog = (avisBonEquipement) => {
    setDialogMode('edit');
    setCurrentAvisBonEquipement({ ...avisBonEquipement });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentAvisBonEquipement(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async () => {
    try {
      if (dialogMode === 'create') {
        await createAvisBonEquipement(currentAvisBonEquipement);
      } else {
        await updateAvisBonEquipement(currentAvisBonEquipement.id, currentAvisBonEquipement);
      }
      handleCloseDialog();
      fetchAvisBonEquipements();
    } catch (err) {
      setError('Failed to save equipment bond notice: ' + err.message);
      console.error('Error saving equipment bond notice:', err);
    }
  };

  const handleOpenDeleteDialog = (avisBonEquipement) => {
    setAvisBonEquipementToDelete(avisBonEquipement);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setAvisBonEquipementToDelete(null);
  };

  const handleDelete = async () => {
    try {
      await deleteAvisBonEquipement(avisBonEquipementToDelete.id);
      handleCloseDeleteDialog();
      fetchAvisBonEquipements();
    } catch (err) {
      setError('Failed to delete equipment bond notice: ' + err.message);
      console.error('Error deleting equipment bond notice:', err);
    }
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'PRIS_EN_CHARGE':
        return '#ff9800';
      case 'COMPTABILISE':
        return '#4caf50';
      case 'REJETE':
        return '#f44336';
      case 'EN_ATTENTE':
        return '#2196f3';
      default:
        return '#9e9e9e';
    }
  };

  const getTypeAvisColor = (typeAvis) => {
    switch (typeAvis) {
      case 'CREDIT':
        return '#4caf50';
      case 'DEBIT':
        return '#ff9800';
      case 'REJET':
        return '#f44336';
      default:
        return '#9e9e9e';
    }
  };

  // Additional safety check to ensure we have an array
  const safeAvisBonEquipements = Array.isArray(avisBonEquipements) ? avisBonEquipements : [];
  
  // Ensure avisBonEquipements is always an array before filtering
  const filteredAvisBonEquipements = safeAvisBonEquipements.filter(avisBonEquipement =>
    avisBonEquipement && 
    (avisBonEquipement.numeroAvis?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    avisBonEquipement.statut?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    avisBonEquipement.typeAvis?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (avisBonEquipement.emetteur && avisBonEquipement.emetteur.toLowerCase().includes(searchTerm.toLowerCase())))
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Additional safety check
  if (!Array.isArray(avisBonEquipements)) {
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
            Gestion des Avis de Bons d'Équipement
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
            Nouvel Avis de Bon d'Équipement
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
              onClick={() => fetchAvisBonEquipements()}
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
          placeholder="Rechercher par numéro d'avis, statut, type ou émetteur..."
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>N° Avis</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Type</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date Réception</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Émetteur</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredAvisBonEquipements.map((avisBonEquipement) => (
              <TableRow 
                key={avisBonEquipement.id}
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
                <TableCell>{avisBonEquipement.numeroAvis}</TableCell>
                <TableCell>
                  <Box
                    sx={{
                      display: 'inline-block',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      bgcolor: getTypeAvisColor(avisBonEquipement.typeAvis),
                      color: 'white',
                      fontSize: '0.8rem',
                      fontWeight: 'bold'
                    }}
                  >
                    {avisBonEquipement.typeAvis}
                  </Box>
                </TableCell>
                <TableCell>{avisBonEquipement.dateReception}</TableCell>
                <TableCell>{parseFloat(avisBonEquipement.montant).toLocaleString()} MAD</TableCell>
                <TableCell>{avisBonEquipement.emetteur || 'N/A'}</TableCell>
                <TableCell>
                  <Box
                    sx={{
                      display: 'inline-block',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      bgcolor: getStatusColor(avisBonEquipement.statut),
                      color: 'white',
                      fontSize: '0.8rem',
                      fontWeight: 'bold'
                    }}
                  >
                    {avisBonEquipement.statut}
                  </Box>
                </TableCell>
                <TableCell>
                  <IconButton 
                    size="small" 
                    onClick={() => handleOpenEditDialog(avisBonEquipement)}
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
                    onClick={() => handleOpenDeleteDialog(avisBonEquipement)}
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
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
          {dialogMode === 'create' ? 'Nouvel Avis de Bon d\'Équipement' : 'Modifier Avis de Bon d\'Équipement'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations pour créer un nouvel avis de bon d\'équipement.' 
              : 'Modifiez les informations de l\'avis de bon d\'équipement.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="ID du Bon d'Équipement"
                name="bonEquipementId"
                type="number"
                value={currentAvisBonEquipement.bonEquipementId}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Type d'Avis</InputLabel>
                <Select
                  name="typeAvis"
                  value={currentAvisBonEquipement.typeAvis}
                  onChange={handleInputChange}
                  label="Type d'Avis"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="CREDIT">Crédit</MenuItem>
                  <MenuItem value="DEBIT">Débit</MenuItem>
                  <MenuItem value="REJET">Rejet</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Numéro d'Avis"
                name="numeroAvis"
                value={currentAvisBonEquipement.numeroAvis}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date de Réception"
                name="dateReception"
                type="date"
                value={currentAvisBonEquipement.dateReception}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
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
                value={currentAvisBonEquipement.montant}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Émetteur"
                name="emetteur"
                value={currentAvisBonEquipement.emetteur}
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
                  value={currentAvisBonEquipement.statut}
                  onChange={handleInputChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="PRIS_EN_CHARGE">Pris en Charge</MenuItem>
                  <MenuItem value="COMPTABILISE">Comptabilisé</MenuItem>
                  <MenuItem value="REJETE">Rejeté</MenuItem>
                  <MenuItem value="EN_ATTENTE">En Attente</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Motif de Rejet"
                name="motifRejet"
                value={currentAvisBonEquipement.motifRejet}
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
                value={currentAvisBonEquipement.commentaire}
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
            Êtes-vous sûr de vouloir supprimer l'avis de bon d'équipement "{avisBonEquipementToDelete?.numeroAvis}" ? 
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

export default AvisBonEquipementList;