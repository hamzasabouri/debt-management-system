import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
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
  Typography,
  Popover
} from '@mui/material';
import {
  AccountBalance as LoanIcon,
  AccountCircle as UserIcon,
  Assessment as AnalyticsIcon,
  CreditCard as CreditIcon,
  Dashboard as DashboardIcon,
  Description as DocumentIcon,
  History as HistoryIcon,
  Home as HomeIcon,
  Menu as MenuIcon,
  Money as DebitIcon,
  Notifications as NotificationsIcon,
  Payment as PaymentIcon,
  Timeline as TimelineIcon
} from '@mui/icons-material';
import LoansList from './LoansList';
import PaymentOrdersList from './PaymentOrdersList';
import CreditAdvicesList from './CreditAdvicesList';
import DebitAdvicesList from './DebitAdvicesList';
import SettlementLettersList from './SettlementLettersList';
import EcheanciersList from './EcheanciersList';
// Import services to fetch real data
import { getAllLoans } from '../../services/dette-tresor/loansService';
import { getAllCreditAdvices } from '../../services/dette-tresor/creditAdvicesService';
import { getAllPaymentOrders } from '../../services/dette-tresor/paymentOrdersService';
import { getAllDebitAdvices } from '../../services/dette-tresor/debitAdvicesService';
import { getAllSettlementLetters } from '../../services/dette-tresor/settlementLettersService';
// Import setAuthToken functions from all services
import { setAuthToken as setLoansAuthToken } from '../../services/dette-tresor/loansService';
import { setAuthToken as setCreditAdvicesAuthToken } from '../../services/dette-tresor/creditAdvicesService';
import { setAuthToken as setPaymentOrdersAuthToken } from '../../services/dette-tresor/paymentOrdersService';
import { setAuthToken as setDebitAdvicesAuthToken } from '../../services/dette-tresor/debitAdvicesService';
import { setAuthToken as setSettlementLettersAuthToken } from '../../services/dette-tresor/settlementLettersService';
import { setAuthToken as setEcheanciersAuthToken } from '../../services/dette-tresor/echeanciersService';
import { setAuthToken as setCalculationAuthToken } from '../../services/dette-tresor/calculationService';
import { setAuthToken as setIntegrationAuthToken } from '../../services/dette-tresor/integrationService';
// Import GDP logo
import GDPLogo from '../../assets/gdp.png';

