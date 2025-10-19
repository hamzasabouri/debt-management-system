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
  Menu,
  MenuItem,
  Paper,
  Popover,
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
  AccountBalance as ProjectIcon,
  AccountCircle as UserIcon,
  Assessment as AnalyticsIcon,
  Dashboard as DashboardIcon,
  Description as DocumentIcon,
  History as HistoryIcon,
  Home as HomeIcon,
  Menu as MenuIcon,
  Money as AdvanceIcon,
  Notifications as NotificationsIcon,
  Payment as PaymentIcon,
  Timeline as TimelineIcon,
  AccountBalanceWallet as FinanceIcon,
  CheckCircle as ActiveIcon,
  AssignmentTurnedIn as CompletedIcon,
  Apps as TotalIcon,
  Logout as LogoutIcon
} from '@mui/icons-material';
import ProjetsList from './ProjetsList';
import AvancesList from './AvancesList';
import PiecesJustificativesList from './PiecesJustificativesList';
// Import services to fetch real data
import { getAllProjets, getProjetStatistics, medaApi } from '../../services/meda/projetsService';
import { getAllAvances } from '../../services/meda/avancesService';
import { getAllPiecesJustificatives } from '../../services/meda/piecesJustificativesService';
// Import setAuthToken functions from all services
import { setAuthToken as setProjetsAuthToken } from '../../services/meda/projetsService';
import { setAuthToken as setAvancesAuthToken } from '../../services/meda/avancesService';
import { setAuthToken as setPiecesJustificativesAuthToken } from '../../services/meda/piecesJustificativesService';
import { initAuth } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
// Import GDP logo
import GDPLogo from '../../assets/gdp.png';

