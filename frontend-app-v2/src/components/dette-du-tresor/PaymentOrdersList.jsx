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
import { getAllPaymentOrders, createPaymentOrder, updatePaymentOrder, deletePaymentOrder, searchPaymentOrders, setAuthToken } from '../../services/dette-tresor/paymentOrdersService';

const PaymentOrdersList = () => {
  const [paymentOrders, setPaymentOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [currentOrder, setCurrentOrder] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchField, setSearchField] = useState('numero_ordre');
  const navigate = useNavigate();

  useEffect(() => {
    // Set auth token for API requests
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      // Only fetch data after setting the token
      fetchPaymentOrders();
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }
  }, [navigate]);

  const fetchPaymentOrders = async () => {
    try {
      setLoading(true);
      const data = await getAllPaymentOrders();
      setPaymentOrders(data);
    } catch (error) {
      console.error('Error fetching payment orders:', error);
      alert('Erreur lors du chargement des ordres de paiement: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setCurrentOrder(null);
    setOpenDialog(true);
  };

  const handleEdit = (order) => {
    setCurrentOrder(order);
    setOpenDialog(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet ordre de paiement ?')) {
      try {
        await deletePaymentOrder(id);
        fetchPaymentOrders();
      } catch (error) {
        console.error('Error deleting payment order:', error);
        alert('Erreur lors de la suppression de l\'ordre de paiement: ' + error.message);
      }
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      fetchPaymentOrders();
      return;
    }
    
    try {
      setLoading(true);
      // Map frontend field names to backend field names and ensure proper capitalization
      let filters = {};
      if (searchField === 'statut') {
        // Ensure status values are in uppercase to match backend enum
        filters[searchField] = searchTerm.toUpperCase();
      } else {
        filters[searchField] = searchTerm;
      }
      const data = await searchPaymentOrders(filters);
      setPaymentOrders(data);
    } catch (error) {
      console.error('Error searching payment orders:', error);
      alert('Erreur lors de la recherche des ordres de paiement: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (orderData) => {
    try {
      // Map frontend field names to backend DTO field names
      const mappedData = {
        numeroOrdre: orderData.numero_ordre,
        montant: parseFloat(orderData.montant),
        devise: orderData.devise,
        dateEmission: orderData.date_emission,
        echeance: orderData.echeance,
        statut: orderData.statut,
        pretId: parseInt(orderData.pret_id, 10)
      };
      
      if (currentOrder) {
        await updatePaymentOrder(currentOrder.id, mappedData);
      } else {
        await createPaymentOrder(mappedData);
      }
      setOpenDialog(false);
      fetchPaymentOrders();
    } catch (error) {
      console.error('Error saving payment order:', error);
      alert('Erreur lors de l\'enregistrement de l\'ordre de paiement: ' + error.message);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, p: 2, bgcolor: 'rgba(255, 107, 53, 0.1)', borderRadius: 2 }}>
        <Typography variant="h5" sx={{ color: '#333', fontWeight: 'bold' }}>Ordres de Paiement</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreate}
          sx={{ bgcolor: '#FF6B35', '&:hover': { bgcolor: '#E65A2B' } }}
        >
          Nouvel Ordre
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
                <MenuItem value="numero_ordre">Numéro d'Ordre</MenuItem>
                <MenuItem value="statut">Statut</MenuItem>
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
                fetchPaymentOrders();
              }}
              sx={{ borderRadius: 2, borderColor: '#FF6B35', color: '#FF6B35', '&:hover': { borderColor: '#E65A2B' } }}
            >
              Réinitialiser
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Payment Orders Table */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} sx={{ borderRadius: 2, boxShadow: '0 2px 10px rgba(0,0,0,0.05)' }}>
          <Table>
            <TableHead sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Numéro d'Ordre</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date d'Émission</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Échéance</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>ID Prêt</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paymentOrders.map((order, index) => (
                <TableRow 
                  key={order.id}
                  sx={{
                    '&:nth-of-type(odd)': {
                      bgcolor: 'rgba(0, 0, 0, 0.02)'
                    },
                    '&:hover': {
                      bgcolor: 'rgba(255, 107, 53, 0.05)'
                    }
                  }}
                >
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{order.numeroOrdre || order.numero_ordre}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{order.montant?.toLocaleString('fr-FR')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{order.devise}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{order.dateEmission ? new Date(order.dateEmission).toLocaleDateString('fr-FR') : (order.date_emission ? new Date(order.date_emission).toLocaleDateString('fr-FR') : '')}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{order.echeance ? new Date(order.echeance).toLocaleDateString('fr-FR') : ''}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{order.statut}</TableCell>
                  <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{order.pretId || order.pret_id}</TableCell>
                  <TableCell>
                    <IconButton 
                      onClick={() => handleEdit(order)}
                      sx={{ color: '#FF6B35', '&:hover': { bgcolor: 'rgba(255, 107, 53, 0.1)' } }}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      onClick={() => handleDelete(order.id)}
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

      {/* Payment Order Dialog */}
      <PaymentOrderDialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        order={currentOrder}
        onSubmit={handleSubmit}
      />
    </Box>
  );
};

const PaymentOrderDialog = ({ open, onClose, order, onSubmit }) => {
  const [formData, setFormData] = useState({
    numero_ordre: '',
    montant: '',
    devise: 'MAD',
    date_emission: '',
    echeance: '',
    statut: 'EN_ATTENTE',
    pret_id: ''
  });

  useEffect(() => {
    if (order) {
      setFormData({
        numero_ordre: order.numeroOrdre || order.numero_ordre || '',
        montant: order.montant || '',
        devise: order.devise || 'MAD',
        date_emission: order.dateEmission ? new Date(order.dateEmission).toISOString().split('T')[0] : 
                     (order.date_emission ? new Date(order.date_emission).toISOString().split('T')[0] : ''),
        echeance: order.echeance ? new Date(order.echeance).toISOString().split('T')[0] : '',
        statut: order.statut || 'EN_ATTENTE',
        pret_id: order.pretId || order.pret_id || ''
      });
    } else {
      setFormData({
        numero_ordre: '',
        montant: '',
        devise: 'MAD',
        date_emission: '',
        echeance: '',
        statut: 'EN_ATTENTE',
        pret_id: ''
      });
    }
  }, [order]);

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
        {order ? 'Modifier Ordre de Paiement' : 'Nouvel Ordre de Paiement'}
      </DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Numéro d'Ordre"
              name="numero_ordre"
              value={formData.numero_ordre}
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
              label="Date d'Émission"
              name="date_emission"
              type="date"
              value={formData.date_emission}
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
              label="Échéance"
              name="echeance"
              type="date"
              value={formData.echeance}
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
              <MenuItem value="EN_ATTENTE">En Attente</MenuItem>
              <MenuItem value="PRIS_EN_CHARGE">Pris en Charge</MenuItem>
              <MenuItem value="PAYE">Payé</MenuItem>
              <MenuItem value="ANNULE">Annulé</MenuItem>
            </TextField>
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
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Annuler</Button>
        <Button onClick={handleSubmit} variant="contained" color="primary">
          {order ? 'Modifier' : 'Créer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default PaymentOrdersList;