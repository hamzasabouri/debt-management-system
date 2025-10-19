import React, { useState, useEffect } from 'react';
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
  Search as SearchIcon
} from '@mui/icons-material';
import { getAllAvances, createAvance, updateAvance, deleteAvance } from '../../services/meda/avancesService';
import { getProjetById } from '../../services/meda/projetsService';
import { handle403Error } from '../../utils/authUtils';

const AvancesList = () => {
  const [avances, setAvances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredAvances, setFilteredAvances] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentAvance, setCurrentAvance] = useState({
    id: '',
    projetId: '',
    numeroAvance: '',
    typeAvance: 'DON',
    dateReception: '',
    montant: '',
    devise: 'MAD',
    emetteur: '',
    statut: 'PRIS_EN_CHARGE',
    createdAt: ''
  });
  const [projectNames, setProjectNames] = useState({}); // State to store project names

  useEffect(() => {
    // Fetch data when component mounts
    fetchAvances();
  }, []);

  useEffect(() => {
    filterAvances();
  }, [searchTerm, avances, projectNames]);

  // Effect to fetch project currency when projetId changes
  useEffect(() => {
    const fetchProjectCurrency = async () => {
      console.log('useEffect: Checking project currency for projetId:', currentAvance.projetId, 'openDialog:', openDialog);
      if (openDialog && currentAvance.projetId && !isNaN(currentAvance.projetId) && parseInt(currentAvance.projetId) > 0) {
        try {
          console.log('Fetching project currency for ID:', currentAvance.projetId);
          const response = await getProjetById(currentAvance.projetId);
          console.log('Project fetched:', response);
          // Extract the project data from the response
          const projet = response?.data || response;
          if (projet && projet.devise) {
            console.log('Setting currency to:', projet.devise);
            setCurrentAvance(prev => ({
              ...prev,
              devise: projet.devise
            }));
          }
        } catch (err) {
          console.error('Error fetching project details:', err);
          // Don't reset currency on error to avoid overriding user input
        }
      } else if (openDialog && (!currentAvance.projetId || currentAvance.projetId === '')) {
        // If project ID is cleared, reset the currency to default
        console.log('Resetting currency to default (MAD)');
        setCurrentAvance(prev => ({
          ...prev,
          devise: 'MAD'
        }));
      }
    };

    fetchProjectCurrency();
  }, [currentAvance.projetId, openDialog]);

  const fetchAvances = async () => {
    try {
      setLoading(true);
      const response = await getAllAvances();
      // Handle both array and object with data property
      const data = Array.isArray(response) ? response : (response.data || []);
      // Also handle pagination response with content property
      const content = Array.isArray(data) ? data : (data.content || []);
      setAvances(content);
      setError(null);
      
      // Fetch project names for all avances
      const projectNamesMap = {};
      for (const avance of content) {
        if (avance.projetId && !projectNamesMap[avance.projetId]) {
          try {
            const projet = await getProjetById(avance.projetId);
            projectNamesMap[avance.projetId] = projet?.nomProjet || `Projet ${avance.projetId}`;
          } catch (err) {
            console.error(`Error fetching project ${avance.projetId}:`, err);
            projectNamesMap[avance.projetId] = `Projet ${avance.projetId}`;
          }
        }
      }
      setProjectNames(projectNamesMap);
    } catch (err) {
      setError('Erreur lors du chargement des avances: ' + err.message);
      console.error('Error fetching avances:', err);
      // Set empty array on error to prevent infinite loading
      setAvances([]);
    } finally {
      setLoading(false);
    }
  };

  // Add a helper function to get project currency
  const getProjectCurrency = async (projetId) => {
    try {
      const projet = await getProjetById(projetId);
      return projet?.devise || 'MAD'; // Default to MAD if not found
    } catch (err) {
      console.error(`Error fetching project ${projetId} currency:`, err);
      return 'MAD'; // Default to MAD on error
    }
  };

  const filterAvances = () => {
    if (!searchTerm) {
      setFilteredAvances(avances);
      return;
    }

    const filtered = avances.filter(avance =>
      avance.numeroAvance?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (avance.emetteur && avance.emetteur.toLowerCase().includes(searchTerm.toLowerCase())) ||
      avance.devise?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      avance.typeAvance?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (avance.projetId && projectNames[avance.projetId] && projectNames[avance.projetId].toLowerCase().includes(searchTerm.toLowerCase()))
    );

    setFilteredAvances(filtered);
  };

  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };

  const handleOpenCreateDialog = () => {
    console.log('Opening create dialog');
    setDialogMode('create');
    // Set today's date as default for dateReception
    const today = new Date().toISOString().split('T')[0];
    console.log('Setting default values for new advance');
    setCurrentAvance({
      projetId: '',
      numeroAvance: '',
      typeAvance: 'DON',
      dateReception: today,
      montant: '',
      devise: 'MAD', // Will be updated when project is selected
      emetteur: '',
      statut: 'PRIS_EN_CHARGE'
    });
    setOpenDialog(true);
  };

  // Add a new function to handle project ID change and fetch project currency
  const handleProjectIdChange = async (event) => {
    const { name, value } = event.target;
    
    // Update the projetId first
    setCurrentAvance(prev => ({
      ...prev,
      [name]: value
    }));
    
    // If this is the projetId field and we have a valid project ID, fetch the project details
    if (name === 'projetId' && value && !isNaN(value) && parseInt(value) > 0) {
      try {
        console.log('Fetching project details for ID:', value);
        // Show loading state or some indicator
        const response = await getProjetById(value);
        console.log('Project details fetched:', response);
        // Extract the project data from the response
        const projet = response?.data || response;
        if (projet && projet.devise) {
          // Update both projetId and devise fields together
          setCurrentAvance(prev => ({
            ...prev,
            devise: projet.devise
          }));
          console.log('Updated currency to:', projet.devise);
        }
      } catch (err) {
        console.error('Error fetching project details:', err);
        // Don't reset devise to default if project not found, let the user know via validation
      }
    } else if (name === 'projetId' && (!value || value === '')) {
      // If project ID is cleared, reset the currency to default
      setCurrentAvance(prev => ({
        ...prev,
        devise: 'MAD'
      }));
    }
  };

  const handleOpenEditDialog = async (avance) => {
    console.log('Opening edit dialog for advance:', avance);
    setDialogMode('edit');
    // Ensure dateReception is properly formatted if it exists
    let dateReception = '';
    if (avance.dateReception) {
      // Handle different date formats
      if (typeof avance.dateReception === 'string') {
        // If it's already a string in YYYY-MM-DD format
        if (avance.dateReception.match(/^\d{4}-\d{2}-\d{2}/)) {
          dateReception = avance.dateReception;
        } else {
          // If it's in another format, try to parse it
          const date = new Date(avance.dateReception);
          if (!isNaN(date.getTime())) {
            dateReception = date.toISOString().split('T')[0];
          }
        }
      } else if (avance.dateReception instanceof Date) {
        // If it's a Date object
        dateReception = avance.dateReception.toISOString().split('T')[0];
      }
    }
    
    // For editing, we should ensure the currency matches the project currency
    let devise = avance.devise || 'MAD';
    if (avance.projetId) {
      try {
        const response = await getProjetById(avance.projetId);
        // Extract the project data from the response
        const projet = response?.data || response;
        if (projet && projet.devise) {
          devise = projet.devise;
        }
      } catch (err) {
        console.error('Error fetching project details for edit dialog:', err);
      }
    }
    
    const advanceToEdit = {
      id: avance.id || '',
      projetId: avance.projetId || '',
      numeroAvance: avance.numeroAvance || '',
      typeAvance: avance.typeAvance || 'DON',
      dateReception: dateReception || new Date().toISOString().split('T')[0],
      montant: avance.montant || '',
      devise: devise,
      emetteur: avance.emetteur || '',
      statut: avance.statut || 'PRIS_EN_CHARGE',
      createdAt: avance.createdAt || ''
    };
    
    console.log('Setting current advance for editing:', advanceToEdit);
    setCurrentAvance(advanceToEdit);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    console.log('Input changed:', name, value);
    // If the user is trying to change the currency manually when a project is selected, prevent it
    if (name === 'devise' && currentAvance.projetId) {
      console.log('Preventing manual currency change when project is selected');
      return;
    }
    setCurrentAvance({
      ...currentAvance,
      [name]: value
    });
  };

  const handleSubmit = async () => {
    try {
      console.log('Submitting advance:', currentAvance);
      // Validate project ID
      if (!currentAvance.projetId || currentAvance.projetId <= 0) {
        throw new Error('Veuillez sélectionner un projet valide.');
      }
      
      // Validate that the project exists and get its currency
      let projectCurrency = 'MAD';
      try {
        const response = await getProjetById(currentAvance.projetId);
        // Extract the project data from the response
        const projet = response?.data || response;
        projectCurrency = projet?.devise || 'MAD';
        console.log('Project currency:', projectCurrency);
      } catch (err) {
        throw new Error('Le projet sélectionné n\'existe pas. Veuillez vérifier l\'ID du projet.');
      }
      
      // Validate dateReception is not empty
      if (!currentAvance.dateReception) {
        throw new Error('La date de réception est obligatoire.');
      }
      
      // Create a copy of the current advance with the correct currency
      // This ensures that the currency always matches the project's currency
      const advanceToSubmit = {
        ...currentAvance,
        devise: projectCurrency
      };
      
      console.log('Advance to submit:', advanceToSubmit);
      
      if (dialogMode === 'create') {
        await createAvance(advanceToSubmit);
      } else {
        await updateAvance(advanceToSubmit.id, advanceToSubmit);
      }
      fetchAvances();
      handleCloseDialog();
    } catch (err) {
      // Handle 403 Forbidden errors specifically
      if (err.message.includes('403') || err.message.includes('Accès non autorisé')) {
        const handled = handle403Error();
        if (handled) {
          return; // Page will reload, so don't show error
        }
      }
      
      setError('Erreur lors de l\'enregistrement de l\'avance: ' + err.message);
      console.error('Error saving avance:', err);
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteAvance(id);
      fetchAvances();
    } catch (err) {
      // Handle 403 Forbidden errors specifically
      if (err.message.includes('403') || err.message.includes('Accès non autorisé')) {
        const handled = handle403Error();
        if (handled) {
          return; // Page will reload, so don't show error
        }
      }
      
      setError('Erreur lors de la suppression de l\'avance: ' + err.message);
      console.error('Error deleting avance:', err);
    }
  };

  const formatCurrency = (amount, currency) => {
    if (!amount) return '0.00';
    return parseFloat(amount).toLocaleString('fr-FR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }) + ' ' + currency;
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'DON': return '#4caf50';
      case 'PRET': return '#2196f3';
      default: return '#9e9e9e';
    }
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'PRIS_EN_CHARGE': return '#ff9800';
      case 'COMPTABILISE': return '#4caf50';
      case 'REJETE': return '#f44336';
      default: return '#9e9e9e';
    }
  };

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
            Gestion des Avances MEDA
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
            Ajouter une Avance
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
              onClick={() => fetchAvances()}
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
          placeholder="Rechercher des avances..."
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Numéro</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Projet</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Type</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date Réception</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Émetteur</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Créé le</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredAvances && filteredAvances.length > 0 ? (
              filteredAvances.map((avance) => (
                <TableRow 
                  key={avance.id}
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
                  <TableCell>{avance.id}</TableCell>
                  <TableCell sx={{ fontWeight: '500' }}>{avance.numeroAvance}</TableCell>
                  <TableCell>
                    {avance.projetId ? (
                      <>
                        {projectNames[avance.projetId] || `Projet ${avance.projetId}`}
                        <Typography variant="caption" display="block" sx={{ color: 'text.secondary' }}>
                          {avance.devise}
                        </Typography>
                      </>
                    ) : 'N/A'}
                  </TableCell>
                  <TableCell>
                    <Box
                      sx={{
                        display: 'inline-block',
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor: getTypeColor(avance.typeAvance),
                        color: 'white',
                        fontSize: '0.8rem',
                        fontWeight: 'bold'
                      }}
                    >
                      {avance.typeAvance}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {avance.dateReception ? new Date(avance.dateReception).toLocaleDateString('fr-FR') : 'N/A'}
                  </TableCell>
                  <TableCell>{formatCurrency(avance.montant, avance.devise)}</TableCell>
                  <TableCell>{avance.devise}</TableCell>
                  <TableCell>{avance.emetteur}</TableCell>
                  <TableCell>
                    <Box
                      sx={{
                        display: 'inline-block',
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor: getStatusColor(avance.statut),
                        color: 'white',
                        fontSize: '0.8rem',
                        fontWeight: 'bold'
                      }}
                    >
                      {avance.statut}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {avance.createdAt ? new Date(avance.createdAt).toLocaleDateString('fr-FR') : 'N/A'}
                  </TableCell>
                  <TableCell>
                    <IconButton
                      color="primary"
                      onClick={() => handleOpenEditDialog(avance)}
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
                      onClick={() => handleDelete(avance.id)}
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
                <TableCell colSpan={11} align="center">
                  Aucune avance trouvée
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
          {dialogMode === 'create' ? 'Ajouter une Avance' : 'Modifier une Avance'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations de la nouvelle avance.' 
              : 'Modifiez les informations de l\'avance.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Numéro d'Avance"
                name="numeroAvance"
                value={currentAvance.numeroAvance}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="ID du Projet"
                name="projetId"
                type="number"
                value={currentAvance.projetId}
                onChange={handleProjectIdChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
                helperText={currentAvance.projetId && projectNames[currentAvance.projetId] 
                  ? `Projet: ${projectNames[currentAvance.projetId]}` 
                  : "Entrez l'ID du projet"}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Type d'Avance</InputLabel>
                <Select
                  name="typeAvance"
                  value={currentAvance.typeAvance}
                  onChange={handleInputChange}
                  label="Type d'Avance"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="DON">Don</MenuItem>
                  <MenuItem value="PRET">Prêt</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Date de Réception"
                name="dateReception"
                type="date"
                value={currentAvance.dateReception}
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
                label="Montant"
                name="montant"
                type="number"
                value={currentAvance.montant}
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
                  value={currentAvance.devise}
                  onChange={handleInputChange}
                  label="Devise"
                  sx={{ borderRadius: 2 }}
                  disabled={!!currentAvance.projetId} // Disable if project is selected
                >
                  <MenuItem value="MAD">MAD</MenuItem>
                  <MenuItem value="EUR">EUR</MenuItem>
                  <MenuItem value="USD">USD</MenuItem>
                </Select>
              </FormControl>
              {currentAvance.projetId ? (
                <Typography variant="caption" display="block" sx={{ mt: 1, color: 'text.secondary' }}>
                  La devise est automatiquement définie selon le projet sélectionné: {currentAvance.devise}
                </Typography>
              ) : (
                <Typography variant="caption" display="block" sx={{ mt: 1, color: 'text.secondary' }}>
                  Sélectionnez une devise
                </Typography>
              )}
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Émetteur"
                name="emetteur"
                value={currentAvance.emetteur}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
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
    </Box>
  );
};

export default AvancesList;
