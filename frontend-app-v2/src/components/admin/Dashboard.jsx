import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import {
  AppBar,
  Box,
  Button,
  CircularProgress,
  Chip,
  Container,
  CssBaseline,
  Divider,
  Drawer,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Toolbar,
  Typography
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Group as GroupIcon,
  Security as SecurityIcon,
  Person as PersonIcon,
  History as HistoryIcon,
  Menu as MenuIcon,
  Home as HomeIcon,
  Notifications as NotificationsIcon,
  // Dette Tresor Icons
  AccountBalance as LoanIcon,
  CreditCard as CreditIcon,
  Payment as PaymentIcon,
  Money as DebitIcon,
  Description as DocumentIcon,
  Timeline as TimelineIcon,
  // Dette Interieur Icons
  Gavel,
  Assignment,
  Build,
  AccountBalance,
  TrendingUp,
  CreditCard,
  // MEDA Icons
  AccountBalance as ProjectIcon,
  Money as AdvanceIcon
} from '@mui/icons-material';
import { useAuth } from '../../context/AuthContext';
// Import GDP logo
import GDPLogo from '../../assets/gdp.png';
// Admin Components
import UserList from './UserList';
import ServiceLists from './ServiceLists.jsx';
// Dette Tresor Components
import LoansList from '../dette-du-tresor/LoansList.jsx';
import PaymentOrdersList from '../dette-du-tresor/PaymentOrdersList.jsx';
import CreditAdvicesList from '../dette-du-tresor/CreditAdvicesList.jsx';
import DebitAdvicesList from '../dette-du-tresor/DebitAdvicesList.jsx';
import SettlementLettersList from '../dette-du-tresor/SettlementLettersList.jsx';
import EcheanciersList from '../dette-du-tresor/EcheanciersList.jsx';
// Dette Interieur Components
import AdjudicationsList from '../dette-interieur/components/AdjudicationsList';
import AvisAdjudicationList from '../dette-interieur/components/AvisAdjudicationList';
import BonEquipementList from '../dette-interieur/components/BonEquipementList';
import AvisBonEquipementList from '../dette-interieur/components/AvisBonEquipementList';
import CommissionList from '../dette-interieur/components/CommissionList';
import InteretDepotList from '../dette-interieur/components/InteretDepotList';
// MEDA Components
import ProjetsList from '../meda-service/ProjetsList.jsx';
import AvancesList from '../meda-service/AvancesList.jsx';
import PiecesJustificativesList from '../meda-service/PiecesJustificativesList.jsx';

// Service Imports
// Admin Services
import userService, { setUserAuthToken as setUsersAuthToken } from '../../services/admin/userService';
// Dette Tresor Services
import { getAllLoans } from '../../services/dette-tresor/loansService';
import { getAllCreditAdvices } from '../../services/dette-tresor/creditAdvicesService';
import { getAllPaymentOrders } from '../../services/dette-tresor/paymentOrdersService';
import { getAllDebitAdvices } from '../../services/dette-tresor/debitAdvicesService';
import { getAllSettlementLetters } from '../../services/dette-tresor/settlementLettersService';
// MEDA Services
import { getAllProjets, getProjetStatistics } from '../../services/meda/projetsService';
import { getAllAvances } from '../../services/meda/avancesService';
import { getAllPiecesJustificatives } from '../../services/meda/piecesJustificativesService';

// Auth Token Setters
// Dette Tresor Auth
import { setAuthToken as setLoansAuthToken } from '../../services/dette-tresor/loansService';
import { setAuthToken as setCreditAdvicesAuthToken } from '../../services/dette-tresor/creditAdvicesService';
import { setAuthToken as setPaymentOrdersAuthToken } from '../../services/dette-tresor/paymentOrdersService';
import { setAuthToken as setDebitAdvicesAuthToken } from '../../services/dette-tresor/debitAdvicesService';
import { setAuthToken as setSettlementLettersAuthToken } from '../../services/dette-tresor/settlementLettersService';
// Dette Interieur Auth
import { setAuthToken as setAdjudicationsAuthToken } from '../../services/dette-interieur/adjudicationsService';
import { setAuthToken as setAvisAdjudicationAuthToken } from '../../services/dette-interieur/avisAdjudicationService';
import { setAuthToken as setBonEquipementAuthToken } from '../../services/dette-interieur/bonEquipementService';
import { setAuthToken as setAvisBonEquipementAuthToken } from '../../services/dette-interieur/avisBonEquipementService';
import { setAuthToken as setCommissionsAuthToken } from '../../services/dette-interieur/commissionsService';
import { setAuthToken as setInteretDepotAuthToken } from '../../services/dette-interieur/interetDepotService';
// MEDA Auth
import { setAuthToken as setProjetsAuthToken } from '../../services/meda/projetsService';
import { setAuthToken as setAvancesAuthToken } from '../../services/meda/avancesService';
import { setAuthToken as setPiecesJustificativesAuthToken } from '../../services/meda/piecesJustificativesService';
import { initAuth } from '../../services/authService';

