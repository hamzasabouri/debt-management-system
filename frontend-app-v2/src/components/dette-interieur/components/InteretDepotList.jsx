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
import { getAllInteretDepots, createInteretDepot, updateInteretDepot, deleteInteretDepot } from '../../../services/dette-interieur/interetDepotService';
import { setAuthToken } from '../../../services/dette-interieur/interetDepotService';

const InteretDepotList = () => {
  const [interetDepots, setInteretDepots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentInteretDepot, setCurrentInteretDepot] = useState({
    numeroCompte: '',
    dateCalcul: '',
    montantInteret: '',
    tauxInteret: '',
    periodeDebut: '',
    periodeFin: '',
    statut: 'CALCULE',
    commentaire: ''
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [interetDepotToDelete, setInteretDepotToDelete] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      fetchInteretDepots();
    }
  }, []);

  const fetchInteretDepots = async () => {
    try {
      setLoading(true);
      const data = await getAllInteretDepots();
      // Ensure data is an array
      const interetDepotsArray = Array.isArray(data) ? data : [];
      setInteretDepots(interetDepotsArray);
      setError(null);
    } catch (err) {
      setError('Failed to fetch deposit interests: ' + err.message);
      console.error('Error fetching deposit interests:', err);
      // Set empty array on error to prevent filter issues
      setInteretDepots([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setCurrentInteretDepot({
      numeroCompte: '',
      dateCalcul: '',
      montantInteret: '',
      tauxInteret: '',
      periodeDebut: '',
      periodeFin: '',
      statut: 'CALCULE',
      commentaire: ''
    });
    setOpenDialog(true);
  };

  const handleOpenEditDialog = (interetDepot) => {
    setDialogMode('edit');
    setCurrentInteretDepot({ ...interetDepot });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentInteretDepot(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async () => {
    try {
      if (dialogMode === 'create') {
        await createInteretDepot(currentInteretDepot);
      } else {
        await updateInteretDepot(currentInteretDepot.id, currentInteretDepot);
      }
      handleCloseDialog();
      fetchInteretDepots();
    } catch (err) {
      setError('Failed to save deposit interest: ' + err.message);
      console.error('Error saving deposit interest:', err);
    }
  };

  const handleOpenDeleteDialog = (interetDepot) => {
    setInteretDepotToDelete(interetDepot);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setInteretDepotToDelete(null);
  };

  const handleDelete = async () => {
    try {
      await deleteInteretDepot(interetDepotToDelete.id);
      handleCloseDeleteDialog();
      fetchInteretDepots();
    } catch (err) {
      setError('Failed to delete deposit interest: ' + err.message);
      console.error('Error deleting deposit interest:', err);
    }
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'CALCULE':
        return '#2196f3';
      case 'VALIDE':
        return '#4caf50';
      case 'REJETE':
        return '#f44336';
      case 'EN_ATTENTE':
        return '#ff9800';
      default:
        return '#9e9e9e';
    }
  };

  // Additional safety check to ensure we have an array
  const safeInteretDepots = Array.isArray(interetDepots) ? interetDepots : [];
  
  // Ensure interetDepots is always an array before filtering
  const filteredInteretDepots = safeInteretDepots.filter(interetDepot =>
    interetDepot && 
    (
      (interetDepot.numeroCompte && interetDepot.numeroCompte.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (interetDepot.statut && interetDepot.statut.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (interetDepot.commentaire && interetDepot.commentaire.toLowerCase().includes(searchTerm.toLowerCase()))
    )
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Additional safety check
  if (!Array.isArray(interetDepots)) {
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
            Gestion des Intérêts de Dépôt
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
            Nouvel Intérêt de Dépôt
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
              onClick={() => fetchInteretDepots()}
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
          placeholder="Rechercher par numéro de compte, statut ou commentaire..."
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>N° Compte</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Calcul</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant Intérêt</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Taux Intérêt</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Période</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredInteretDepots.map((interetDepot) => (
              <TableRow 
                key={interetDepot.id}
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
                <TableCell>{interetDepot.numeroCompte}</TableCell>
                <TableCell>{interetDepot.dateCalcul}</TableCell>
                <TableCell>{parseFloat(interetDepot.montantInteret).toLocaleString()} MAD</TableCell>
                <TableCell>{interetDepot.tauxInteret}%</TableCell>
                <TableCell>{interetDepot.periodeDebut} à {interetDepot.periodeFin}</TableCell>
                <TableCell>
                  <Box
                    sx={{
                      display: 'inline-block',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      bgcolor: getStatusColor(interetDepot.statut),
                      color: 'white',
                      fontSize: '0.8rem',
                      fontWeight: 'bold'
                    }}
                  >
                    {interetDepot.statut}
                  </Box>
                </TableCell>
                <TableCell>
                  <IconButton 
                    size="small" 
                    onClick={() => handleOpenEditDialog(interetDepot)}
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
                    onClick={() => handleOpenDeleteDialog(interetDepot)}
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
          {dialogMode === 'create' ? 'Nouvel Intérêt de Dépôt' : 'Modifier Intérêt de Dépôt'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations pour créer un nouvel intérêt de dépôt.' 
              : 'Modifiez les informations de l\'intérêt de dépôt.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Numéro de Compte"
                name="numeroCompte"
                value={currentInteretDepot.numeroCompte}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date de Calcul"
                name="dateCalcul"
                type="date"
                value={currentInteretDepot.dateCalcul}
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
                label="Montant Intérêt"
                name="montantInteret"
                type="number"
                value={currentInteretDepot.montantInteret}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Taux Intérêt (%)"
                name="tauxInteret"
                type="number"
                value={currentInteretDepot.tauxInteret}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Période Début"
                name="periodeDebut"
                type="date"
                value={currentInteretDepot.periodeDebut}
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
                label="Période Fin"
                name="periodeFin"
                type="date"
                value={currentInteretDepot.periodeFin}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
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
                  value={currentInteretDepot.statut}
                  onChange={handleInputChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="CALCULE">Calculé</MenuItem>
                  <MenuItem value="VALIDE">Validé</MenuItem>
                  <MenuItem value="REJETE">Rejeté</MenuItem>
                  <MenuItem value="EN_ATTENTE">En Attente</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Commentaire"
                name="commentaire"
                value={currentInteretDepot.commentaire}
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
            Êtes-vous sûr de vouloir supprimer l'intérêt de dépôt "{interetDepotToDelete?.numeroCompte}" ? 
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

export default InteretDepotList;