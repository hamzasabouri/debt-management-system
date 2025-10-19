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
  Typography,
  Chip
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
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }
    
    fetchLoans();
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
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#333', fontWeight: 'bold' }}>
            Gestion des Prêts
          </Typography>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreate}
            sx={{ 
              bgcolor: '#FF6B35',
              '&:hover': {
                bgcolor: '#e55a2b'
              },
              borderRadius: 2,
              boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)'
            }}
          >
            Nouveau Prêt
          </Button>
        </Grid>
      </Grid>

      {/* Search Section */}
      <Paper sx={{ p: 2, mb: 3, borderRadius: 2, border: '1px solid #e0e0e0' }}>
        <Grid container spacing={2} alignItems="center">
          <Grid size={{ xs: 12, md: 4 }}>
            <Box>
              <Typography variant="body2" sx={{ mb: 1 }}>Champ de recherche</Typography>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <Select
                  value={searchField}
                  onChange={(e) => setSearchField(e.target.value)}
                  displayEmpty
                  sx={{ borderRadius: 2 }}
                  renderValue={(value) => value === 'numeroPret' ? 'Numéro de Prêt' : value === 'organismeBailleur' ? 'Organisme Bailleur' : 'Devise'}
                >
                  <MenuItem value="numeroPret">Numéro de Prêt</MenuItem>
                  <MenuItem value="organismeBailleur">Organisme Bailleur</MenuItem>
                  <MenuItem value="devise">Devise</MenuItem>
                </Select>
              </FormControl>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              placeholder="Entrez le terme de recherche..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              sx={{ mt: 3, borderRadius: 2 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: '#FF6B35' }} />
                  </InputAdornment>
                ),
                sx: { borderRadius: 2 }
              }}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
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
              sx={{ 
                mt: 3, 
                color: '#FF6B35', 
                borderColor: '#FF6B35', 
                '&:hover': { 
                  borderColor: '#C74A00',
                  bgcolor: 'rgba(255, 107, 53, 0.05)'
                },
                borderRadius: 2
              }}
            >
              Réinitialiser
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Loans Table */}
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Numéro</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Organisme Bailleur</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant Total</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Signature</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : loans.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  Aucun prêt trouvé
                </TableCell>
              </TableRow>
            ) : (
              loans.map((loan) => (
                <TableRow 
                  key={loan.id}
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
                  <TableCell>{loan.numeroPret}</TableCell>
                  <TableCell>{loan.organismeBailleur}</TableCell>
                  <TableCell>{parseFloat(loan.montantTotal).toLocaleString()} MAD</TableCell>
                  <TableCell>{loan.devise}</TableCell>
                  <TableCell>{new Date(loan.dateSignature).toLocaleDateString('fr-FR')}</TableCell>
                  <TableCell>
                    <Chip 
                      label={loan.statut} 
                      size="small"
                      sx={{
                        bgcolor: loan.statut === 'ACTIF' ? '#4caf50' : '#f44336',
                        color: 'white',
                        fontWeight: 'bold',
                        borderRadius: 1
                      }}
                    />
                  </TableCell>
                  <TableCell>
                    <IconButton 
                      size="small" 
                      onClick={() => handleEdit(loan)}
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
                      onClick={() => handleDelete(loan.id)}
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
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Loan Form Dialog */}
      <LoanFormDialog 
        open={openDialog} 
        onClose={() => setOpenDialog(false)} 
        loan={currentLoan} 
        onSubmit={handleSubmit} 
      />
    </Box>
  );
};

// Loan Form Dialog Component
const LoanFormDialog = ({ open, onClose, loan, onSubmit }) => {
  const [formData, setFormData] = useState({
    numeroPret: '',
    organismeBailleur: '',
    montantTotal: '',
    devise: 'MAD',
    dateSignature: '',
    statut: 'ACTIF'
  });

  useEffect(() => {
    if (loan) {
      setFormData({
        numeroPret: loan.numeroPret || '',
        organismeBailleur: loan.organismeBailleur || '',
        montantTotal: loan.montantTotal || '',
        devise: loan.devise || 'MAD',
        dateSignature: loan.dateSignature || '',
        statut: loan.statut || 'ACTIF'
      });
    } else {
      setFormData({
        numeroPret: '',
        organismeBailleur: '',
        montantTotal: '',
        devise: 'MAD',
        dateSignature: '',
        statut: 'ACTIF'
      });
    }
  }, [loan]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
        {loan ? 'Modifier Prêt' : 'Nouveau Prêt'}
      </DialogTitle>
      <DialogContent sx={{ mt: 2 }}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Numéro de Prêt"
                name="numeroPret"
                value={formData.numeroPret}
                onChange={handleChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Organisme Bailleur"
                name="organismeBailleur"
                value={formData.organismeBailleur}
                onChange={handleChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Montant Total"
                name="montantTotal"
                type="number"
                value={formData.montantTotal}
                onChange={handleChange}
                required
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Devise</InputLabel>
                <Select
                  name="devise"
                  value={formData.devise}
                  onChange={handleChange}
                  label="Devise"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="MAD">MAD</MenuItem>
                  <MenuItem value="EUR">EUR</MenuItem>
                  <MenuItem value="USD">USD</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date de Signature"
                name="dateSignature"
                type="date"
                value={formData.dateSignature}
                onChange={handleChange}
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
                  value={formData.statut}
                  onChange={handleChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="ACTIF">Actif</MenuItem>
                  <MenuItem value="INACTIF">Inactif</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </form>
      </DialogContent>
      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} sx={{ borderRadius: 2 }}>
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
          {loan ? 'Modifier' : 'Créer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default LoansList;