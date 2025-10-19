import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { applyEmergencyFixes } from '../../utils/authUtils';
import {
  Box,
  Button,
  CircularProgress,
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
  Search as SearchIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import { getAllProjets, createProjet, updateProjet, deleteProjet, medaApi } from '../../services/meda/projetsService';

const ProjetsList = () => {
  const [projets, setProjets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredProjets, setFilteredProjets] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentProjet, setCurrentProjet] = useState({
    id: '',
    nomProjet: '',
    description: '',
    dateDebut: '',
    dateFin: '',
    montantTotal: '',
    devise: 'MAD',
    createdAt: ''
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [projetToDelete, setProjetToDelete] = useState(null);
  const { user, hasRole } = useAuth();

  useEffect(() => {
    // Try applying emergency fixes first
    applyEmergencyFixes();
    
    // Fetch projects when component mounts
    fetchProjets();
  }, [hasRole, user]);

  useEffect(() => {
    filterProjets();
  }, [searchTerm, projets]);

  const fetchProjets = async () => {
    try {
      setLoading(true);
      // Use 2 retries when fetching projects
      const response = await getAllProjets(2);
      // Handle both array and object with data property
      const data = Array.isArray(response) ? response : (response.data || []);
      setProjets(data);
      setError(null);
    } catch (err) {
      console.error('Error fetching projets:', err);
      // Provide more descriptive error message
      if (err.message.includes('Network error')) {
        setError('Erreur: Le service MEDA n\'est pas disponible actuellement. Veuillez vérifier que le microservice est démarré.');
      } else {
        setError('Erreur lors du chargement des projets: ' + err.message);
      }
      // Set empty array on error to prevent infinite loading
      setProjets([]);
    } finally {
      setLoading(false);
    }
  };

  const filterProjets = () => {
    if (!searchTerm) {
      setFilteredProjets(projets);
      return;
    }

    const filtered = projets.filter(projet =>
      projet.nomProjet.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (projet.description && projet.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
      projet.devise.toLowerCase().includes(searchTerm.toLowerCase())
    );

    setFilteredProjets(filtered);
  };

  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setCurrentProjet({
      nomProjet: '',
      description: '',
      dateDebut: '',
      dateFin: '',
      montantTotal: '',
      devise: 'MAD'
    });
    setOpenDialog(true);
  };

  const handleOpenEditDialog = (projet) => {
    setDialogMode('edit');
    setCurrentProjet({
      id: projet.id || '',
      nomProjet: projet.nomProjet || '',
      description: projet.description || '',
      dateDebut: projet.dateDebut || '',
      dateFin: projet.dateFin || '',
      montantTotal: projet.montantTotal || '',
      devise: projet.devise || 'MAD',
      createdAt: projet.createdAt || ''
    });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    setCurrentProjet({
      ...currentProjet,
      [name]: value
    });
  };

  const handleSubmit = async () => {
    try {
      // Log user info for debugging
      const userInfoStr = localStorage.getItem('userInfo');
      console.log('=== USER INFO DEBUG ===');
      console.log('Raw userInfo from localStorage:', userInfoStr);
      
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          console.log('Parsed user info:', userInfo);
          console.log('User roles:', userInfo.roles);
          
          // Check if user is admin
          const isAdmin = userInfo.roles && Array.isArray(userInfo.roles) && 
            userInfo.roles.some(role => {
              const normalizedRole = role.trim().toLowerCase();
              return normalizedRole === 'admin' || 
                     normalizedRole.includes('admin') ||
                     normalizedRole === 'administrateur' ||
                     normalizedRole.includes('administrateur');
            });
          
          console.log('Is user admin?', isAdmin);
          
          // Check if user has MEDA role
          const hasMedaRole = userInfo.roles && Array.isArray(userInfo.roles) &&
            userInfo.roles.some(role => {
              const normalizedRole = role.trim().toLowerCase();
              return normalizedRole === 'meda' || 
                     normalizedRole.includes('meda') ||
                     normalizedRole === 'role_meda';
            });
          
          console.log('User has MEDA role?', hasMedaRole);
        } catch (e) {
          console.error('Error parsing user info:', e);
        }
      }
      
      console.log('=== END USER INFO DEBUG ===');
      
      if (dialogMode === 'create') {
        await createProjet(currentProjet);
      } else {
        await updateProjet(currentProjet.id, currentProjet);
      }
      fetchProjets();
      handleCloseDialog();
    } catch (err) {
      // Handle 403 errors specifically without redirecting
      if (err.message.includes('Accès non autorisé') || err.message.includes('403')) {
        setError('Accès refusé. Veuillez vérifier vos permissions avec un administrateur.');
      } else {
        setError('Erreur lors de l\'enregistrement du projet: ' + err.message);
      }
      console.error('Error saving projet:', err);
    }
  };

  const handleOpenDeleteDialog = (projet) => {
    setProjetToDelete(projet);
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setProjetToDelete(null);
  };

  const handleDelete = async () => {
    try {
      await deleteProjet(projetToDelete.id);
      fetchProjets();
      handleCloseDeleteDialog();
    } catch (err) {
      setError('Erreur lors de la suppression du projet: ' + err.message);
      console.error('Error deleting projet:', err);
    }
  };

  const formatCurrency = (amount, currency) => {
    if (!amount) return '0.00';
    return parseFloat(amount).toLocaleString('fr-FR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }) + ' ' + currency;
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'ACTIF': return '#4caf50';
      case 'TERMINE': return '#2196f3';
      case 'EN_COURS': return '#ff9800';
      default: return '#9e9e9e';
    }
  };

  const retryFetch = () => {
    setLoading(true);
    setError(null);
    fetchProjets();
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '200px' }}>
        <CircularProgress />
        <Typography sx={{ mt: 2 }}>Chargement des projets...</Typography>
      </Box>
    );
  }
  
  if (error && !projets.length) {
    return (
      <Box sx={{ p: 3 }}>
        <Paper sx={{ p: 4, bgcolor: '#ffebee', textAlign: 'center', borderRadius: 2 }}>
          <Typography variant="h5" gutterBottom color="error">
            {error.includes('Accès non autorisé') ? 'Erreur: Accès non autorisé' : 'Erreur: API request failed'}
          </Typography>
          <Typography variant="body1" paragraph>
            {error.includes('Accès non autorisé') 
              ? 'Vous n\'avez pas les permissions nécessaires pour accéder au service MEDA.' 
              : 'Le service MEDA n\'est pas disponible actuellement.'}
          </Typography>
          <Typography variant="body2" paragraph color="textSecondary" sx={{ whiteSpace: 'pre-wrap' }}>
            {error}
          </Typography>
          <Typography variant="body2" paragraph color="primary">
            Les en-têtes d'authentification sont gérés automatiquement par le service.
          </Typography>
          {error.includes('Accès non autorisé') ? (
            <>
              <Typography variant="body2" paragraph color="textSecondary">
                Veuillez contacter un administrateur pour vérifier vos permissions.
              </Typography>
              <Button 
                variant="outlined" 
                color="primary" 
                onClick={retryFetch}
                startIcon={<RefreshIcon />}
                sx={{ mt: 2, borderRadius: 2 }}
              >
                Réessayer
              </Button>
            </>
          ) : (
            <Button 
              variant="contained" 
              color="primary" 
              onClick={retryFetch}
              startIcon={<RefreshIcon />}
              sx={{ mt: 2, borderRadius: 2 }}
            >
              Réessayer
            </Button>
          )}
        </Paper>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#333', fontWeight: 'bold' }}>
            Gestion des Projets MEDA
          </Typography>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            color="primary"
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
            Ajouter un Projet
          </Button>
        </Grid>
      </Grid>

      {error && (
        <Box sx={{ mb: 3 }}>
          <Paper sx={{ p: 3, bgcolor: '#ffebee', borderRadius: 2 }}>
            <Typography color="error" variant="h6">Erreur: API request failed</Typography>
            <Typography color="error">{error}</Typography>
            <Button 
              variant="outlined" 
              color="primary" 
              sx={{ mt: 2, borderRadius: 2 }}
              onClick={() => fetchProjets()}
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
          placeholder="Rechercher des projets..."
          value={searchTerm}
          onChange={handleSearchChange}
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Nom du Projet</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Description</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date Début</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date Fin</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant Total</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Créé le</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredProjets && filteredProjets.length > 0 ? (
              filteredProjets.map((projet) => (
                <TableRow 
                  key={projet.id}
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
                  <TableCell>{projet.id}</TableCell>
                  <TableCell sx={{ fontWeight: '500' }}>{projet.nomProjet}</TableCell>
                  <TableCell>{projet.description || 'N/A'}</TableCell>
                  <TableCell>
                    {projet.dateDebut ? new Date(projet.dateDebut).toLocaleDateString('fr-FR') : 'N/A'}
                  </TableCell>
                  <TableCell>
                    {projet.dateFin ? new Date(projet.dateFin).toLocaleDateString('fr-FR') : 'N/A'}
                  </TableCell>
                  <TableCell>{formatCurrency(projet.montantTotal, projet.devise)}</TableCell>
                  <TableCell>{projet.devise}</TableCell>
                  <TableCell>
                    <Box
                      sx={{
                        display: 'inline-block',
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor: getStatusColor(projet.statut),
                        color: 'white',
                        fontSize: '0.8rem',
                        fontWeight: 'bold'
                      }}
                    >
                      {projet.statut || 'N/A'}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {projet.createdAt ? new Date(projet.createdAt).toLocaleDateString('fr-FR') : 'N/A'}
                  </TableCell>
                  <TableCell>
                    <IconButton
                      color="primary"
                      onClick={() => handleOpenEditDialog(projet)}
                      size="small"
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
                      color="error"
                      onClick={() => handleOpenDeleteDialog(projet)}
                      size="small"
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
            ) : (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  Aucun projet trouvé
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
          {dialogMode === 'create' ? 'Ajouter un Projet' : 'Modifier un Projet'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations du nouveau projet.' 
              : 'Modifiez les informations du projet.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Nom du Projet"
                name="nomProjet"
                value={currentProjet.nomProjet}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Description"
                name="description"
                value={currentProjet.description}
                onChange={handleInputChange}
                multiline
                rows={3}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Date Début"
                name="dateDebut"
                type="date"
                value={currentProjet.dateDebut}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Date Fin"
                name="dateFin"
                type="date"
                value={currentProjet.dateFin}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Montant Total"
                name="montantTotal"
                type="number"
                value={currentProjet.montantTotal}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Devise</InputLabel>
                <Select
                  name="devise"
                  value={currentProjet.devise}
                  onChange={handleInputChange}
                  label="Devise"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="MAD">MAD</MenuItem>
                  <MenuItem value="EUR">EUR</MenuItem>
                  <MenuItem value="USD">USD</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button 
            onClick={handleCloseDialog} 
            sx={{ borderRadius: 2 }}
          >
            Annuler
          </Button>
          <Button 
            onClick={handleSubmit} 
            variant="contained" 
            color="primary"
            sx={{ 
              bgcolor: '#FF6B35',
              '&:hover': {
                bgcolor: '#e55a2b'
              },
              borderRadius: 2
            }}
          >
            {dialogMode === 'create' ? 'Créer' : 'Mettre à jour'}
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
            Êtes-vous sûr de vouloir supprimer le projet "{projetToDelete?.nomProjet}" ?
            Cette action est irréversible.
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button 
            onClick={handleCloseDeleteDialog}
            sx={{ borderRadius: 2 }}
          >
            Annuler
          </Button>
          <Button 
            onClick={handleDelete} 
            color="error" 
            variant="contained"
            sx={{ borderRadius: 2 }}
          >
            Supprimer
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ProjetsList;