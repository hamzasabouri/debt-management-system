import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
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
import { useNavigate } from 'react-router-dom';
import { getAllEcheanciers, createEcheancier, updateEcheancier, deleteEcheancier, searchEcheanciers, setAuthToken } from '../../services/dette-tresor/echeanciersService';

const EcheanciersList = () => {
  const [echeanciers, setEcheanciers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [currentEcheancier, setCurrentEcheancier] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchField, setSearchField] = useState('numeroEcheance');
  const navigate = useNavigate();

  useEffect(() => {
    // Set auth token for API requests
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      // Only fetch data after setting the token
      fetchEcheanciers();
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }
  }, [navigate]);

  const fetchEcheanciers = async () => {
    try {
      setLoading(true);
      const data = await getAllEcheanciers();
      setEcheanciers(data);
    } catch (error) {
      console.error('Error fetching echeanciers:', error);
      alert('Erreur lors du chargement des échéanciers: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setCurrentEcheancier(null);
    setOpenDialog(true);
  };

  const handleEdit = (echeancier) => {
    setCurrentEcheancier(echeancier);
    setOpenDialog(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet échéancier ?')) {
      try {
        await deleteEcheancier(id);
        fetchEcheanciers();
      } catch (error) {
        console.error('Error deleting echeancier:', error);
        alert('Erreur lors de la suppression de l\'échéancier: ' + error.message);
      }
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      fetchEcheanciers();
      return;
    }
    
    try {
      setLoading(true);
      const filters = {};
      if (searchField === 'numeroEcheance') {
        filters.numeroEcheance = searchTerm;
      } else if (searchField === 'pretId') {
        filters.pretId = searchTerm;
      } else if (searchField === 'statut') {
        filters.statut = searchTerm;
      }
      const data = await searchEcheanciers(filters);
      setEcheanciers(data);
    } catch (error) {
      console.error('Error searching echeanciers:', error);
      alert('Erreur lors de la recherche des échéanciers: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (echeancierData) => {
    try {
      if (currentEcheancier) {
        await updateEcheancier(currentEcheancier.id, echeancierData);
      } else {
        await createEcheancier(echeancierData);
      }
      setOpenDialog(false);
      fetchEcheanciers();
    } catch (error) {
      console.error('Error saving echeancier:', error);
      alert('Erreur lors de l\'enregistrement de l\'échéancier: ' + error.message);
    }
  };

  return (
    <Box>
      {/* Modern Header with Orange Background */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, p: 2, bgcolor: 'rgba(255, 107, 53, 0.1)', borderRadius: 2 }}>
        <Typography variant="h5" sx={{ color: '#333', fontWeight: 'bold' }}>Échéanciers</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreate}
          sx={{ bgcolor: '#FF6B35', '&:hover': { bgcolor: '#E65A2B' } }}
        >
          Nouvel Échéancier
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
                <MenuItem value="numeroEcheance">Numéro d'Échéance</MenuItem>
                <MenuItem value="statut">Statut</MenuItem>
                <MenuItem value="pretId">ID Prêt</MenuItem>
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
                fetchEcheanciers();
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
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID Prêt</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Numéro d'Échéance</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date d'Échéance</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Capital</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Intérêt</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Commission</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant Total</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID Ordre de Paiement</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {echeanciers.map((echeancier, index) => (
                <TableRow 
                  key={echeancier.id}
                  sx={{
                    '&:nth-of-type(odd)': {
                      bgcolor: 'rgba(0, 0, 0, 0.02)'
                    },
                    '&:hover': {
                      bgcolor: 'rgba(255, 107, 53, 0.05)'
                    }
                  }}
                >
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.id}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.pretId}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.numeroEcheance}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.dateEcheance ? new Date(echeancier.dateEcheance).toLocaleDateString('fr-FR') : ''}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.capital?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.interet?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.commission?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.montantTotal?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.statut}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{echeancier.ordrePaiementId || 'N/A'}</TableCell>
                  <TableCell>
                    <IconButton 
                      onClick={() => handleEdit(echeancier)}
                      sx={{ color: '#FF6B35', '&:hover': { bgcolor: 'rgba(255, 107, 53, 0.1)' } }}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => handleDelete(echeancier.id)}
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

      {/* Echeancier Dialog */}
      <EcheancierDialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        echeancier={currentEcheancier}
        onSubmit={handleSubmit}
      />
    </Box>
  );
};

const EcheancierDialog = ({ open, onClose, echeancier, onSubmit }) => {
  const [formData, setFormData] = useState({
    pretId: '',
    numeroEcheance: '',
    dateEcheance: '',
    capital: '',
    interet: '',
    commission: '',
    montantTotal: '',
    statut: 'PREVU',
    ordrePaiementId: ''
  });

  useEffect(() => {
    if (echeancier) {
      setFormData({
        pretId: echeancier.pretId || '',
        numeroEcheance: echeancier.numeroEcheance || '',
        dateEcheance: echeancier.dateEcheance ? new Date(echeancier.dateEcheance).toISOString().split('T')[0] : '',
        capital: echeancier.capital || '',
        interet: echeancier.interet || '',
        commission: echeancier.commission || '',
        montantTotal: echeancier.montantTotal || '',
        statut: echeancier.statut || 'PREVU',
        ordrePaiementId: echeancier.ordrePaiementId || ''
      });
    } else {
      setFormData({
        pretId: '',
        numeroEcheance: '',
        dateEcheance: '',
        capital: '',
        interet: '',
        commission: '',
        montantTotal: '',
        statut: 'PREVU',
        ordrePaiementId: ''
      });
    }
  }, [echeancier]);

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
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {echeancier ? 'Modifier l\'Échéancier' : 'Nouvel Échéancier'}
      </DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
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
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Numéro d'Échéance"
              name="numeroEcheance"
              type="number"
              value={formData.numeroEcheance}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Date d'Échéance"
              name="dateEcheance"
              type="date"
              value={formData.dateEcheance}
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
              select
              label="Statut"
              name="statut"
              value={formData.statut}
              onChange={handleChange}
              required
            >
              <MenuItem value="PREVU">Prévu</MenuItem>
              <MenuItem value="PAYE">Payé</MenuItem>
              <MenuItem value="PARTIELLEMENT_PAYE">Partiellement Payé</MenuItem>
              <MenuItem value="EN_RETARD">En Retard</MenuItem>
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Capital"
              name="capital"
              type="number"
              value={formData.capital}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Intérêt"
              name="interet"
              type="number"
              value={formData.interet}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Commission"
              name="commission"
              type="number"
              value={formData.commission}
              onChange={handleChange}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Montant Total"
              name="montantTotal"
              type="number"
              value={formData.montantTotal}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="ID Ordre de Paiement (optionnel)"
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
          {echeancier ? 'Modifier' : 'Créer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default EcheanciersList;