const MEDADashboard = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [activeTab, setActiveTab] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [projectStatistics, setProjectStatistics] = useState([]);
  const [activeProjectsCount, setActiveProjectsCount] = useState(0);
  const [completedProjectsCount, setCompletedProjectsCount] = useState(0);
  const [loadingStats, setLoadingStats] = useState(true);
  const [integrationStatus, setIntegrationStatus] = useState(null);
  const [loadingIntegration, setLoadingIntegration] = useState(true);
  const [userActivityHistory, setUserActivityHistory] = useState([]);
  const [projectStats, setProjectStats] = useState(null);
  const [projectsData, setProjectsData] = useState([]);
  const [financialSummary, setFinancialSummary] = useState(null);
  const [loadingFinancial, setLoadingFinancial] = useState(true);
  const [anchorEl, setAnchorEl] = useState(null);
  const [notificationAnchorEl, setNotificationAnchorEl] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();

  // Set auth token for all services when component mounts
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // Set the auth token for each service
      setProjetsAuthToken(token);
      setAvancesAuthToken(token);
      setPiecesJustificativesAuthToken(token);
      
      // Set user info from auth context
      setUserInfo(user);
    } else {
      // If no token, redirect to login
      navigate('/');
      return;
    }
  }, []); // Empty dependency array to run only once on mount

  // Additional effect to ensure token is set when location changes
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // Set the auth token for each service
      setProjetsAuthToken(token);
      setAvancesAuthToken(token);
      setPiecesJustificativesAuthToken(token);
    }
  }, [location]); // Run when location changes

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    
    if (!token) {
      navigate('/');
      return;
    }
    
    // Also check if user info is available
    const userInfoStr = localStorage.getItem('userInfo');
    if (!userInfoStr) {
      navigate('/');
      return;
    }
    
    // Initialize dashboard data - with proper error handling to prevent infinite loops
    const initializeData = async () => {
      try {
        await fetchNotifications();
        await fetchProjectData();
        await fetchFinancialData();
        await fetchIntegrationData();
        await initializeUserActivityHistory();
      } catch (error) {
        console.error('Error initializing dashboard data:', error);
      }
    };
    
    initializeData();
  }, []); // Empty dependency array to run only once on mount

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
        {
          id: 1,
          title: 'Nouveau projet ajouté',
          message: 'Le projet "Infrastructure routière" a été ajouté',
          timestamp: new Date(Date.now() - 3600000).toISOString(),
          read: false,
          type: 'info'
        },
        {
          id: 2,
          title: 'Avance approuvée',
          message: 'L\'avance pour le projet "Équipement hospitalier" a été approuvée',
          timestamp: new Date(Date.now() - 86400000).toISOString(),
          read: true,
          type: 'success'
        }
      ];
      
      setNotifications(mockNotifications);
      setUnreadCount(mockNotifications.filter(n => !n.read).length);
    } catch (error) {
      console.error('Error fetching notifications:', error);
    }
  };

  const fetchProjectData = async () => {
    try {
      setLoadingStats(true);
      // Ensure token is set before making request
      const token = localStorage.getItem('token');
      if (token) {
        setProjetsAuthToken(token);
      }
      
      // Fetch project statistics
      const statsResponse = await getProjetStatistics();
      setProjectStats(statsResponse);
      
      // Fetch all projects
      const projectsResponse = await getAllProjets();
      const projects = Array.isArray(projectsResponse) ? projectsResponse : (projectsResponse.data || []);
      setProjectsData(projects);
      
      // Calculate counts
      const activeCount = projects.filter(p => p.statut === 'ACTIF').length;
      const completedCount = projects.filter(p => p.statut === 'TERMINE').length;
      
      setActiveProjectsCount(activeCount);
      setCompletedProjectsCount(completedCount);
      setProjectStatistics([
        { name: 'Projets Actifs', value: activeCount, icon: ActiveIcon },
        { name: 'Projets Terminés', value: completedCount, icon: CompletedIcon },
        { name: 'Total Projets', value: projects.length, icon: TotalIcon }
      ]);
    } catch (error) {
      console.error('Error fetching project data:', error);
    } finally {
      setLoadingStats(false);
    }
  };

  const fetchFinancialData = async () => {
    try {
      setLoadingFinancial(true);
      // Ensure token is set before making request
      const token = localStorage.getItem('token');
      if (token) {
        setProjetsAuthToken(token);
      }
      
      // Fetch financial summary
      const financialResponse = await getProjetStatistics();
      setFinancialSummary(financialResponse);
    } catch (error) {
      console.error('Error fetching financial data:', error);
    } finally {
      setLoadingFinancial(false);
    }
  };

  const fetchIntegrationData = async () => {
    try {
      setLoadingIntegration(true);
      // Simulate fetching integration status
      const mockStatus = {
        bam: { status: 'connected', lastCheck: new Date().toISOString() },
        comptabilite: { status: 'connected', lastCheck: new Date().toISOString() },
        timestamp: new Date().toISOString()
      };
      setIntegrationStatus(mockStatus);
    } catch (error) {
      console.error('Error fetching integration data:', error);
    } finally {
      setLoadingIntegration(false);
    }
  };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleTabChange = (tabIndex) => {
    setActiveTab(tabIndex);
    // Add activity to history
    addUserActivity('Navigation', `Changement d'onglet vers ${tabIndex === 0 ? 'Tableau de bord' : tabIndex === 1 ? 'Projets' : tabIndex === 2 ? 'Avances' : 'Pièces justificatives'}`);
  };

  const handleUserMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleUserMenuClose = () => {
    setAnchorEl(null);
  };

  const handleNotificationsClick = (event) => {
    setNotificationAnchorEl(event.currentTarget);
  };

  const handleNotificationsClose = () => {
    setNotificationAnchorEl(null);
  };

  const handleProfileClick = () => {
    navigate('/profile');
    handleUserMenuClose();
  };

  const handleSettingsClick = () => {
    navigate('/settings');
    handleUserMenuClose();
  };

  const handleLogout = () => {
    // Clear localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    
    // Redirect to login
    navigate('/');
  };

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
          button={true}
          selected={activeTab === 0}
          onClick={() => handleTabChange(0)}
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
          <ListItemText primary="Tableau de bord" />
        </ListItem>
        <ListItem 
          button={true}
          selected={activeTab === 1}
          onClick={() => handleTabChange(1)}
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
          button={true}
          selected={activeTab === 2}
          onClick={() => handleTabChange(2)}
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
          button={true}
          selected={activeTab === 3}
          onClick={() => handleTabChange(3)}
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
          <ListItemText primary="Pièces justificatives" />
        </ListItem>
      </List>
      <Divider />
      
    </div>
  );

  const formatCurrency = (amount, currency = 'MAD') => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2
    }).format(amount);
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'actif': return '#4caf50';
      case 'termine': return '#2196f3';
      case 'annule': return '#f44336';
      default: return '#9e9e9e';
    }
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar 
        position="fixed" 
        sx={{
          width: { sm: `calc(100% - 240px)` },
          ml: { sm: `240px` },
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
            Système de Gestion MEDA
          </Typography>
          <IconButton color="inherit" onClick={handleNotificationsClick}>
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
          <Button 
            color="inherit" 
            onClick={handleUserMenuOpen}
            sx={{ display: 'flex', alignItems: 'center' }}
          >
            <UserIcon sx={{ mr: 1 }} />
            {userInfo ? userInfo.nomComplet : 'Utilisateur'}
          </Button>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleUserMenuClose}
            sx={{ mt: 1 }}
          >
            
            <Divider />
            <MenuItem onClick={handleLogout}>
              <LogoutIcon sx={{ mr: 1 }} />
              Déconnecter
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      
      {/* Notifications Popover */}
      <Popover
        open={Boolean(notificationAnchorEl)}
        anchorEl={notificationAnchorEl}
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
              Notifications & Intégrations
            </Typography>
          </Box>
        </Box>
        
        {/* Notifications List */}
        <Box sx={{ p: 2, maxHeight: 300, overflowY: 'auto' }}>
          {notifications.length > 0 ? (
            <List>
              {notifications.map((notification) => (
                <React.Fragment key={notification.id}>
                  <ListItem sx={{ py: 1 }}>
                    <ListItemText 
                      primary={notification.title}
                      secondary={
                        <>
                          <Typography component="span" variant="body2" color="textSecondary">
                            {notification.message}
                          </Typography>
                          <br />
                          <Typography component="span" variant="caption" color="textSecondary">
                            {new Date(notification.timestamp).toLocaleString('fr-FR')}
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
              Aucune notification
            </Typography>
          )}
        </Box>
        
        <Divider />
        
        {/* Integration Status Section in Popover */}
        <Box sx={{ p: 2 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 2, color: '#333' }}>
            Statut des Intégrations Externes
          </Typography>
          
          {loadingIntegration ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
              <CircularProgress size={24} />
            </Box>
          ) : (
            <Grid container spacing={2}>
              <Grid size={{ xs: 12 }}>
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 1.5, 
                    borderRadius: 1, 
                    border: '1px solid #e0e0e0'
                  }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <Box sx={{ 
                      width: 10, 
                      height: 10, 
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
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 1.5, 
                    borderRadius: 1, 
                    border: '1px solid #e0e0e0'
                  }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <Box sx={{ 
                      width: 10, 
                      height: 10, 
                      borderRadius: '50%', 
                      bgcolor: integrationStatus?.comptabilite?.status === 'connected' ? '#4caf50' : '#f44336',
                      mr: 1 
                    }} />
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                      Service de Comptabilité
                    </Typography>
                  </Box>
                  <Typography variant="caption" color="textSecondary">
                    {integrationStatus?.comptabilite?.status === 'connected' 
                      ? 'Connecté' 
                      : 'Déconnecté'}
                  </Typography>
                </Paper>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Paper 
                  elevation={0} 
                  sx={{ 
                    p: 1.5, 
                    borderRadius: 1, 
                    border: '1px solid #e0e0e0'
                  }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Box sx={{ 
                      width: 12, 
                      height: 12, 
                      borderRadius: '50%', 
                      bgcolor: (integrationStatus?.bam?.status === 'connected' && integrationStatus?.comptabilite?.status === 'connected') 
                        ? '#4caf50' : '#f44336',
                      mr: 1 
                    }} />
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                      {integrationStatus?.bam?.status === 'connected' && integrationStatus?.comptabilite?.status === 'connected' 
                        ? 'Tous les systèmes sont opérationnels' 
                        : 'Problème de connectivité détecté'}
                    </Typography>
                  </Box>
                </Paper>
              </Grid>
            </Grid>
          )}
        </Box>
      </Popover>
      <Box
        component="nav"
        sx={{ width: { sm: 240 }, flexShrink: { sm: 0 } }}
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
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: 240 },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', 'sm': 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: 240 },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - 240px)` } }}
      >
        <Toolbar />
        {activeTab === 0 && (
          <DashboardContent 
            projectStats={projectStats}
            projectsData={projectsData}
            financialSummary={financialSummary}
            integrationStatus={integrationStatus}
            loadingStats={loadingStats}
            loadingFinancial={loadingFinancial}
            loadingIntegration={loadingIntegration}
            formatCurrency={formatCurrency}
            getStatusColor={getStatusColor}
            FinanceIcon={FinanceIcon}
            AnalyticsIcon={AnalyticsIcon}
            ProjectIcon={ProjectIcon}
            HomeIcon={HomeIcon}
            NotificationsIcon={NotificationsIcon}
          />
        )}
        {activeTab === 1 && <ProjetsList />}
        {activeTab === 2 && <AvancesList />}
        {activeTab === 3 && <PiecesJustificativesList />}
      </Box>
    </Box>
  );
};

// Dashboard Content Component
const DashboardContent = ({ 
  projectStats, 
  projectsData, 
  financialSummary, 
  integrationStatus, 
  loadingStats, 
  loadingFinancial, 
  loadingIntegration, 
  formatCurrency, 
  getStatusColor,
  FinanceIcon,
  AnalyticsIcon,
  ProjectIcon,
  HomeIcon,
  NotificationsIcon
}) => {
  return (
    <>
      {/* Welcome Section */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12 }}>
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
                  Bienvenue dans le Système de Gestion MEDA
                </Typography>
                <Typography variant="body1">
                  Gérez vos projets, avances et documents de manière centralisée
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
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>
      
      {/* Recent Projects Section */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12 }}>
          <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <ProjectIcon sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
              <Typography variant="h6" sx={{ color: '#333', fontWeight: 'bold' }}>
                Projets Récents
              </Typography>
            </Box>
            {loadingStats ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                <CircularProgress />
              </Box>
            ) : (
              <Grid container spacing={3}>
                <Grid size={{ xs: 12 }}>
                  <TableContainer 
                    sx={{ 
                      borderRadius: 2, 
                      border: '1px solid #e0e0e0',
                      boxShadow: '0 2px 10px rgba(0,0,0,0.05)'
                    }}
                  >
                    <Table size="small">
                      <TableHead>
                        <TableRow sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
                          <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Nom du Projet</TableCell>
                          <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Début</TableCell>
                          <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de Fin</TableCell>
                          <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Montant</TableCell>
                          <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
                          <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {projectsData?.slice(0, 3).map((project) => (
                          <TableRow 
                            key={project.id}
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
                            <TableCell sx={{ fontWeight: '500' }}>{project.nomProjet}</TableCell>
                            <TableCell>{project.dateDebut ? new Date(project.dateDebut).toLocaleDateString('fr-FR') : 'N/A'}</TableCell>
                            <TableCell>{project.dateFin ? new Date(project.dateFin).toLocaleDateString('fr-FR') : 'N/A'}</TableCell>
                            <TableCell>{formatCurrency(project.montantTotal, project.devise)}</TableCell>
                            <TableCell>{project.devise}</TableCell>
                            <TableCell>
                              <Box
                                sx={{
                                  display: 'inline-block',
                                  px: 1,
                                  py: 0.5,
                                  borderRadius: 1,
                                  bgcolor: getStatusColor(project.statut),
                                  color: 'white',
                                  fontSize: '0.75rem',
                                  fontWeight: 'bold'
                                }}
                              >
                                {project.statut}
                              </Box>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Grid>
              </Grid>
            )}
          </Paper>
        </Grid>
      </Grid>
      
      {/* Integration Status Section - This will be moved to notifications popover */}
    </>
  );
};

export default MEDADashboard;