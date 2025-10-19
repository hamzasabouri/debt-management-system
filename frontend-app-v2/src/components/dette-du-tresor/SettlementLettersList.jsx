import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogTitle,
  DialogContent,
  TextField,
  Grid,
  IconButton,
  Typography,
  InputAdornment,
  MenuItem,
  Select,
  InputLabel,
  FormControl
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  PictureAsPdf as PdfIcon,
  Search as SearchIcon,
  Visibility as ViewIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { getAllSettlementLetters, createSettlementLetter, updateSettlementLetter, deleteSettlementLetter, searchSettlementLetters, generateSettlementLetterPdf, setAuthToken } from '../../services/dette-tresor/settlementLettersService';
import PdfViewer from './PdfViewer';

// Function to flatten settlement letter data and remove circular references
const flattenSettlementLetter = (letter) => {
  if (!letter) return null;
  
  // Create a shallow copy of the letter with proper property mapping
  const flattened = { 
    id: letter.id,
    numero_lettre: letter.numeroLettre || letter.numero_lettre,
    compte_tresor: letter.compteTresor || letter.compte_tresor,
    montant: letter.montant,
    devise: letter.devise,
    date_creation: letter.dateCreation || letter.date_creation || letter.dateTransmission || letter.date_transmission,
    pret_id: letter.pretId || letter.pret_id || letter.pret?.id,
    ordre_paiement_id: letter.ordrePaiementId || letter.ordre_paiement_id || letter.ordrePaiement?.id
  };
  
  // Flatten ordrePaiement if it exists
  if (letter.ordrePaiement) {
    flattened.ordrePaiement = {
      id: letter.ordrePaiement.id,
      numeroOrdre: letter.ordrePaiement.numeroOrdre,
      dateEmission: letter.ordrePaiement.dateEmission,
      montant: letter.ordrePaiement.montant,
      devise: letter.ordrePaiement.devise,
      echeance: letter.ordrePaiement.echeance,
      statut: letter.ordrePaiement.statut
      // Note: Not including the pret object to avoid circular references
    };
    
    // Flatten pret within ordrePaiement if it exists
    if (letter.ordrePaiement.pret) {
      flattened.ordrePaiement.pret = {
        id: letter.ordrePaiement.pret.id,
        numeroPret: letter.ordrePaiement.pret.numeroPret,
        dateSignature: letter.ordrePaiement.pret.dateSignature,
        organismeBailleur: letter.ordrePaiement.pret.organismeBailleur,
        objet: letter.ordrePaiement.pret.objet,
        montantTotal: letter.ordrePaiement.pret.montantTotal,
        soldeCourant: letter.ordrePaiement.pret.soldeCourant,
        devise: letter.ordrePaiement.pret.devise,
        duree: letter.ordrePaiement.pret.duree,
        tauxInteret: letter.ordrePaiement.pret.tauxInteret
        // Note: Not including echeanciers to avoid circular references
      };
    }
  }
  
  return flattened;
};

// Function to flatten an array of settlement letters
const flattenSettlementLetters = (letters) => {
  if (!Array.isArray(letters)) return [];
  return letters.map(flattenSettlementLetter).filter(letter => letter !== null);
};

