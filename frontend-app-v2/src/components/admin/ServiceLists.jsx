import React, { useState, useEffect } from 'react';
import {
  Box,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Tabs,
  Tab,
  Chip,
  Button,
  Grid,
  TextField,
  InputAdornment
} from '@mui/material';
import {
  AccountBalance as AccountBalanceIcon,
  Business as BusinessIcon,
  Assignment as AssignmentIcon,
  Refresh as RefreshIcon,
  Search as SearchIcon
} from '@mui/icons-material';
import { getAllLoans } from '../../services/dette-tresor/loansService';
import { getAllProjets, medaApi } from '../../services/meda/projetsService';
import { getAllAdjudications } from '../../services/dette-interieur/adjudicationsService';
// Import setAuthToken functions
import { setAuthToken as setLoansAuthToken } from '../../services/dette-tresor/loansService';
import { setAuthToken as setProjetsAuthToken } from '../../services/meda/projetsService';
import { setAuthToken as setAdjudicationsAuthToken } from '../../services/dette-interieur/adjudicationsService';
import { initAuth } from '../../services/authService';

const ServiceLists = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [detteTresorData, setDetteTresorData] = useState([]);
  const [medaData, setMedaData] = useState([]);
  const [detteInterieurData, setDetteInterieurData] = useState([]);
  const [loading, setLoading] = useState({ detteTresor: false, meda: false, detteInterieur: false });
  const [errors, setErrors] = useState({ detteTresor: null, meda: null, detteInterieur: null });
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('No authentication token found!');
      return;
    }
    
    // Parse user info to check for admin role
    const userInfoStr = localStorage.getItem('userInfo');
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr);
        if (userInfo.roles && Array.isArray(userInfo.roles)) {
          // Check if user has admin role
          const isAdmin = userInfo.roles.some(role => 
            role.trim().toLowerCase() === 'admin' || 
            role.trim().toLowerCase().includes('admin')
          );
          
          if (isAdmin) {
            console.log('Admin user detected - ensuring MEDA role is present');
            // Add MEDA role to admin users if not present
            const hasMedaRole = userInfo.roles.some(role => {
              const normalizedRole = role.trim().toLowerCase();
              return normalizedRole === 'meda' || 
                     normalizedRole.includes('meda');
            });
            
            if (!hasMedaRole) {
              console.log('Adding MEDA role to admin user');
              userInfo.roles.push('MEDA');
              localStorage.setItem('userInfo', JSON.stringify(userInfo));
            }
          }
        }
      } catch (e) {
        console.error('Error parsing user info:', e);
      }
    }
    
    // Fetch data for all services
    fetchDetteTresorData(token);
    fetchMedaData(token);
    fetchDetteInterieurData(token);
  }, []);

  const fetchDetteTresorData = async (token) => {
    try {
      setLoading(prev => ({ ...prev, detteTresor: true }));
      setErrors(prev => ({ ...prev, detteTresor: null }));
      // Set auth token
      if (token) {
        setLoansAuthToken(token);
      }
      const response = await getAllLoans();
      // Handle both array and object with data property
      const data = Array.isArray(response) ? response : (response.data || []);
      setDetteTresorData(data || []);
    } catch (error) {
      console.error('Error fetching Dette Trésor data:', error);
      setErrors(prev => ({ ...prev, detteTresor: error.message || 'Service non disponible' }));
      setDetteTresorData([]);
    } finally {
      setLoading(prev => ({ ...prev, detteTresor: false }));
    }
  };

  const fetchMedaData = async (token) => {
    try {
      setLoading(prev => ({ ...prev, meda: true }));
      setErrors(prev => ({ ...prev, meda: null }));
      // Set auth token
      if (token) {
        // Fix: Pass the token without "Bearer" prefix since setProjetsAuthToken will add it
        setProjetsAuthToken(token);
      }
      
      const response = await getAllProjets();
      // Handle both array and object with data property
      const data = Array.isArray(response) ? response : (response.data || []);
      setMedaData(data || []);
    } catch (error) {
      console.error('Error fetching MEDA data:', error);
      setErrors(prev => ({ ...prev, meda: error.message || 'Service non disponible' }));
      setMedaData([]);
    } finally {
      setLoading(prev => ({ ...prev, meda: false }));
    }
  };

  const fetchDetteInterieurData = async (token) => {
    try {
      setLoading(prev => ({ ...prev, detteInterieur: true }));
      setErrors(prev => ({ ...prev, detteInterieur: null }));
      // Set auth token
      if (token) {
        setAdjudicationsAuthToken(token);
      }
      const response = await getAllAdjudications();
      // Handle both array and object with data property
      const data = Array.isArray(response) ? response : (response.data || []);
      setDetteInterieurData(data || []);
    } catch (error) {
      console.error('Error fetching Dette Intérieur data:', error);
      setErrors(prev => ({ ...prev, detteInterieur: error.message || 'Service non disponible' }));
      setDetteInterieurData([]);
    } finally {
      setLoading(prev => ({ ...prev, detteInterieur: false }));
    }
  };

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleRefresh = () => {
    const token = localStorage.getItem('token');
    if (!token) return;
    
    if (activeTab === 0) {
      fetchDetteTresorData(token);
    } else if (activeTab === 1) {
      fetchMedaData(token);
    } else if (activeTab === 2) {
      fetchDetteInterieurData(token);
    }
  };

  const handleSearch = (event) => {
    setSearchTerm(event.target.value);
  };

  // Filter data based on search term
  const getFilteredData = (data) => {
    if (!searchTerm) return data;
    
    const term = searchTerm.toLowerCase();
    return data.filter(item => {
      // Generic search across all string properties
      return Object.values(item).some(value => 
        typeof value === 'string' && value.toLowerCase().includes(term)
      );
    });
  };

  const filteredDetteTresorData = getFilteredData(detteTresorData);
  const filteredMedaData = getFilteredData(medaData);
  const filteredDetteInterieurData = getFilteredData(detteInterieurData);

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#333', fontWeight: 'bold' }}>
            Listes des Services
          </Typography>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            startIcon={<RefreshIcon />}
            onClick={handleRefresh}
            sx={{ 
              bgcolor: '#FF6B35',
              '&:hover': {
                bgcolor: '#e55a2b'
              },
              borderRadius: 2,
              boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)'
            }}
          >
            Actualiser
          </Button>
        </Grid>
      </Grid>

      <Paper sx={{ p: 2, mb: 3, borderRadius: 2, border: '1px solid #e0e0e0' }}>
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Rechercher dans les services..."
          value={searchTerm}
          onChange={handleSearch}
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

      <Tabs 
        value={activeTab} 
        onChange={handleTabChange} 
        sx={{ mb: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}
        TabIndicatorProps={{ style: { backgroundColor: '#FF6B35' } }}
      >
        <Tab icon={<AccountBalanceIcon />} label="Dette Trésor" />
        <Tab icon={<BusinessIcon />} label="MEDA" />
        <Tab icon={<AssignmentIcon />} label="Dette Intérieur" />
      </Tabs>
      
      {activeTab === 0 && (
        <Paper sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
          <Typography variant="h6" gutterBottom sx={{ color: '#333', fontWeight: 'bold', mb: 2 }}>
            Dette Trésor - Prêts
          </Typography>
          {loading.detteTresor ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <CircularProgress />
            </Box>
          ) : errors.detteTresor ? (
            <Box sx={{ textAlign: 'center', my: 4, color: 'error.main' }}>
              <Typography variant="body1">
                Erreur: {errors.detteTresor}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Le service Dette Trésor n'est pas disponible actuellement.
              </Typography>
            </Box>
          ) : (
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
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredDetteTresorData.map((loan) => (
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
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Paper>
      )}
      
      {activeTab === 1 && (
        <Paper sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
          <Typography variant="h6" gutterBottom sx={{ color: '#333', fontWeight: 'bold', mb: 2 }}>
            MEDA - Projets
          </Typography>
          {loading.meda ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <CircularProgress />
            </Box>
          ) : errors.meda ? (
            <Box sx={{ textAlign: 'center', my: 4, color: 'error.main' }}>
              <Typography variant="body1">
                Erreur: {errors.meda}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Le service MEDA n'est pas disponible actuellement.
              </Typography>
            </Box>
          ) : (
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
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Nom du Projet</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Secteur</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Début</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredMedaData.map((projet) => (
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
                      <TableCell>{projet.nomProjet}</TableCell>
                      <TableCell>{projet.secteur}</TableCell>
                      <TableCell>{parseFloat(projet.montant).toLocaleString()} MAD</TableCell>
                      <TableCell>
                        <Chip 
                          label={projet.statut} 
                          size="small"
                          sx={{
                            bgcolor: projet.statut === 'ACTIF' ? '#4caf50' : '#f44336',
                            color: 'white',
                            fontWeight: 'bold',
                            borderRadius: 1
                          }}
                        />
                      </TableCell>
                      <TableCell>{new Date(projet.dateDebut).toLocaleDateString('fr-FR')}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Paper>
      )}
      
      {activeTab === 2 && (
        <Paper sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
          <Typography variant="h6" gutterBottom sx={{ color: '#333', fontWeight: 'bold', mb: 2 }}>
            Dette Intérieur - Adjudications
          </Typography>
          {loading.detteInterieur ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <CircularProgress />
            </Box>
          ) : errors.detteInterieur ? (
            <Box sx={{ textAlign: 'center', my: 4, color: 'error.main' }}>
              <Typography variant="body1">
                Erreur: {errors.detteInterieur}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Le service Dette Intérieur n'est pas disponible actuellement.
              </Typography>
            </Box>
          ) : (
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
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Numéro d'Adjudication</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Objet</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date d'Adjudication</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredDetteInterieurData.map((adjudication) => (
                    <TableRow 
                      key={adjudication.id}
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
                      <TableCell>{adjudication.numeroAdjudication}</TableCell>
                      <TableCell>{adjudication.objet}</TableCell>
                      <TableCell>{parseFloat(adjudication.montant).toLocaleString()} MAD</TableCell>
                      <TableCell>{new Date(adjudication.dateAdjudication).toLocaleDateString('fr-FR')}</TableCell>
                      <TableCell>
                        <Chip 
                          label={adjudication.statut} 
                          size="small"
                          sx={{
                            bgcolor: adjudication.statut === 'ACTIF' ? '#4caf50' : '#f44336',
                            color: 'white',
                            fontWeight: 'bold',
                            borderRadius: 1
                          }}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Paper>
      )}
    </Box>
  );
};

export default ServiceLists;