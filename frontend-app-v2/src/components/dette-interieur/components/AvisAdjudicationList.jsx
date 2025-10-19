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
import { getAllAvisAdjudications, createAvisAdjudication, updateAvisAdjudication, deleteAvisAdjudication } from '../../../services/dette-interieur/avisAdjudicationService';

const AvisAdjudicationList = () => {
  const [avisAdjudications, setAvisAdjudications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentAvisAdjudication, setCurrentAvisAdjudication] = useState({
    adjudicationId: '',
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
  const [avisAdjudicationToDelete, setAvisAdjudicationToDelete] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    // Remove the manual token setting since it's handled by the base service
    // The base service will automatically add the token and headers
    fetchAvisAdjudications();
  }, []);

  const fetchAvisAdjudications = async () => {
    try {
      setLoading(true);
      const data = await getAllAvisAdjudications();
      
      // More robust handling of paginated response
      let avisArray = [];
      if (data && typeof data === 'object') {
        if (Array.isArray(data)) {
          // Direct array response
          avisArray = data;
        } else if (data.content && Array.isArray(data.content)) {
          // Paginated response with content array
          avisArray = data.content;
        }
        // If data is an object but doesn't match expected structures, avisArray remains empty
      }
      
      setAvisAdjudications(avisArray);
      setError(null);
    } catch (err) {
      // Check if the error is related to authentication
      if (err.message.includes('Authentication failed') || err.message.includes('Please log in again')) {
        setError('Authentication error: Please log in again to view auction advices.');
      } else {
        setError('Failed to fetch auction advices: ' + err.message);
      }
      console.error('Error fetching auction advices:', err);
      // Set empty array on error to prevent filter issues
      setAvisAdjudications([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setCurrentAvisAdjudication({
      adjudicationId: '',
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

  const handleOpenEditDialog = (avisAdjudication) => {
    setDialogMode('edit');
    setCurrentAvisAdjudication({ ...avisAdjudication });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentAvisAdjudication(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async () => {
    try {
      if (dialogMode === 'create') {
        await createAvisAdjudication(currentAvisAdjudication);
      } else {
        await updateAvisAdjudication(currentAvisAdjudication.id, currentAvisAdjudication);
      }
      handleCloseDialog();
      fetchAvisAdjudications();
    } catch (err) {
      // Check if the error is related to authentication
      if (err.message.includes('Authentication failed') || err.message.includes('Please log in again')) {
        setError('Authentication error: Please log in again to perform this operation.');
      } else {
        setError('Failed to save auction advice: ' + err.message);
      }
      console.error('Error saving auction advice:', err);
    }
  };

  const handleOpenDeleteDialog = (avisAdjudication) => {
    setAvisAdjudicationToDelete(avisAdjudication);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setAvisAdjudicationToDelete(null);
  };

  const handleDelete = async () => {
    try {
      await deleteAvisAdjudication(avisAdjudicationToDelete.id);
      handleCloseDeleteDialog();
      fetchAvisAdjudications();
    } catch (err) {
      // Check if the error is related to authentication
      if (err.message.includes('Authentication failed') || err.message.includes('Please log in again')) {
        setError('Authentication error: Please log in again to perform this operation.');
      } else {
        setError('Failed to delete auction advice: ' + err.message);
      }
      console.error('Error deleting auction advice:', err);
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

  // Ensure avisAdjudications is always an array before filtering
  const filteredAvisAdjudications = (() => {
    try {
      return Array.isArray(avisAdjudications) 
        ? avisAdjudications.filter(avisAdjudication =>
            avisAdjudication && 
            (avisAdjudication.numeroAvis?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            avisAdjudication.statut?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            avisAdjudication.typeAvis?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (avisAdjudication.emetteur && avisAdjudication.emetteur.toLowerCase().includes(searchTerm.toLowerCase())))
          )
        : [];
    } catch (error) {
      console.error('Error filtering avisAdjudications:', error);
      return [];
    }
  })();

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Additional safety check
  if (!Array.isArray(avisAdjudications)) {
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
            Gestion des Avis d'Adjudication
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
            Nouvel Avis d'Adjudication
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
              onClick={() => fetchAvisAdjudications()}
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Adjudication ID</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Type</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date Réception</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Émetteur</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredAvisAdjudications.map((avisAdjudication) => (
              <TableRow 
                key={avisAdjudication.id}
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
                <TableCell>{avisAdjudication.numeroAvis}</TableCell>
                <TableCell>{avisAdjudication.adjudicationId}</TableCell>
                <TableCell>
                  <Box
                    sx={{
                      display: 'inline-block',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      bgcolor: getTypeAvisColor(avisAdjudication.typeAvis),
                      color: 'white',
                      fontSize: '0.8rem',
                      fontWeight: 'bold'
                    }}
                  >
                    {avisAdjudication.typeAvis}
                  </Box>
                </TableCell>
                <TableCell>{avisAdjudication.dateReception}</TableCell>
                <TableCell>{parseFloat(avisAdjudication.montant).toLocaleString()} MAD</TableCell>
                <TableCell>{avisAdjudication.emetteur || 'N/A'}</TableCell>
                <TableCell>
                  <Box
                    sx={{
                      display: 'inline-block',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      bgcolor: getStatusColor(avisAdjudication.statut),
                      color: 'white',
                      fontSize: '0.8rem',
                      fontWeight: 'bold'
                    }}
                  >
                    {avisAdjudication.statut}
                  </Box>
                </TableCell>
                <TableCell>
                  <IconButton 
                    size="small" 
                    onClick={() => handleOpenEditDialog(avisAdjudication)}
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
                    onClick={() => handleOpenDeleteDialog(avisAdjudication)}
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
          {dialogMode === 'create' ? 'Nouvel Avis d\'Adjudication' : 'Modifier Avis d\'Adjudication'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations pour créer un nouvel avis d\'adjudication.' 
              : 'Modifiez les informations de l\'avis d\'adjudication.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="ID Adjudication"
                name="adjudicationId"
                type="number"
                value={currentAvisAdjudication.adjudicationId}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Type d'Avis</InputLabel>
                <Select
                  name="typeAvis"
                  value={currentAvisAdjudication.typeAvis}
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
            <Grid size={12}>
              <TextField
                fullWidth
                label="Numéro d'Avis"
                name="numeroAvis"
                value={currentAvisAdjudication.numeroAvis}
                onChange={handleInputChange}
                required
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
                value={currentAvisAdjudication.dateReception}
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
                value={currentAvisAdjudication.montant}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={12}>
              <TextField
                fullWidth
                label="Émetteur"
                name="emetteur"
                value={currentAvisAdjudication.emetteur}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Statut</InputLabel>
                <Select
                  name="statut"
                  value={currentAvisAdjudication.statut}
                  onChange={handleInputChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="EN_ATTENTE">En Attente</MenuItem>
                  <MenuItem value="PRIS_EN_CHARGE">Pris en Charge</MenuItem>
                  <MenuItem value="COMPTABILISE">Comptabilisé</MenuItem>
                  <MenuItem value="REJETE">Rejeté</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={12}>
              <TextField
                fullWidth
                label="Commentaire"
                name="commentaire"
                value={currentAvisAdjudication.commentaire}
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
            Êtes-vous sûr de vouloir supprimer cet avis d'adjudication ? Cette action est irréversible.
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

export default AvisAdjudicationList;