const DetteDuTresorDashboard = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [activeTab, setActiveTab] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [debtStatistics, setDebtStatistics] = useState([]);
  const [activeLoansCount, setActiveLoansCount] = useState(0);
  const [fullyPaidLoansCount, setFullyPaidLoansCount] = useState(0);
  const [loadingStats, setLoadingStats] = useState(true);
  const [integrationStatus, setIntegrationStatus] = useState(null);
  const [loadingIntegration, setLoadingIntegration] = useState(true);
  const [userActivityHistory, setUserActivityHistory] = useState([]);
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState(null);

  // Set auth token for all services
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // Set the auth token for each service
      setLoansAuthToken(token);
      setCreditAdvicesAuthToken(token);
      setPaymentOrdersAuthToken(token);
      setDebitAdvicesAuthToken(token);
      setSettlementLettersAuthToken(token);
      setEcheanciersAuthToken(token);
      setCalculationAuthToken(token);
      setIntegrationAuthToken(token);
    }
  }, []); // Run once on mount

  // Additional effect to ensure token is set when location changes
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // Set the auth token for each service
      setLoansAuthToken(token);
      setCreditAdvicesAuthToken(token);
      setPaymentOrdersAuthToken(token);
      setDebitAdvicesAuthToken(token);
      setSettlementLettersAuthToken(token);
      setEcheanciersAuthToken(token);
      setCalculationAuthToken(token);
      setIntegrationAuthToken(token);
    }
  }, [location]); // Run when location changes

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    const storedUserInfo = localStorage.getItem('userInfo');
    
    if (!token || !storedUserInfo) {
      navigate('/');
      return;
    }
    
    const user = JSON.parse(storedUserInfo);
    setUserInfo(user);
    
    // Initialize dashboard data
    fetchNotifications();
    fetchCalculationData();
    fetchIntegrationData();
    initializeUserActivityHistory();
  }, [navigate]);

  const initializeUserActivityHistory = () => {
    // Load existing history from localStorage or initialize with default events
    const storedHistory = localStorage.getItem('userActivityHistory');
    if (storedHistory) {
      setUserActivityHistory(JSON.parse(storedHistory));
    } else {
      // Get current user info to avoid undefined reference
      const storedUserInfo = localStorage.getItem('userInfo');
      const currentUser = storedUserInfo ? JSON.parse(storedUserInfo) : { username: 'inconnu' };
      
      // Initialize with login event
      const loginEvent = {
        id: Date.now(),
        timestamp: new Date().toISOString(),
        action: 'Connexion au tableau de bord',
        details: `Utilisateur ${currentUser.username} connecté`
      };
      const history = [loginEvent];
      setUserActivityHistory(history);
      localStorage.setItem('userActivityHistory', JSON.stringify(history));
    }
  };

  // Function to add activity to history
  const addUserActivity = (action, details) => {
    const newActivity = {
      id: Date.now(),
      timestamp: new Date().toISOString(),
      action,
      details
    };
    
    const updatedHistory = [newActivity, ...userActivityHistory].slice(0, 50); // Keep only last 50 activities
    setUserActivityHistory(updatedHistory);
    localStorage.setItem('userActivityHistory', JSON.stringify(updatedHistory));
  };

  const fetchNotifications = async () => {
    try {
      // Simulate fetching notifications
      const mockNotifications = [
        { id: 1, message: "Nouveau prêt créé: PR1000", time: "2 heures ago", read: false },
        { id: 2, message: "Ordre de paiement mis à jour: OP2000", time: "5 heures ago", read: false },
        { id: 3, message: "Maintenance système prévue ce soir", time: "1 jour ago", read: true }
      ];
      
      setNotifications(mockNotifications);
      setUnreadCount(mockNotifications.filter(n => !n.read).length);
    } catch (error) {
      console.error('Error fetching notifications:', error);
    }
  };

  const fetchCalculationData = async () => {
    try {
      setLoadingStats(true);
      
      // Fetch real data from services
      const loansData = await getAllLoans();
      
      // Calculate statistics
      const activeLoans = loansData.filter(loan => 
        parseFloat(loan.soldeCourant) > 0
      ).length;
      
      const paidLoans = loansData.filter(loan => 
        parseFloat(loan.soldeCourant) === 0
      ).length;
      
      // Group loans by currency for debt statistics
      const currencyMap = {};
      loansData.forEach(loan => {
        if (!currencyMap[loan.devise]) {
          currencyMap[loan.devise] = { total: 0, count: 0 };
        }
        currencyMap[loan.devise].total += parseFloat(loan.soldeCourant || 0);
        currencyMap[loan.devise].count += 1;
      });
      
      // Convert to array format for display
      const statsArray = Object.entries(currencyMap).map(([currency, data]) => [
        currency,
        data.total.toFixed(2),
        data.count
      ]);
      
      setDebtStatistics(statsArray);
      setActiveLoansCount(activeLoans);
      setFullyPaidLoansCount(paidLoans);
      setLoadingStats(false);
    } catch (error) {
      console.error('Error fetching calculation data:', error);
      // Fallback to mock data in case of error
      setDebtStatistics([
        ['MAD', '15000000.00', 12],
        ['EUR', '2500000.00', 5],
        ['USD', '3000000.00', 8]
      ]);
      setActiveLoansCount(15);
      setFullyPaidLoansCount(8);
      setLoadingStats(false);
    }
  };

  const fetchIntegrationData = async () => {
    try {
      setLoadingIntegration(true);
      
      // Simulate fetching integration status
      setTimeout(() => {
        setIntegrationStatus({
          bam: { status: 'connected' },
          dtfe: { status: 'connected' },
          timestamp: new Date().toISOString()
        });
        setLoadingIntegration(false);
      }, 1000);
    } catch (error) {
      console.error('Error fetching integration data:', error);
      setLoadingIntegration(false);
    }
  };

  const handleLogout = () => {
    // Clear localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    
    // Redirect to login
    navigate('/');
  };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMarkAsRead = (id) => {
    setNotifications(notifications.map(notification => 
      notification.id === id ? { ...notification, read: true } : notification
    ));
    setUnreadCount(unreadCount - 1);
  };

  const handleMarkAllAsRead = () => {
    setNotifications(notifications.map(notification => ({ ...notification, read: true })));
    setUnreadCount(0);
  };

  const handleNotificationsClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleNotificationsClose = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);
  const id = open ? 'simple-popover' : undefined;

  const drawerWidth = 240;

  const drawer = (
    <div>
      <Toolbar 
        sx={{ 
          bgcolor: '#FF6B35',
          color: 'white',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center'
        }}
      >
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
          to="/dette-du-tresor"
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
          to="/dette-du-tresor/prets"
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
            <LoanIcon sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Prêts" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/dette-du-tresor/avis-credit"
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
          to="/dette-du-tresor/ordres-paiement"
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
          to="/dette-du-tresor/avis-debit"
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
          to="/dette-du-tresor/lettres-reglement"
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
          <ListItemText primary="Lettres de Règlement" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/dette-du-tresor/echeancier"
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
      </List>
    </div>
  );

  // Remove the Routes/Route wrapper and directly render components based on location
  const renderContent = () => {
    // Dashboard home content
    if (location.pathname === '/dette-du-tresor' || location.pathname === '/dette-du-tresor/') {
      return <DashboardContent 
        userInfo={userInfo}
        notifications={notifications}
        unreadCount={unreadCount}
        debtStatistics={debtStatistics}
        activeLoansCount={activeLoansCount}
        fullyPaidLoansCount={fullyPaidLoansCount}
        loadingStats={loadingStats}
        integrationStatus={integrationStatus}
        loadingIntegration={loadingIntegration}
        userActivityHistory={userActivityHistory}
        handleMarkAsRead={handleMarkAsRead}
        handleMarkAllAsRead={handleMarkAllAsRead}
        addUserActivity={addUserActivity}
      />;
    }
    
    // Render specific components based on route
    if (location.pathname === '/dette-du-tresor/prets') {
      return <LoansList />;
    }
    
    if (location.pathname === '/dette-du-tresor/avis-credit') {
      return <CreditAdvicesList />;
    }
    
    if (location.pathname === '/dette-du-tresor/ordres-paiement') {
      return <PaymentOrdersList />;
    }
    
    if (location.pathname === '/dette-du-tresor/avis-debit') {
      return <DebitAdvicesList />;
    }
    
    if (location.pathname === '/dette-du-tresor/lettres-reglement') {
      return <SettlementLettersList />;
    }
    
    if (location.pathname === '/dette-du-tresor/echeancier') {
      return <EcheanciersList />;
    }
    
    // Default to dashboard content
    return <DashboardContent 
      userInfo={userInfo}
      notifications={notifications}
      unreadCount={unreadCount}
      debtStatistics={debtStatistics}
      activeLoansCount={activeLoansCount}
      fullyPaidLoansCount={fullyPaidLoansCount}
      loadingStats={loadingStats}
      integrationStatus={integrationStatus}
      loadingIntegration={loadingIntegration}
      userActivityHistory={userActivityHistory}
      handleMarkAsRead={handleMarkAsRead}
      handleMarkAllAsRead={handleMarkAllAsRead}
      addUserActivity={addUserActivity}
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
          bgcolor: '#FF6B35',
          boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)'
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
            Gestion de la Dette du Trésor
          </Typography>
          <IconButton 
            color="inherit"
            aria-describedby={id}
            onClick={handleNotificationsClick}
          >
            <NotificationsIcon />
            {unreadCount > 0 && (
              <Chip 
                label={unreadCount} 
                size="small" 
                sx={{ 
                  position: 'absolute', 
                  top: 5, 
                  right: 5, 
                  backgroundColor: '#f44336', 
                  color: 'white',
                  height: 18,
                  minWidth: 18
                }} 
              />
            )}
          </IconButton>
          <Button color="inherit" onClick={handleLogout} >
            Déconnexion
          </Button>
        </Toolbar>
      </AppBar>
      
      {/* Notifications Popover */}
      <Popover
        id={id}
        open={open}
        anchorEl={anchorEl}
        onClose={handleNotificationsClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        PaperProps={{
          sx: { 
            width: 400, 
            maxHeight: 600,
            mt: 1.5,
            borderRadius: 2,
            boxShadow: '0 4px 20px rgba(0,0,0,0.15)'
          }
        }}
      >
        <Box sx={{ p: 2, bgcolor: '#FF6B35', color: 'white' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
              Notifications
            </Typography>
            {unreadCount > 0 && (
              <Button 
                size="small" 
                onClick={handleMarkAllAsRead}
                sx={{ color: 'white', borderColor: 'white' }}
                variant="outlined"
              >
                Tout marquer comme lu
              </Button>
            )}
          </Box>
        </Box>
        <Box sx={{ maxHeight: 500, overflowY: 'auto' }}>
          {/* Integration Status Section */}
          <Box sx={{ p: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <NotificationsIcon sx={{ fontSize: 20, mr: 1, color: '#FF6B35' }} />
              <Typography variant="subtitle1" sx={{ color: '#333', fontWeight: 'bold' }}>
                Statut des Intégrations Externes
              </Typography>
            </Box>
            {loadingIntegration ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
                <CircularProgress size={20} />
              </Box>
            ) : (
              <Grid container spacing={1}>
                <Grid size={{ xs: 12 }}>
                  <Paper elevation={0} sx={{ p: 1, borderRadius: 1, height: '100%', border: '1px solid #e0e0e0' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <Box sx={{ 
                        width: 8, 
                        height: 8, 
                        borderRadius: '50%', 
                        bgcolor: integrationStatus?.bam?.status === 'connected' ? '#4caf50' : '#f44336',
                        mr: 1 
                      }} />
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        Bank Al-Maghrib (BAM)
                      </Typography>
                    </Box>
                    <Typography variant="caption" color="textSecondary">
                      {integrationStatus?.bam?.status === 'connected' 
                        ? 'Connecté' 
                        : 'Déconnecté'}
                    </Typography>
                  </Paper>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Paper elevation={0} sx={{ p: 1, borderRadius: 1, height: '100%', border: '1px solid #e0e0e0' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <Box sx={{ 
                        width: 8, 
                        height: 8, 
                        borderRadius: '50%', 
                        bgcolor: integrationStatus?.dtfe?.status === 'connected' ? '#4caf50' : '#f44336',
                        mr: 1 
                      }} />
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        Direction de la Trésorerie et des Finances Extérieures (DTFE)
                      </Typography>
                    </Box>
                    <Typography variant="caption" color="textSecondary">
                      {integrationStatus?.dtfe?.status === 'connected' 
                        ? 'Connecté' 
                        : 'Déconnecté'}
                    </Typography>
                  </Paper>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Paper elevation={0} sx={{ p: 1, borderRadius: 1, bgcolor: 'rgba(96, 125, 139, 0.1)', border: '1px solid #e0e0e0' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Box sx={{ 
                        width: 10, 
                        height: 10, 
                        borderRadius: '50%', 
                        bgcolor: (integrationStatus?.bam?.status === 'connected' && integrationStatus?.dtfe?.status === 'connected') 
                          ? '#4caf50' : '#f44336',
                        mr: 1 
                      }} />
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        Statut Global: {
                          (integrationStatus?.bam?.status === 'connected' && integrationStatus?.dtfe?.status === 'connected') 
                            ? 'Opérationnel' 
                            : 'Problème détecté'}
                      </Typography>
                    </Box>
                  </Paper>
                </Grid>
              </Grid>
            )}
          </Box>
          
          <Divider sx={{ my: 1 }} />
          
          {/* Notifications List */}
          {notifications.length > 0 ? (
            <List>
              {notifications.map((notification) => (
                <React.Fragment key={notification.id}>
                  <ListItem 
                    sx={{ 
                      bgcolor: notification.read ? 'transparent' : 'rgba(255, 107, 53, 0.1)',
                      '&:hover': {
                        bgcolor: 'rgba(255, 107, 53, 0.05)'
                      }
                    }}
                  >
                    <ListItemIcon>
                      <NotificationsIcon sx={{ color: notification.read ? '#999' : '#FF6B35' }} />
                    </ListItemIcon>
                    <ListItemText 
                      primary={notification.message} 
                      secondary={notification.time}
                      primaryTypographyProps={{ 
                        sx: { 
                          fontWeight: notification.read ? 'normal' : 'bold',
                          color: notification.read ? '#666' : '#333'
                        } 
                      }}
                    />
                    {!notification.read && (
                      <Button 
                        size="small" 
                        onClick={() => handleMarkAsRead(notification.id)}
                        variant="outlined"
                        sx={{ 
                          ml: 2, 
                          borderColor: '#FF6B35', 
                          color: '#FF6B35',
                          minWidth: 'auto',
                          px: 1
                        }}
                      >
                        Lu
                      </Button>
                    )}
                  </ListItem>
                  <Divider component="li" />
                </React.Fragment>
              ))}
            </List>
          ) : (
            <Box sx={{ p: 3, textAlign: 'center' }}>
              <NotificationsIcon sx={{ fontSize: 40, color: '#ccc', mb: 1 }} />
              <Typography variant="body2" color="textSecondary">
                Aucune notification
              </Typography>
            </Box>
          )}
        </Box>
      </Popover>
      
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

const DashboardContent = ({ 
  userInfo, 
  notifications, 
  unreadCount, 
  debtStatistics, 
  activeLoansCount, 
  fullyPaidLoansCount, 
  loadingStats, 
  integrationStatus, 
  loadingIntegration,
  userActivityHistory,
  handleMarkAsRead,
  handleMarkAllAsRead,
  addUserActivity
}) => {
  return (
    <Container maxWidth="lg">
      <Grid container spacing={3}>
        {/* Welcome Section */}
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
                  Bienvenue, {userInfo?.username || 'Utilisateur'}!
                </Typography>
                <Typography variant="body1">
                  Tableau de bord de gestion de la dette du Trésor
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
                  label={userInfo?.roles?.join(', ') || 'Rôle non défini'} 
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

        {/* Statistics Cards */}
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
              <LoanIcon sx={{ fontSize: 30, color: '#FF6B35', mr: 1 }} />
              <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                Prêts Actifs
              </Typography>
            </Box>
            {loadingStats ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
                <CircularProgress size={24} />
              </Box>
            ) : (
              <Typography variant="h3" sx={{ color: '#FF6B35', fontWeight: 'bold', mt: 'auto' }}>
                {activeLoansCount}
              </Typography>
            )}
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
              <DocumentIcon sx={{ fontSize: 30, color: '#4caf50', mr: 1 }} />
              <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                Prêts Remboursés
              </Typography>
            </Box>
            {loadingStats ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
                <CircularProgress size={24} />
              </Box>
            ) : (
              <Typography variant="h3" sx={{ color: '#4caf50', fontWeight: 'bold', mt: 'auto' }}>
                {fullyPaidLoansCount}
              </Typography>
            )}
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
              <AnalyticsIcon sx={{ fontSize: 30, color: '#2196f3', mr: 1 }} />
              <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                Devise Principale
              </Typography>
            </Box>
            {loadingStats ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
                <CircularProgress size={24} />
              </Box>
            ) : (
              <Box sx={{ mt: 'auto' }}>
                {debtStatistics.length > 0 ? (
                  <>
                    <Typography variant="h4" sx={{ color: '#2196f3', fontWeight: 'bold' }}>
                      {debtStatistics[0][0]}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      {debtStatistics[0][1]} {debtStatistics[0][0]}
                    </Typography>
                  </>
                ) : (
                  <Typography variant="body2" color="textSecondary">
                    Aucune donnée disponible
                  </Typography>
                )}
              </Box>
            )}
          </Paper>
        </Grid>

        {/* Debt Statistics Table */}
        <Grid size={{ xs: 12, lg: 8 }}>
          <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <AnalyticsIcon sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
              <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                Statistiques de la Dette par Devise
              </Typography>
            </Box>
            {loadingStats ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                <CircularProgress />
              </Box>
            ) : (
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
                      <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
                      <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Solde Total</TableCell>
                      <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Nombre de Prêts</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {debtStatistics.map((row, index) => (
                      <TableRow 
                        key={index}
                        sx={{
                          '&:nth-of-type(odd)': {
                            bgcolor: 'rgba(0, 0, 0, 0.02)'
                          },
                          '&:hover': {
                            bgcolor: 'rgba(255, 107, 53, 0.05)'
                          }
                        }}
                      >
                        <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{row[0]}</TableCell>
                        <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>
                          {parseFloat(row[1]).toLocaleString('fr-FR', {
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2
                          })} {row[0]}
                        </TableCell>
                        <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{row[2]}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>
        </Grid>

        {/* User Activity and Integration Status */}
        <Grid size={{ xs: 12, lg: 4 }}>
          <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0', mb: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <HistoryIcon sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
              <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                Activité Récente
              </Typography>
            </Box>
            <Box sx={{ maxHeight: 300, overflowY: 'auto' }}>
              {userActivityHistory.length > 0 ? (
                <List>
                  {userActivityHistory.slice(0, 5).map((activity) => (
                    <React.Fragment key={activity.id}>
                      <ListItem sx={{ py: 1 }}>
                        <ListItemText 
                          primary={activity.action}
                          secondary={
                            <>
                              <Typography component="span" variant="body2" color="textSecondary">
                                {activity.details}
                              </Typography>
                              <br />
                              <Typography component="span" variant="caption" color="textSecondary">
                                {new Date(activity.timestamp).toLocaleString('fr-FR')}
                              </Typography>
                            </>
                          }
                        />
                      </ListItem>
                      <Divider component="li" />
                    </React.Fragment>
                  ))}
                </List>
              ) : (
                <Typography variant="body2" color="textSecondary" sx={{ textAlign: 'center', py: 2 }}>
                  Aucune activité récente
                </Typography>
              )}
            </Box>
          </Paper>

          
        </Grid>

        {/* Quick Access Section */}
        <Grid size={12}>
          <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <DashboardIcon sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
              <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                Accès Rapide
              </Typography>
            </Box>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 2, 
                    textAlign: 'center', 
                    borderRadius: 2, 
                    cursor: 'pointer',
                    border: '1px solid #e0e0e0',
                    '&:hover': { 
                      bgcolor: 'rgba(255, 107, 53, 0.1)',
                      transform: 'translateY(-2px)',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    },
                    transition: 'all 0.2s ease-in-out'
                  }}
                  onClick={() => addUserActivity('Navigation', 'Accès à la gestion des prêts')}
                  component={Link}
                  to="/dette-du-tresor/prets"
                >
                  <LoanIcon sx={{ fontSize: 40, color: '#FF6B35', mb: 1 }} />
                  <Typography variant="body2">Prêts</Typography>
                </Paper>
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 2, 
                    textAlign: 'center', 
                    borderRadius: 2, 
                    cursor: 'pointer',
                    border: '1px solid #e0e0e0',
                    '&:hover': { 
                      bgcolor: 'rgba(255, 107, 53, 0.1)',
                      transform: 'translateY(-2px)',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    },
                    transition: 'all 0.2s ease-in-out'
                  }}
                  onClick={() => addUserActivity('Navigation', 'Accès aux avis de crédit')}
                  component={Link}
                  to="/dette-du-tresor/avis-credit"
                >
                  <CreditIcon sx={{ fontSize: 40, color: '#FF6B35', mb: 1 }} />
                  <Typography variant="body2">Avis de Crédit</Typography>
                </Paper>
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 2, 
                    textAlign: 'center', 
                    borderRadius: 2, 
                    cursor: 'pointer',
                    border: '1px solid #e0e0e0',
                    '&:hover': { 
                      bgcolor: 'rgba(255, 107, 53, 0.1)',
                      transform: 'translateY(-2px)',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    },
                    transition: 'all 0.2s ease-in-out'
                  }}
                  onClick={() => addUserActivity('Navigation', 'Accès aux ordres de paiement')}
                  component={Link}
                  to="/dette-du-tresor/ordres-paiement"
                >
                  <PaymentIcon sx={{ fontSize: 40, color: '#FF6B35', mb: 1 }} />
                  <Typography variant="body2">Ordres de Paiement</Typography>
                </Paper>
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 2, 
                    textAlign: 'center', 
                    borderRadius: 2, 
                    cursor: 'pointer',
                    border: '1px solid #e0e0e0',
                    '&:hover': { 
                      bgcolor: 'rgba(255, 107, 53, 0.1)',
                      transform: 'translateY(-2px)',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    },
                    transition: 'all 0.2s ease-in-out'
                  }}
                  onClick={() => addUserActivity('Navigation', 'Accès aux avis de débit')}
                  component={Link}
                  to="/dette-du-tresor/avis-debit"
                >
                  <DebitIcon sx={{ fontSize: 40, color: '#FF6B35', mb: 1 }} />
                  <Typography variant="body2">Avis de Débit</Typography>
                </Paper>
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 2, 
                    textAlign: 'center', 
                    borderRadius: 2, 
                    cursor: 'pointer',
                    border: '1px solid #e0e0e0',
                    '&:hover': { 
                      bgcolor: 'rgba(255, 107, 53, 0.1)',
                      transform: 'translateY(-2px)',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    },
                    transition: 'all 0.2s ease-in-out'
                  }}
                  onClick={() => addUserActivity('Navigation', 'Accès aux lettres de règlement')}
                  component={Link}
                  to="/dette-du-tresor/lettres-reglement"
                >
                  <DocumentIcon sx={{ fontSize: 40, color: '#FF6B35', mb: 1 }} />
                  <Typography variant="body2">Lettres de Règlement</Typography>
                </Paper>
              </Grid>
              <Grid size={{ xs: 6, sm: 4, md: 2 }}>
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 2, 
                    textAlign: 'center', 
                    borderRadius: 2, 
                    cursor: 'pointer',
                    border: '1px solid #e0e0e0',
                    '&:hover': { 
                      bgcolor: 'rgba(255, 107, 53, 0.1)',
                      transform: 'translateY(-2px)',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    },
                    transition: 'all 0.2s ease-in-out'
                  }}
                  onClick={() => addUserActivity('Navigation', 'Accès à l\'échéancier')}
                  component={Link}
                  to="/dette-du-tresor/echeancier"
                >
                  <TimelineIcon sx={{ fontSize: 40, color: '#FF6B35', mb: 1 }} />
                  <Typography variant="body2">Échéancier</Typography>
                </Paper>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default DetteDuTresorDashboard;