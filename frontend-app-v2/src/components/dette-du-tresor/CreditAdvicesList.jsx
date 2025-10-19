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
import { getAllCreditAdvices, createCreditAdvice, updateCreditAdvice, deleteCreditAdvice, searchCreditAdvices, setAuthToken } from '../../services/dette-tresor/creditAdvicesService';

const CreditAdvicesList = () => {
  const [creditAdvices, setCreditAdvices] = useState([]);
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
      fetchCreditAdvices();
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }
  }, [navigate]);

  const fetchCreditAdvices = async () => {
    try {
      setLoading(true);
      const data = await getAllCreditAdvices();
      setCreditAdvices(data);
    } catch (error) {
      console.error('Error fetching credit advices:', error);
      alert('Erreur lors du chargement des avis de crédit: ' + error.message);
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
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet avis de crédit ?')) {
      try {
        await deleteCreditAdvice(id);
        fetchCreditAdvices();
      } catch (error) {
        console.error('Error deleting credit advice:', error);
        alert('Erreur lors de la suppression de l\'avis de crédit: ' + error.message);
      }
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      fetchCreditAdvices();
      return;
    }
    
    try {
      setLoading(true);
      const data = await searchCreditAdvices(searchField, searchTerm);
      setCreditAdvices(data);
    } catch (error) {
      console.error('Error searching credit advices:', error);
      alert('Erreur lors de la recherche des avis de crédit: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (adviceData) => {
    try {
      if (currentAdvice) {
        await updateCreditAdvice(currentAdvice.id, adviceData);
      } else {
        await createCreditAdvice(adviceData);
      }
      setOpenDialog(false);
      fetchCreditAdvices();
    } catch (error) {
      console.error('Error saving credit advice:', error);
      alert('Erreur lors de l\'enregistrement de l\'avis de crédit: ' + error.message);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, p: 2, bgcolor: 'rgba(255, 107, 53, 0.1)', borderRadius: 2 }}>
        <Typography variant="h5" sx={{ color: '#333', fontWeight: 'bold' }}>Avis de Crédit</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreate}
          sx={{ bgcolor: '#FF6B35', '&:hover': { bgcolor: '#E65A2B' } }}
        >
          Nouvel Avis
        </Button>
      </Box>

      {/* Search Section */}
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
                <MenuItem value="emetteur">Émetteur</MenuItem>
                <MenuItem value="typeAvis">Type d'Avis</MenuItem>
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
                fetchCreditAdvices();
              }}
              sx={{ borderRadius: 2, borderColor: '#FF6B35', color: '#FF6B35', '&:hover': { borderColor: '#E65A2B' } }}
            >
              Réinitialiser
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Credit Advices Table */}
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
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Émetteur</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Type d'Avis</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Réception</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID Prêt</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {creditAdvices.map((advice, index) => (
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
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.emetteur}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.typeAvis}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.montant?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.devise}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.dateReception ? new Date(advice.dateReception).toLocaleDateString('fr-FR') : ''}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{advice.pretId}</TableCell>
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

      {/* Credit Advice Dialog */}
      <CreditAdviceDialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        advice={currentAdvice}
        onSubmit={handleSubmit}
      />
    </Box>
  );
};

const CreditAdviceDialog = ({ open, onClose, advice, onSubmit }) => {
  const [formData, setFormData] = useState({
    numeroAvis: '',
    emetteur: '',
    typeAvis: '',
    montant: '',
    devise: 'MAD',
    dateReception: '',
    pretId: ''
  });

  useEffect(() => {
    if (advice) {
      setFormData({
        numeroAvis: advice.numeroAvis || '',
        emetteur: advice.emetteur || '',
        typeAvis: advice.typeAvis || '',
        montant: advice.montant || '',
        devise: advice.devise || 'MAD',
        dateReception: advice.dateReception ? new Date(advice.dateReception).toISOString().split('T')[0] : '',
        pretId: advice.pretId || ''
      });
    } else {
      setFormData({
        numeroAvis: '',
        emetteur: '',
        typeAvis: '',
        montant: '',
        devise: 'MAD',
        dateReception: '',
        pretId: ''
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
        {advice ? 'Modifier Avis de Crédit' : 'Nouvel Avis de Crédit'}
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
              label="Émetteur"
              name="emetteur"
              value={formData.emetteur}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              select
              label="Type d'Avis"
              name="typeAvis"
              value={formData.typeAvis}
              onChange={handleChange}
              required
            >
              <MenuItem value="VERSEMENT_CREANCIER">Versement Créancier</MenuItem>
              <MenuItem value="AVIS_REGLEMENT">Avis Règlement</MenuItem>
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

export default CreditAdvicesList;