const AdminDashboard = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [userStatistics, setUserStatistics] = useState({ total: 0, active: 0, inactive: 0 });
  const [roleStatistics, setRoleStatistics] = useState({ total: 0, admin: 0, service: 0 });
  const [loadingStats, setLoadingStats] = useState(true);
  const [userActivityHistory, setUserActivityHistory] = useState([]);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  useEffect(() => {
    // Set auth token for all services
    const token = localStorage.getItem('token');
    if (token) {
      // Admin services
      setUsersAuthToken(token);
      
      // Dette Tresor services
      setLoansAuthToken(token);
      setCreditAdvicesAuthToken(token);
      setPaymentOrdersAuthToken(token);
      setDebitAdvicesAuthToken(token);
      setSettlementLettersAuthToken(token);
      
      // Dette Interieur services
      setAdjudicationsAuthToken(token);
      setAvisAdjudicationAuthToken(token);
      setBonEquipementAuthToken(token);
      setAvisBonEquipementAuthToken(token);
      setCommissionsAuthToken(token);
      setInteretDepotAuthToken(token);
      
      // MEDA services
      setProjetsAuthToken(token);
      setAvancesAuthToken(token);
      setPiecesJustificativesAuthToken(token);
      
      // Initialize all auth services
      initAuth(token);
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }
    
    setUserInfo(user);
    // Initialize dashboard data
    fetchNotifications();
    fetchStatistics();
    initializeUserActivityHistory();
  }, [user, navigate]);

  const initializeUserActivityHistory = () => {
    // Initialize with login event
    const loginEvent = {
      id: Date.now(),
      timestamp: new Date().toISOString(),
      action: 'Connexion au tableau de bord',
      details: `Administrateur ${user?.username} connecté`
    };
    const history = [loginEvent];
    setUserActivityHistory(history);
  };

  const fetchNotifications = async () => {
    try {
      // Simulate fetching notifications
      const mockNotifications = [
        { id: 1, message: "Nouvel utilisateur créé: USER1000", time: "2 heures ago", read: false },
        { id: 2, message: "Rôle mis à jour: ROLE2000", time: "5 heures ago", read: false },
        { id: 3, message: "Maintenance système prévue ce soir", time: "1 jour ago", read: true }
      ];
      
      setNotifications(mockNotifications);
      setUnreadCount(mockNotifications.filter(n => !n.read).length);
    } catch (error) {
      console.error('Error fetching notifications:', error);
    }
  };

  const fetchStatistics = async () => {
    try {
      setLoadingStats(true);
      
      // Simulate fetching data
      setTimeout(() => {
        // Mock statistics data
        setUserStatistics({
          total: 42,
          active: 38,
          inactive: 4
        });
        
        setRoleStatistics({
          total: 8,
          admin: 2,
          service: 6
        });
        
        setLoadingStats(false);
      }, 1000);
    } catch (error) {
      console.error('Error fetching statistics data:', error);
      // Fallback to mock data in case of error
      setUserStatistics({
        total: 42,
        active: 38,
        inactive: 4
      });
      
      setRoleStatistics({
        total: 8,
        admin: 2,
        service: 6
      });
      
      setLoadingStats(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const drawerWidth = 240;

  const drawer = (
    <div>
      <Toolbar sx={{ 
        bgcolor: '#FF6B35',
        color: 'white',
        fontWeight: 'bold',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
      }}>
        <Box
          component="img"
          src={GDPLogo}
          alt="GDP Logo"
          sx={{
            height: 45,
            mr: 1
          }}
        />
      </Toolbar>
      <Divider />
      <List>
        <ListItem 
          component={Link} 
          to="/admin" 
          selected={location.pathname === '/admin'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <DashboardIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Tableau de Bord" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/users" 
          selected={location.pathname === '/admin/users'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <GroupIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Gestion des Utilisateurs" />
        </ListItem>
        
        
        {/* Dette Tresor Section */}
        <Divider sx={{ my: 1 }} />
        <ListItem>
          <ListItemText primary="Dette Trésor" sx={{ fontWeight: 'bold' }} />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/prets" 
          selected={location.pathname === '/admin/prets'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <PaymentIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Prêts" />
        </ListItem>
        
        <ListItem 
          component={Link} 
          to="/admin/ordres-paiement" 
          selected={location.pathname === '/admin/ordres-paiement'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <PaymentIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Ordres de Paiement" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/avis-credit" 
          selected={location.pathname === '/admin/avis-credit'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <CreditIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Avis de Crédit" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/avis-debit" 
          selected={location.pathname === '/admin/avis-debit'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <DebitIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Avis de Débit" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/lettres-reglement" 
          selected={location.pathname === '/admin/lettres-reglement'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <DocumentIcon sx={{ color: '#FF6B35' }}/>
          </ListItemIcon>
          <ListItemText primary="Lettres de Règlement" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/echeancier" 
          selected={location.pathname === '/admin/echeancier'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <TimelineIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Échéancier" />
        </ListItem>
        
        {/* Dette Interieur Links */}
        <Divider sx={{ my: 1 }} />
        <ListItem>
          <ListItemText primary="Dette Interieur" sx={{ fontWeight: 'bold' }} />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/adjudications" 
          selected={location.pathname === '/admin/adjudications'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <Gavel sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Adjudications" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/avis-adjudication" 
          selected={location.pathname === '/admin/avis-adjudication'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <Assignment sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Avis d'Adjudication" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/bon-equipement" 
          selected={location.pathname === '/admin/bon-equipement'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <Build sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Bons d'Équipement" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/avis-bon-equipement" 
          selected={location.pathname === '/admin/avi-bons-equipement'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <DocumentIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Avis de Bons d'Équipement" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/commissions" 
          selected={location.pathname === '/admin/commissions'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <AccountBalance sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Commissions" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/interet-depot" 
          selected={location.pathname === '/admin/interet-depot'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <TrendingUp sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Intérêts de Dépôt" />
        </ListItem>
        
        {/* MEDA Section */}
        <Divider sx={{ my: 1 }} />
        <ListItem>
          <ListItemText primary="MEDA" sx={{ fontWeight: 'bold' }} />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/projets" 
          selected={location.pathname === '/admin/projets'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <ProjectIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Projets" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/avances" 
          selected={location.pathname === '/admin/avances'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <AdvanceIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Avances" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/admin/pieces-justificatives" 
          selected={location.pathname === '/admin/pieces-justificatives'}
          sx={{
            '&:hover': {
              bgcolor: 'rgba(255, 107, 53, 0.1)'
            },
            '&.Mui-selected': {
              bgcolor: 'rgba(255, 107, 53, 0.2)',
              borderRight: '3px solid #FF6B35'
            }
          }}
        >
          <ListItemIcon>
            <DocumentIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Pièces Justificatives" />
        </ListItem>
      </List>
    </div>
  );

  // Dashboard content component
  const DashboardContent = () => {
    return (
      <Box sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - ${drawerWidth}px)` } }}>
        <Toolbar />
        
        {/* Welcome Section */}
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid size={12}>
            <Paper 
              elevation={0} 
              sx={{ 
                p: 3, 
                mb: 3, 
                background: 'linear-gradient(135deg, #FF6B35 0%, #F7931E 100%)',
                color: 'white',
                borderRadius: 2,
                boxShadow: '0 4px 20px rgba(0,0,0,0.1)'
              }}
            >
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap' }}>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 1 }}>
                    Bienvenue, {userInfo?.username || 'Administrateur'}!
                  </Typography>
                  <Typography variant="body1">
                    Tableau de bord d'administration
                  </Typography>
                </Box>
                <Box sx={{ textAlign: 'right' }}>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    {new Date().toLocaleDateString('fr-FR', { 
                      weekday: 'long', 
                      year: 'numeric', 
                      month: 'long', 
                      day: 'numeric' 
                    })}
                  </Typography>
                  <Chip 
                    label="Administrateur" 
                    sx={{ 
                      bgcolor: 'rgba(255, 255, 255, 0.2)', 
                      color: 'white',
                      fontWeight: 'bold'
                    }} 
                  />
                </Box>
              </Box>
            </Paper>
          </Grid>
        </Grid>

        <Grid container spacing={3}>
          {/* User Profile Section */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid size={12}>
              <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <PersonIcon sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    Informations de l'Utilisateur
                  </Typography>
                </Box>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      <strong>Nom d'utilisateur:</strong> {userInfo?.username || 'N/A'}
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      <strong>Email:</strong> {userInfo?.email || 'N/A'}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      <strong>Nom complet:</strong> {userInfo?.nomComplet || 'N/A'}
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 1 }}>
                      <strong>Statut:</strong> 
                      <Chip 
                        label={userInfo?.statut === 'ACTIF' ? 'Actif' : 'Inactif'} 
                        size="small" 
                        sx={{ 
                          ml: 1,
                          bgcolor: userInfo?.statut === 'ACTIF' ? '#4caf50' : '#f44336',
                          color: 'white',
                          fontWeight: 'bold'
                        }} 
                      />
                    </Typography>
                  </Grid>
                </Grid>
              </Paper>
            </Grid>
          </Grid>
          
          {/* Statistics Section */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid size={12}>
              <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <SecurityIcon sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    Statistiques Système
                  </Typography>
                </Box>
                {loadingStats ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                  </Box>
                ) : (
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <Paper 
                        elevation={0} 
                        sx={{ 
                          p: 2, 
                          borderRadius: 2, 
                          border: '1px solid #e0e0e0',
                          height: '100%',
                          display: 'flex',
                          flexDirection: 'column'
                        }}
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <GroupIcon sx={{ fontSize: 30, color: '#FF6B35', mr: 1 }} />
                          <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            Utilisateurs
                          </Typography>
                        </Box>
                        <Typography variant="h3" sx={{ color: '#FF6B35', fontWeight: 'bold', mt: 'auto' }}>
                          {userStatistics.total}
                        </Typography>
                        <Box sx={{ display: 'flex', mt: 1 }}>
                          <Typography variant="body2" sx={{ color: '#4caf50', mr: 2 }}>
                            Actifs: {userStatistics.active}
                          </Typography>
                          <Typography variant="body2" sx={{ color: '#f44336' }}>
                            Inactifs: {userStatistics.inactive}
                          </Typography>
                        </Box>
                      </Paper>
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <Paper 
                        elevation={0} 
                        sx={{ 
                          p: 2, 
                          borderRadius: 2, 
                          border: '1px solid #e0e0e0',
                          height: '100%',
                          display: 'flex',
                          flexDirection: 'column'
                        }}
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <SecurityIcon sx={{ fontSize: 30, color: '#2196f3', mr: 1 }} />
                          <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            Rôles
                          </Typography>
                        </Box>
                        <Typography variant="h3" sx={{ color: '#2196f3', fontWeight: 'bold', mt: 'auto' }}>
                          {roleStatistics.total}
                        </Typography>
                        <Box sx={{ display: 'flex', mt: 1 }}>
                          <Typography variant="body2" sx={{ color: '#4caf50', mr: 2 }}>
                            Admin: {roleStatistics.admin}
                          </Typography>
                          <Typography variant="body2" sx={{ color: '#ff9800' }}>
                            Service: {roleStatistics.service}
                          </Typography>
                        </Box>
                      </Paper>
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <Paper 
                        elevation={0} 
                        sx={{ 
                          p: 2, 
                          borderRadius: 2, 
                          border: '1px solid #e0e0e0',
                          height: '100%',
                          display: 'flex',
                          flexDirection: 'column'
                        }}
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <HistoryIcon sx={{ fontSize: 30, color: '#9c27b0', mr: 1 }} />
                          <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            Services
                          </Typography>
                        </Box>
                        <Typography variant="h3" sx={{ color: '#9c27b0', fontWeight: 'bold', mt: 'auto' }}>
                          3
                        </Typography>
                        <Box sx={{ display: 'flex', mt: 1 }}>
                          <Typography variant="body2" sx={{ color: '#4caf50', mr: 2 }}>
                            Actifs: 3
                          </Typography>
                          <Typography variant="body2" sx={{ color: '#f44336' }}>
                            Inactifs: 0
                          </Typography>
                        </Box>
                      </Paper>
                    </Grid>
                  </Grid>
                )}
              </Paper>
            </Grid>
          </Grid>
          
          {/* History Tracking Section */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid size={12}>
              <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <HistoryIcon sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    Historique des Activités
                  </Typography>
                </Box>
                <TableContainer component={Paper} elevation={0} sx={{ borderRadius: 2, border: '1px solid #e0e0e0' }}>
                  <Table>
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
                        <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date/Heure</TableCell>
                        <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Action</TableCell>
                        <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Détails</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {userActivityHistory.length > 0 ? (
                        userActivityHistory.map((activity) => (
                          <TableRow 
                            key={activity.id}
                            sx={{
                              '&:nth-of-type(odd)': {
                                bgcolor: 'rgba(0, 0, 0, 0.02)'
                              },
                              '&:hover': {
                                bgcolor: 'rgba(255, 107, 53, 0.05)'
                              }
                            }}
                          >
                            <TableCell>
                              {new Date(activity.timestamp).toLocaleString('fr-FR', {
                                day: '2-digit',
                                month: '2-digit',
                                year: 'numeric',
                                hour: '2-digit',
                                minute: '2-digit'
                              })}
                            </TableCell>
                            <TableCell>
                              <Chip 
                                label={activity.action} 
                                size="small" 
                                sx={{ 
                                  bgcolor: activity.action.includes('Connexion') || activity.action.includes('Déconnexion') 
                                    ? '#e3f2fd' : '#f5f5f5',
                                  fontWeight: 'bold'
                                }} 
                              />
                            </TableCell>
                            <TableCell>{activity.details}</TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={3} align="center">
                            Aucune activité récente
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            </Grid>
          </Grid>
        </Grid>
      </Box>
    );
  };

  // Remove the Routes/Route wrapper and directly render components based on location
  const renderContent = () => {
    // Dashboard home content
    if (location.pathname === '/admin' || location.pathname === '/admin/') {
      return <DashboardContent 
        userInfo={userInfo}
        notifications={notifications}
        unreadCount={unreadCount}
        userStatistics={userStatistics}
        roleStatistics={roleStatistics}
        loadingStats={loadingStats}
        userActivityHistory={userActivityHistory}
      />;
    }
    
    // Render specific components based on route
    if (location.pathname === '/admin/users') {
      return <UserList />;
    }
    if (location.pathname === '/admin/prets') {
      return <LoansList />;
    }
    
    if (location.pathname === '/admin/avis-credit') {
      return <CreditAdvicesList />;
    }
    
    if (location.pathname === '/admin/ordres-paiement') {
      return <PaymentOrdersList />;
    }
    
    if (location.pathname === '/admin/avis-debit') {
      return <DebitAdvicesList />;
    }
    
    if (location.pathname === '/admin/lettres-reglement') {
      return <SettlementLettersList />;
    }
    
    if (location.pathname === '/admin/echeancier') {
      return <EcheanciersList />;
    }
    if (location.pathname === '/admin/projets') {
      return <ProjetsList />;
    }
    
    if (location.pathname === '/admin/avances') {
      return <AvancesList />;
    }
    
    if (location.pathname === '/admin/pieces-justificatives') {
      return <PiecesJustificativesList />;
    }




    if (location.pathname === '/admin/adjudications') {
      return <AdjudicationsList />;
    }
    
    if (location.pathname === '/admin/avis-adjudication') {
      return <AvisAdjudicationList />;
    }
    
    if (location.pathname === '/admin/bon-equipement') {
      return <BonEquipementList />;
    }
    
    if (location.pathname === '/admin/avis-bon-equipement') {
      return <AvisBonEquipementList />;
    }
    
    if (location.pathname === '/admin/commissions') {
      return <CommissionList />;
    }
    
    if (location.pathname === '/admin/interet-depot') {
      return <InteretDepotList />;
    }
    
    
    // Default to dashboard content
    return <DashboardContent 
      userInfo={userInfo}
      notifications={notifications}
      unreadCount={unreadCount}
      userStatistics={userStatistics}
      roleStatistics={roleStatistics}
      loadingStats={loadingStats}
      userActivityHistory={userActivityHistory}
    />;
  };

  if (!userInfo) {
    return <div className="dashboard-loading">Loading...</div>;
  }

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Tableau de Bord Administrateur
          </Typography>
          <Button color="inherit" onClick={handleLogout}>
            Déconnexion
          </Button>
        </Toolbar>
      </AppBar>
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
        aria-label="mailbox folders"
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true,
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - ${drawerWidth}px)` } }}
      >
        <Toolbar />
        {renderContent()}
      </Box>
    </Box>
  );
};

export default AdminDashboard;