const SettlementLettersList = () => {
  const [settlementLetters, setSettlementLetters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [currentLetter, setCurrentLetter] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchField, setSearchField] = useState('numero_lettre');
  const [openPdfViewer, setOpenPdfViewer] = useState(false);
  const [selectedLetterId, setSelectedLetterId] = useState(null);
  const [selectedLetterNumber, setSelectedLetterNumber] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    // Set auth token for API requests
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      // Only fetch data after setting the token
      fetchSettlementLetters();
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }
  }, [navigate]);

  const fetchSettlementLetters = async () => {
    try {
      setLoading(true);
      const data = await getAllSettlementLetters();
      // Ensure data is an array before setting state
      if (Array.isArray(data)) {
        // Flatten the data to remove circular references
        const flattenedData = flattenSettlementLetters(data);
        setSettlementLetters(flattenedData);
      } else {
        console.warn('Expected array but received:', data);
        setSettlementLetters([]);
      }
    } catch (error) {
      console.error('Error fetching settlement letters:', error);
      // Set to empty array on error to prevent map errors
      setSettlementLetters([]);
      alert('Erreur lors du chargement des lettres de règlement: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setCurrentLetter(null);
    setOpenDialog(true);
  };

  const handleEdit = (letter) => {
    setCurrentLetter(letter);
    setOpenDialog(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cette lettre de règlement ?')) {
      try {
        await deleteSettlementLetter(id);
        fetchSettlementLetters();
      } catch (error) {
        console.error('Error deleting settlement letter:', error);
        alert('Erreur lors de la suppression de la lettre de règlement: ' + error.message);
      }
    }
  };

  const handleGeneratePDF = async (id) => {
    try {
      const pdfBlob = await generateSettlementLetterPdf(id);
      const url = window.URL.createObjectURL(new Blob([pdfBlob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `lettre-reglement-${id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Error generating PDF:', error);
      // Provide more specific error messages based on the error type
      let errorMessage = error.message;
      if (errorMessage.includes('not found') || errorMessage.includes('404')) {
        errorMessage = 'Lettre de règlement non trouvée ou aucun ordre de paiement associé. Veuillez associer un ordre de paiement à cette lettre avant de générer le PDF.';
      } else if (errorMessage.includes('Internal server error') || errorMessage.includes('500')) {
        errorMessage = 'Erreur serveur lors de la génération du PDF. Veuillez réessayer ou contacter l\'administrateur.';
      } else if (errorMessage.includes('Network error')) {
        errorMessage = 'Erreur de communication avec le serveur. Veuillez vérifier votre connexion et réessayer.';
      }
      alert('Erreur lors de la génération du PDF: ' + errorMessage);
    }
  };

  const handleViewPDF = (letter) => {
    setSelectedLetterId(letter.id);
    setSelectedLetterNumber(letter.numero_lettre);
    setOpenPdfViewer(true);
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      fetchSettlementLetters();
      return;
    }
    
    try {
      setLoading(true);
      // Map frontend field names to backend field names
      const fieldMapping = {
        'numero_lettre': 'numero_lettre',
        'compte_tresor': 'compte_tresor',
        'devise': 'devise',
        'pret_id': 'pret_id'
      };
      
      const backendField = fieldMapping[searchField] || searchField;
      const filters = { [backendField]: searchTerm };
      
      console.log('Searching with filters:', filters);
      const data = await searchSettlementLetters(filters);
      
      // Ensure data is an array before setting state
      if (Array.isArray(data)) {
        // Flatten the data to remove circular references and map properties correctly
        const flattenedData = flattenSettlementLetters(data);
        setSettlementLetters(flattenedData);
      } else {
        console.warn('Expected array but received from search:', data);
        setSettlementLetters([]);
      }
    } catch (error) {
      console.error('Error searching settlement letters:', error);
      // Provide more specific error messages based on the error type
      let errorMessage = error.message;
      if (errorMessage.includes('Data inconsistency') || errorMessage.includes('Multiple records')) {
        errorMessage = 'Incohérence de données détectée lors de la recherche. Veuillez contacter l\'administrateur système.';
      } else if (errorMessage.includes('Network error')) {
        errorMessage = 'Erreur de communication avec le serveur. Veuillez vérifier votre connexion et réessayer.';
      } else {
        errorMessage = 'Erreur lors de la recherche des lettres de règlement: ' + errorMessage;
      }
      // Set to empty array on error to prevent map errors
      setSettlementLetters([]);
      alert(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (letterData) => {
    try {
      console.log('Submitting settlement letter data:', letterData);
      // Map frontend field names to backend DTO field names
      const mappedData = {
        numeroLettre: letterData.numero_lettre,
        compteTresor: letterData.compte_tresor,
        montant: parseFloat(letterData.montant),
        devise: letterData.devise,
        dateTransmission: letterData.date_creation,
        pretId: parseInt(letterData.pret_id, 10),
        ordrePaiementId: letterData.ordre_paiement_id ? parseInt(letterData.ordre_paiement_id, 10) : null
      };
      
      console.log('Mapped data for submission:', mappedData);
      
      if (currentLetter) {
        console.log('Updating existing settlement letter with ID:', currentLetter.id);
        await updateSettlementLetter(currentLetter.id, mappedData);
      } else {
        console.log('Creating new settlement letter');
        await createSettlementLetter(mappedData);
      }
      setOpenDialog(false);
      fetchSettlementLetters();
    } catch (error) {
      console.error('Error saving settlement letter:', error);
      // Provide more specific error messages based on the error type
      let errorMessage = error.message;
      if (errorMessage.includes('constraint') || errorMessage.includes('duplicate')) {
        errorMessage = 'Une lettre de règlement avec ce numéro existe déjà. Veuillez utiliser un numéro différent.';
      } else if (errorMessage.includes('not found') || errorMessage.includes('404')) {
        errorMessage = 'Lettre de règlement non trouvée. Elle peut avoir été supprimée par un autre utilisateur.';
      } else if (errorMessage.includes('Data inconsistency') || errorMessage.includes('Multiple records')) {
        errorMessage = 'Incohérence de données détectée. Veuillez contacter l\'administrateur système.';
      }
      alert('Erreur lors de l\'enregistrement de la lettre de règlement: ' + errorMessage);
    }
  };

  return (
    <Box>
      {/* Modern Header with Orange Background */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, p: 2, bgcolor: 'rgba(255, 107, 53, 0.1)', borderRadius: 2 }}>
        <Typography variant="h5" sx={{ color: '#333', fontWeight: 'bold' }}>Lettres de Règlement</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreate}
          sx={{ bgcolor: '#FF6B35', '&:hover': { bgcolor: '#E65A2B' } }}
        >
          Nouvelle Lettre
        </Button>
      </Box>

      {/* Enhanced Search Section with Modern Styling */}
      <Paper sx={{ p: 2, mb: 3, borderRadius: 2, boxShadow: '0 2px 10px rgba(0,0,0,0.05)' }}>
        <Grid container spacing={2} alignItems="center">
          <Grid size={{ xs: 12, md: 4 }}>
            <FormControl fullWidth>
              <InputLabel>Champ de recherche</InputLabel>
              <Select
                value={searchField}
                label="Champ de recherche"
                onChange={(e) => setSearchField(e.target.value)}
                sx={{ borderRadius: 2 }}
              >
                <MenuItem value="numero_lettre">Numéro de Lettre</MenuItem>
                <MenuItem value="compte_tresor">Compte Trésor</MenuItem>
                <MenuItem value="devise">Devise</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              placeholder="Entrez le terme de recherche..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton 
                      onClick={handleSearch}
                      sx={{ bgcolor: '#FF6B35', color: 'white', '&:hover': { bgcolor: '#E65A2B' } }}
                    >
                      <SearchIcon />
                    </IconButton>
                  </InputAdornment>
                ),
                sx: { borderRadius: 2 }
              }}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <Button
              fullWidth
              variant="outlined"
              onClick={() => {
                setSearchTerm('');
                fetchSettlementLetters();
              }}
              sx={{ borderRadius: 2, borderColor: '#FF6B35', color: '#FF6B35', '&:hover': { borderColor: '#E65A2B' } }}
            >
              Réinitialiser
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Modern Styled Table */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} sx={{ borderRadius: 2, boxShadow: '0 2px 10px rgba(0,0,0,0.05)' }}>
          <Table>
            <TableHead sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Numéro de Lettre</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Compte Trésor</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Création</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID Prêt</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID Ordre de Paiement</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {settlementLetters.map((letter, index) => (
                <TableRow 
                  key={letter.id}
                  sx={{
                    '&:nth-of-type(odd)': {
                      bgcolor: 'rgba(0, 0, 0, 0.02)'
                    },
                    '&:hover': {
                      bgcolor: 'rgba(255, 107, 53, 0.05)'
                    }
                  }}
                >
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{letter.numero_lettre}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{letter.compte_tresor}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{letter.montant?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{letter.devise}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{letter.date_creation ? new Date(letter.date_creation).toLocaleDateString('fr-FR') : ''}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{letter.pret_id}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{letter.ordre_paiement_id || 'N/A'}</TableCell>
                  <TableCell>
                    <IconButton 
                      onClick={() => handleEdit(letter)}
                      sx={{ color: '#FF6B35', '&:hover': { bgcolor: 'rgba(255, 107, 53, 0.1)' } }}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => handleDelete(letter.id)}
                      sx={{ color: '#f44336', '&:hover': { bgcolor: 'rgba(244, 67, 54, 0.1)' } }}
                    >
                      <DeleteIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => handleViewPDF(letter)}
                      sx={{ color: '#2196f3', '&:hover': { bgcolor: 'rgba(33, 150, 243, 0.1)' } }}
                    >
                      <ViewIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => handleGeneratePDF(letter.id)}
                      sx={{ color: '#f44336', '&:hover': { bgcolor: 'rgba(244, 67, 54, 0.1)' } }}
                    >
                      <PdfIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Settlement Letter Dialog */}
      <SettlementLetterDialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        letter={currentLetter}
        onSubmit={handleSubmit}
      />

      {/* PDF Viewer Dialog */}
      <PdfViewer
        open={openPdfViewer}
        onClose={() => setOpenPdfViewer(false)}
        letterId={selectedLetterId}
        letterNumber={selectedLetterNumber}
      />
    </Box>
  );
};

const SettlementLetterDialog = ({ open, onClose, letter, onSubmit }) => {
  const [formData, setFormData] = useState({
    numero_lettre: '',
    compte_tresor: '',
    montant: '',
    devise: 'MAD',
    date_creation: '',
    pret_id: '',
    ordre_paiement_id: ''  // Added this line
  });

  useEffect(() => {
    if (letter) {
      setFormData({
        numero_lettre: letter.numero_lettre || '',
        compte_tresor: letter.compte_tresor || '',
        montant: letter.montant || '',
        devise: letter.devise || 'MAD',
        date_creation: letter.date_creation ? new Date(letter.date_creation).toISOString().split('T')[0] : '',
        pret_id: letter.pret_id || '',
        ordre_paiement_id: letter.ordre_paiement_id || ''  // Added this line
      });
    } else {
      setFormData({
        numero_lettre: '',
        compte_tresor: '',
        montant: '',
        devise: 'MAD',
        date_creation: '',
        pret_id: '',
        ordre_paiement_id: ''  // Added this line
      });
    }
  }, [letter]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = () => {
    onSubmit(formData);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {letter ? 'Modifier Lettre de Règlement' : 'Nouvelle Lettre de Règlement'}
      </DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Numéro de Lettre"
              name="numero_lettre"
              value={formData.numero_lettre}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Compte Trésor"
              name="compte_tresor"
              value={formData.compte_tresor}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Montant"
              name="montant"
              type="number"
              value={formData.montant}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              select
              label="Devise"
              name="devise"
              value={formData.devise}
              onChange={handleChange}
              required
            >
              <MenuItem value="MAD">MAD</MenuItem>
              <MenuItem value="EUR">EUR</MenuItem>
              <MenuItem value="USD">USD</MenuItem>
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Date de Création"
              name="date_creation"
              type="date"
              value={formData.date_creation}
              onChange={handleChange}
              InputLabelProps={{
                shrink: true,
              }}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="ID Prêt"
              name="pret_id"
              type="number"
              value={formData.pret_id}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="ID Ordre de Paiement (optionnel)"
              name="ordre_paiement_id"
              type="number"
              value={formData.ordre_paiement_id}
              onChange={handleChange}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Annuler</Button>
        <Button onClick={handleSubmit} variant="contained" color="primary">
          {letter ? 'Modifier' : 'Créer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default SettlementLettersList;