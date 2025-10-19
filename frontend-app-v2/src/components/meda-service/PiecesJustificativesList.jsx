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
import { getAllPiecesJustificatives, createPieceJustificative, updatePieceJustificative, deletePieceJustificative } from '../../services/meda/piecesJustificativesService';
import { getProjetById } from '../../services/meda/projetsService'; // Import projet service
import { handle403Error } from '../../utils/authUtils';

const PiecesJustificativesList = () => {
  const [pieces, setPieces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredPieces, setFilteredPieces] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentPiece, setCurrentPiece] = useState({
    id: '',
    projetId: '',
    numeroPiece: '',
    typePiece: 'FACTURE',
    dateExecution: '',
    montant: '',
    devise: 'MAD',
    emetteur: '',
    statut: 'PRIS_EN_CHARGE',
    commentaire: '',
    createdAt: ''
  });
  const [projectNames, setProjectNames] = useState({}); // State to store project names

  useEffect(() => {
    // Fetch data when component mounts
    fetchPieces();
  }, []);

  useEffect(() => {
    filterPieces();
  }, [searchTerm, pieces, projectNames]);

  // Effect to fetch project currency when projetId changes
  useEffect(() => {
    const fetchProjectCurrency = async () => {
      console.log('useEffect: Checking project currency for projetId:', currentPiece.projetId, 'openDialog:', openDialog);
      if (openDialog && currentPiece.projetId && !isNaN(currentPiece.projetId) && parseInt(currentPiece.projetId) > 0) {
        try {
          console.log('Fetching project currency for ID:', currentPiece.projetId);
          const response = await getProjetById(currentPiece.projetId);
          console.log('Project fetched:', response);
          // Extract the project data from the response
          const projet = response?.data || response;
          if (projet && projet.devise) {
            console.log('Setting currency to:', projet.devise);
            setCurrentPiece(prev => ({
              ...prev,
              devise: projet.devise
            }));
          }
        } catch (err) {
          console.error('Error fetching project details:', err);
          // Don't reset currency on error to avoid overriding user input
        }
      } else if (openDialog && (!currentPiece.projetId || currentPiece.projetId === '')) {
        // If project ID is cleared, reset the currency to default
        console.log('Resetting currency to default (MAD)');
        setCurrentPiece(prev => ({
          ...prev,
          devise: 'MAD'
        }));
      }
    };

    fetchProjectCurrency();
  }, [currentPiece.projetId, openDialog]);

  const fetchPieces = async () => {
    try {
      setLoading(true);
      const response = await getAllPiecesJustificatives();
      console.log('Réponse de l\'API (pièces justificatives):', response);
      // Handle both array and object with data property
      const data = Array.isArray(response) ? response : (response.data || []);
      // Also handle pagination response with content property
      const content = Array.isArray(data) ? data : (data.content || []);
      console.log('Données des pièces justificatives:', content);
      setPieces(content);
      setError(null);
      
      // Fetch project names for all pieces
      const projectNamesMap = {};
      console.log('Chargement des noms de projets pour les pièces justificatives');
      for (const piece of content) {
        console.log('Pièce justificative:', { id: piece.id, projetId: piece.projetId });
        if (piece.projetId && !projectNamesMap[piece.projetId]) {
          try {
            console.log(`Récupération des détails du projet ${piece.projetId}`);
            const response = await getProjetById(piece.projetId);
            // Extract the project data from the response
            const projet = response?.data || response;
            console.log(`Détails du projet ${piece.projetId}:`, projet);
            projectNamesMap[piece.projetId] = projet?.nomProjet || `Projet ${piece.projetId}`;
          } catch (err) {
            console.error(`Error fetching project ${piece.projetId}:`, err);
            projectNamesMap[piece.projetId] = `Projet ${piece.projetId}`;
          }
        }
      }
      console.log('Noms de projets chargés:', projectNamesMap);
      setProjectNames(projectNamesMap);
    } catch (err) {
      setError('Erreur lors du chargement des pièces justificatives: ' + err.message);
      console.error('Error fetching pieces:', err);
      // Set empty array on error to prevent infinite loading
      setPieces([]);
    } finally {
      setLoading(false);
    }
  };

  const filterPieces = () => {
    if (!searchTerm) {
      setFilteredPieces(pieces);
      return;
    }

    const filtered = pieces.filter(piece =>
      piece.numeroPiece?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (piece.emetteur && piece.emetteur.toLowerCase().includes(searchTerm.toLowerCase())) ||
      piece.devise?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      piece.typePiece?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (piece.projetId && projectNames[piece.projetId] && projectNames[piece.projetId].toLowerCase().includes(searchTerm.toLowerCase()))
    );

    setFilteredPieces(filtered);
  };

  // Add a helper function to get project currency
  const getProjectCurrency = async (projetId) => {
    try {
      const response = await getProjetById(projetId);
      // Extract the project data from the response
      const projet = response?.data || response;
      return projet?.devise || 'MAD'; // Default to MAD if not found
    } catch (err) {
      console.error(`Error fetching project ${projetId} currency:`, err);
      return 'MAD'; // Default to MAD on error
    }
  };

  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };

  const handleOpenCreateDialog = () => {
    console.log('Opening create dialog for piece');
    setDialogMode('create');
    // Set today's date as default for dateExecution
    const today = new Date().toISOString().split('T')[0];
    setCurrentPiece({
      projetId: '',
      numeroPiece: '',
      typePiece: 'FACTURE',
      dateExecution: today,
      montant: '',
      devise: 'MAD',
      emetteur: '',
      statut: 'PRIS_EN_CHARGE',
      commentaire: ''
    });
    setOpenDialog(true);
  };

  // Add a new function to handle project ID change and fetch project currency
  const handleProjectIdChange = async (event) => {
    const { name, value } = event.target;
    
    // Update the projetId first
    setCurrentPiece(prev => ({
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
          setCurrentPiece(prev => ({
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
      setCurrentPiece(prev => ({
        ...prev,
        devise: 'MAD'
      }));
    }
  };

  const handleOpenEditDialog = async (piece) => {
    console.log('Opening edit dialog for piece:', piece);
    setDialogMode('edit');
    
    // Ensure dateExecution is properly formatted if it exists
    let dateExecution = '';
    if (piece.dateExecution) {
      // Handle different date formats
      if (typeof piece.dateExecution === 'string') {
        // If it's already a string in YYYY-MM-DD format
        if (piece.dateExecution.match(/^\d{4}-\d{2}-\d{2}/)) {
          dateExecution = piece.dateExecution;
        } else {
          // If it's in another format, try to parse it
          const date = new Date(piece.dateExecution);
          if (!isNaN(date.getTime())) {
            dateExecution = date.toISOString().split('T')[0];
          }
        }
      } else if (piece.dateExecution instanceof Date) {
        // If it's a Date object
        dateExecution = piece.dateExecution.toISOString().split('T')[0];
      }
    }
    
    // For editing, we should ensure the currency matches the project currency
    let devise = piece.devise || 'MAD';
    if (piece.projetId) {
      try {
        const response = await getProjetById(piece.projetId);
        // Extract the project data from the response
        const projet = response?.data || response;
        if (projet && projet.devise) {
          devise = projet.devise;
        }
      } catch (err) {
        console.error('Error fetching project details for edit dialog:', err);
      }
    }
    
    setCurrentPiece({
      id: piece.id || '',
      projetId: piece.projetId || '',
      numeroPiece: piece.numeroPiece || '',
      typePiece: piece.typePiece || 'FACTURE',
      dateExecution: dateExecution || new Date().toISOString().split('T')[0],
      montant: piece.montant || '',
      devise: devise,
      emetteur: piece.emetteur || '',
      statut: piece.statut || 'PRIS_EN_CHARGE',
      commentaire: piece.commentaire || '',
      createdAt: piece.createdAt || ''
    });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    console.log('Input changed:', name, value);
    // If the user is trying to change the currency manually when a project is selected, prevent it
    if (name === 'devise' && currentPiece.projetId) {
      console.log('Preventing manual currency change when project is selected');
      return;
    }
    setCurrentPiece({
      ...currentPiece,
      [name]: value
    });
  };

  const handleSubmit = async () => {
    try {
      // Validate project ID
      if (!currentPiece.projetId || currentPiece.projetId <= 0) {
        throw new Error('Veuillez sélectionner un projet valide.');
      }
      
      // Validate that the project exists and get its currency
      let projectCurrency = 'MAD';
      try {
        const response = await getProjetById(currentPiece.projetId);
        // Extract the project data from the response
        const projet = response?.data || response;
        projectCurrency = projet?.devise || 'MAD';
        console.log('Project currency:', projectCurrency);
      } catch (err) {
        throw new Error('Le projet sélectionné n\'existe pas. Veuillez vérifier l\'ID du projet.');
      }
      
      // Validate dateExecution is not empty
      if (!currentPiece.dateExecution) {
        throw new Error('La date d\'exécution est obligatoire.');
      }
      
      // Create a copy of the current piece with the correct currency
      // This ensures that the currency always matches the project's currency
      // Also convert projetId to projet object as expected by the backend
      const pieceToSubmit = {
        ...currentPiece,
        projet: { id: currentPiece.projetId },
        devise: projectCurrency
      };
      
      // Remove the projetId field as it's not needed in the backend object
      delete pieceToSubmit.projetId;
      
      console.log('Piece to submit:', pieceToSubmit);
      
      if (dialogMode === 'create') {
        await createPieceJustificative(pieceToSubmit);
      } else {
        await updatePieceJustificative(pieceToSubmit.id, pieceToSubmit);
      }
      fetchPieces();
      handleCloseDialog();
    } catch (err) {
      // Handle 403 Forbidden errors specifically
      if (err.message.includes('403') || err.message.includes('Accès non autorisé')) {
        const handled = handle403Error();
        if (handled) {
          return; // Page will reload, so don't show error
        }
      }
      
      setError('Erreur lors de l\'enregistrement de la pièce justificative: ' + err.message);
      console.error('Error saving piece:', err);
    }
  };

  const handleDelete = async (id) => {
    try {
      await deletePieceJustificative(id);
      fetchPieces();
    } catch (err) {
      // Handle 403 Forbidden errors specifically
      if (err.message.includes('403') || err.message.includes('Accès non autorisé')) {
        const handled = handle403Error();
        if (handled) {
          return; // Page will reload, so don't show error
        }
      }
      
      setError('Erreur lors de la suppression de la pièce justificative: ' + err.message);
      console.error('Error deleting piece:', err);
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
      case 'FACTURE': return '#4caf50';
      case 'BON_PAIEMENT': return '#2196f3';
      case 'RECU': return '#ff9800';
      case 'AUTRE': return '#9c27b0';
      default: return '#9e9e9e';
    }
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'PRIS_EN_CHARGE': return '#ff9800';
      case 'VALIDE': return '#4caf50';
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
            Gestion des Pièces Justificatives MEDA
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
            Ajouter une Pièce
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
              onClick={() => fetchPieces()}
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
          placeholder="Rechercher des pièces justificatives..."
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date Exécution</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Émetteur</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Commentaire</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Créé le</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredPieces && filteredPieces.length > 0 ? (
              filteredPieces.map((piece) => (
                <TableRow 
                  key={piece.id}
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
                  <TableCell>{piece.id}</TableCell>
                  <TableCell sx={{ fontWeight: '500' }}>{piece.numeroPiece}</TableCell>
                  <TableCell>{piece.projetId ? (projectNames[piece.projetId] || `Projet ${piece.projetId}`) : 'N/A'}</TableCell>
                  <TableCell>
                    <Box
                      sx={{
                        display: 'inline-block',
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor: getTypeColor(piece.typePiece),
                        color: 'white',
                        fontSize: '0.8rem',
                        fontWeight: 'bold'
                      }}
                    >
                      {piece.typePiece}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {piece.dateExecution ? new Date(piece.dateExecution).toLocaleDateString('fr-FR') : 'N/A'}
                  </TableCell>
                  <TableCell>{formatCurrency(piece.montant, piece.devise)}</TableCell>
                  <TableCell>{piece.devise}</TableCell>
                  <TableCell>{piece.emetteur}</TableCell>
                  <TableCell>
                    <Box
                      sx={{
                        display: 'inline-block',
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor: getStatusColor(piece.statut),
                        color: 'white',
                        fontSize: '0.8rem',
                        fontWeight: 'bold'
                      }}
                    >
                      {piece.statut}
                    </Box>
                  </TableCell>
                  <TableCell>{piece.commentaire || 'N/A'}</TableCell>
                  <TableCell>
                    {piece.createdAt ? new Date(piece.createdAt).toLocaleDateString('fr-FR') : 'N/A'}
                  </TableCell>
                  <TableCell>
                    <IconButton
                      color="primary"
                      onClick={() => handleOpenEditDialog(piece)}
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
                      onClick={() => handleDelete(piece.id)}
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
                <TableCell colSpan={12} align="center">
                  Aucune pièce justificative trouvée
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
          {dialogMode === 'create' ? 'Ajouter une Pièce Justificative' : 'Modifier une Pièce Justificative'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {dialogMode === 'create' 
              ? 'Remplissez les informations de la nouvelle pièce justificative.' 
              : 'Modifiez les informations de la pièce justificative.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Numéro de Pièce"
                name="numeroPiece"
                value={currentPiece.numeroPiece}
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
                value={currentPiece.projetId}
                onChange={handleProjectIdChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
                helperText={currentPiece.projetId && projectNames[currentPiece.projetId] 
                  ? `Projet: ${projectNames[currentPiece.projetId]}` 
                  : "Entrez l'ID du projet"}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Type de Pièce"
                name="typePiece"
                value={currentPiece.typePiece}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Date d'Exécution"
                name="dateExecution"
                type="date"
                value={currentPiece.dateExecution}
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
                value={currentPiece.montant}
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
                  value={currentPiece.devise}
                  onChange={handleInputChange}
                  label="Devise"
                  sx={{ borderRadius: 2 }}
                  disabled={!!currentPiece.projetId} // Disable if project is selected
                >
                  <MenuItem value="MAD">MAD</MenuItem>
                  <MenuItem value="EUR">EUR</MenuItem>
                  <MenuItem value="USD">USD</MenuItem>
                </Select>
              </FormControl>
              {currentPiece.projetId ? (
                <Typography variant="caption" display="block" sx={{ mt: 1, color: 'text.secondary' }}>
                  La devise est automatiquement définie selon le projet sélectionné: {currentPiece.devise}
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
                value={currentPiece.emetteur}
                onChange={handleInputChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Commentaire"
                name="commentaire"
                value={currentPiece.commentaire}
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

export default PiecesJustificativesList;