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
import { getAllLoans, createLoan, updateLoan, deleteLoan, searchLoans, setAuthToken } from '../../services/dette-tresor/loansService';

const LoansList = () => {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [currentLoan, setCurrentLoan] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchField, setSearchField] = useState('numeroPret');
  const navigate = useNavigate();

  useEffect(() => {
    // Set auth token for API requests
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      // Only fetch loans after setting the token
      fetchLoans();
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }
  }, [navigate]);

  const fetchLoans = async () => {
    try {
      setLoading(true);
      const data = await getAllLoans();
      setLoans(data);
    } catch (error) {
      console.error('Error fetching loans:', error);
      // Show error to user
      alert('Erreur lors du chargement des prêts: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setCurrentLoan(null);
    setOpenDialog(true);
  };

  const handleEdit = (loan) => {
    setCurrentLoan(loan);
    setOpenDialog(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce prêt ?')) {
      try {
        await deleteLoan(id);
        fetchLoans();
      } catch (error) {
        console.error('Error deleting loan:', error);
        alert('Erreur lors de la suppression du prêt: ' + error.message);
      }
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      fetchLoans();
      return;
    }
    
    try {
      setLoading(true);
      const filters = { [searchField]: searchTerm };
      const data = await searchLoans(filters);
      setLoans(data);
    } catch (error) {
      console.error('Error searching loans:', error);
      alert('Erreur lors de la recherche des prêts: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (loanData) => {
    try {
      if (currentLoan) {
        await updateLoan(currentLoan.id, loanData);
      } else {
        await createLoan(loanData);
      }
      setOpenDialog(false);
      fetchLoans();
    } catch (error) {
      console.error('Error saving loan:', error);
      alert('Erreur lors de l\'enregistrement du prêt: ' + error.message);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, p: 2, bgcolor: 'rgba(255, 107, 53, 0.1)', borderRadius: 2 }}>
        <Typography variant="h5" sx={{ color: '#333', fontWeight: 'bold' }}>Gestion des Prêts</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreate}
          sx={{ bgcolor: '#FF6B35', '&:hover': { bgcolor: '#E65A2B' } }}
        >
          Nouveau Prêt
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
                <MenuItem value="numeroPret">Numéro de Prêt</MenuItem>
                <MenuItem value="organismeBailleur">Organisme Bailleur</MenuItem>
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
                fetchLoans();
              }}
              sx={{ borderRadius: 2, borderColor: '#FF6B35', color: '#FF6B35', '&:hover': { borderColor: '#E65A2B' } }}
            >
              Réinitialiser
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Loans Table */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} sx={{ borderRadius: 2, boxShadow: '0 2px 10px rgba(0,0,0,0.05)' }}>
          <Table>
            <TableHead sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Numéro</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Organisme Bailleur</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant Total</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Solde Courant</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Signature</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Durée (mois)</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Taux d'Intérêt</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loans.map((loan, index) => (
                <TableRow 
                  key={loan.id}
                  sx={{
                    '&:nth-of-type(odd)': {
                      bgcolor: 'rgba(0, 0, 0, 0.02)'
                    },
                    '&:hover': {
                      bgcolor: 'rgba(255, 107, 53, 0.05)'
                    }
                  }}
                >
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{loan.numeroPret}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{loan.organismeBailleur}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{loan.montantTotal?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{loan.soldeCourant?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{loan.devise}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{loan.dateSignature ? new Date(loan.dateSignature).toLocaleDateString('fr-FR') : ''}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{loan.duree}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{loan.tauxInteret}%</TableCell>
                  <TableCell>
                    <IconButton 
                      onClick={() => handleEdit(loan)}
                      sx={{ color: '#FF6B35', '&:hover': { bgcolor: 'rgba(255, 107, 53, 0.1)' } }}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => handleDelete(loan.id)}
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

      {/* Loan Dialog */}
      <LoanDialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        loan={currentLoan}
        onSubmit={handleSubmit}
      />
    </Box>
  );
};

const LoanDialog = ({ open, onClose, loan, onSubmit }) => {
  const [formData, setFormData] = useState({
    numeroPret: '',
    dateSignature: '',
    organismeBailleur: '',
    objet: '',
    montantTotal: '',
    soldeCourant: '',
    devise: 'MAD',
    duree: '',
    tauxInteret: ''
  });

  useEffect(() => {
    if (loan) {
      setFormData({
        numeroPret: loan.numeroPret || '',
        dateSignature: loan.dateSignature ? new Date(loan.dateSignature).toISOString().split('T')[0] : '',
        organismeBailleur: loan.organismeBailleur || '',
        objet: loan.objet || '',
        montantTotal: loan.montantTotal || '',
        soldeCourant: loan.soldeCourant || '',
        devise: loan.devise || 'MAD',
        duree: loan.duree || '',
        tauxInteret: loan.tauxInteret || ''
      });
    } else {
      setFormData({
        numeroPret: '',
        dateSignature: '',
        organismeBailleur: '',
        objet: '',
        montantTotal: '',
        soldeCourant: '',
        devise: 'MAD',
        duree: '',
        tauxInteret: ''
      });
    }
  }, [loan]);

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
        {loan ? 'Modifier le Prêt' : 'Nouveau Prêt'}
      </DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Numéro de Prêt"
              name="numeroPret"
              value={formData.numeroPret}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Organisme Bailleur"
              name="organismeBailleur"
              value={formData.organismeBailleur}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={12}>
            <TextField
              fullWidth
              label="Objet"
              name="objet"
              value={formData.objet}
              onChange={handleChange}
              multiline
              rows={3}
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
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Solde Courant"
              name="soldeCourant"
              type="number"
              value={formData.soldeCourant}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <FormControl fullWidth>
              <InputLabel>Devise</InputLabel>
              <Select
                name="devise"
                value={formData.devise}
                label="Devise"
                onChange={handleChange}
              >
                <MenuItem value="MAD">MAD</MenuItem>
                <MenuItem value="EUR">EUR</MenuItem>
                <MenuItem value="USD">USD</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Durée (mois)"
              name="duree"
              type="number"
              value={formData.duree}
              onChange={handleChange}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Date de Signature"
              name="dateSignature"
              type="date"
              value={formData.dateSignature}
              onChange={handleChange}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              label="Taux d'Intérêt (%)"
              name="tauxInteret"
              type="number"
              value={formData.tauxInteret}
              onChange={handleChange}
              required
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Annuler</Button>
        <Button onClick={handleSubmit} variant="contained" color="primary">
          {loan ? 'Modifier' : 'Créer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default LoansList;