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
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { getAllDebitAdvices, createDebitAdvice, updateDebitAdvice, deleteDebitAdvice, searchDebitAdvices, setAuthToken } from '../../services/dette-tresor/debitAdvicesService';

const DebitAdvicesList = () => {
  const [debitAdvices, setDebitAdvices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [currentAdvice, setCurrentAdvice] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchField, setSearchField] = useState('numeroAvis');
  const navigate = useNavigate();

  useEffect(() => {
    // Set auth token for API requests
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      // Only fetch data after setting the token
      fetchDebitAdvices();
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }
  }, [navigate]);

  const fetchDebitAdvices = async () => {
    try {
      setLoading(true);
      const data = await getAllDebitAdvices();
      setDebitAdvices(data);
    } catch (error) {
      console.error('Error fetching debit advices:', error);
      alert('Erreur lors du chargement des avis de débit: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setCurrentAdvice(null);
    setOpenDialog(true);
  };

  const handleEdit = (advice) => {
    setCurrentAdvice(advice);
    setOpenDialog(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet avis de débit ?')) {
      try {
        await deleteDebitAdvice(id);
        fetchDebitAdvices();
      } catch (error) {
        console.error('Error deleting debit advice:', error);
        alert('Erreur lors de la suppression de l\'avis de débit: ' + error.message);
      }
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      fetchDebitAdvices();
      return;
    }
    
    try {
      setLoading(true);
      const data = await searchDebitAdvices(searchField, searchTerm);
      setDebitAdvices(data);
    } catch (error) {
      console.error('Error searching debit advices:', error);
      alert('Erreur lors de la recherche des avis de débit: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (adviceData) => {
    try {
      if (currentAdvice) {
        await updateDebitAdvice(currentAdvice.id, adviceData);
      } else {
        await createDebitAdvice(adviceData);
      }
      setOpenDialog(false);
      fetchDebitAdvices();
    } catch (error) {
      console.error('Error saving debit advice:', error);
      alert('Erreur lors de l\'enregistrement de l\'avis de débit: ' + error.message);
    }
  };

  return (
    <Box>
      {/* Modern Header with Orange Background */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, p: 2, bgcolor: 'rgba(255, 107, 53, 0.1)', borderRadius: 2 }}>
        <Typography variant="h5" sx={{ color: '#333', fontWeight: 'bold' }}>Avis de Débit</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreate}
          sx={{ bgcolor: '#FF6B35', '&:hover': { bgcolor: '#E65A2B' } }}
        >
          Nouvel Avis
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
                <MenuItem value="numeroAvis">Numéro d'Avis</MenuItem>
                <MenuItem value="motif">Motif</MenuItem>
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
                fetchDebitAdvices();
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
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Numéro d'Avis</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Motif</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Réception</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID Prêt</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID Ordre de Paiement</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {debitAdvices.map((advice, index) => (
                <TableRow 
                  key={advice.id}
                  sx={{
                    '&:nth-of-type(odd)': {
                      bgcolor: 'rgba(0, 0, 0, 0.02)'
                    },
                    '&:hover': {
                      bgcolor: 'rgba(255, 107, 53, 0.05)'
                    }
                  }}
                >
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.numeroAvis}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.motif}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.montant?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.devise}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.dateReception ? new Date(advice.dateReception).toLocaleDateString('fr-FR') : ''}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.pretId}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.ordrePaiementId || 'N/A'}</TableCell>
                  <TableCell>
                    <IconButton 
                      onClick={() => handleEdit(advice)}
                      sx={{ color: '#FF6B35', '&:hover': { bgcolor: 'rgba(255, 107, 53, 0.1)' } }}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => handleDelete(advice.id)}
                      sx={{ color: '#f44336', '&:hover': { bgcolor: 'rgba(244, 67, 54, 0.1)' } }}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Debit Advice Dialog */}
      <DebitAdviceDialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        advice={currentAdvice}
        onSubmit={handleSubmit}
      />
    </Box>
  );
};

const DebitAdviceDialog = ({ open, onClose, advice, onSubmit }) => {
  const [formData, setFormData] = useState({
    numeroAvis: '',
    motif: '',
    montant: '',
    devise: 'MAD',
    dateReception: '',
    pretId: '',
    ordrePaiementId: ''
  });

  useEffect(() => {
    if (advice) {
      setFormData({
        numeroAvis: advice.numeroAvis || '',
        motif: advice.motif || '',
        montant: advice.montant || '',
        devise: advice.devise || 'MAD',
        dateReception: advice.dateReception ? new Date(advice.dateReception).toISOString().split('T')[0] : '',
        pretId: advice.pretId || '',
        ordrePaiementId: advice.ordrePaiementId || ''
      });
    } else {
      setFormData({
        numeroAvis: '',
        motif: '',
        montant: '',
        devise: 'MAD',
        dateReception: '',
        pretId: '',
        ordrePaiementId: ''
      });
    }
  }, [advice]);

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
        {advice ? 'Modifier Avis de Débit' : 'Nouvel Avis de Débit'}
      </DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Numéro d'Avis"
              name="numeroAvis"
              value={formData.numeroAvis}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              select
              label="Motif"
              name="motif"
              value={formData.motif}
              onChange={handleChange}
              required
            >
              <MenuItem value="REMBOURSEMENT">Remboursement</MenuItem>
              <MenuItem value="FRAIS_TRANSFERT">Frais de Transfert</MenuItem>
              <MenuItem value="AIDE_BALANCE">Aide Balance</MenuItem>
            </TextField>
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
              label="Date de Réception"
              name="dateReception"
              type="date"
              value={formData.dateReception}
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
              name="pretId"
              type="number"
              value={formData.pretId}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="ID Ordre de Paiement (Optionnel)"
              name="ordrePaiementId"
              type="number"
              value={formData.ordrePaiementId}
              onChange={handleChange}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Annuler</Button>
        <Button onClick={handleSubmit} variant="contained" color="primary">
          {advice ? 'Modifier' : 'Créer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DebitAdvicesList;