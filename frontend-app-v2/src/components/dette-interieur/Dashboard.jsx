import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate, useLocation, Outlet, Link } from 'react-router-dom';
import { 
  Box, 
  Typography, 
  Card, 
  CardContent, 
  Grid, 
  Avatar,
  Button,
  AppBar,
  Toolbar,
  IconButton,
  Menu,
  MenuItem,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Drawer,
  CssBaseline,
  Chip,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress
} from '@mui/material';
import { 
  Dashboard as DashboardIcon, 
  AccountCircle, 
  Logout, 
  Person,
  Notifications,
  Gavel,
  Build,
  AccountBalance,
  Euro,
  Assignment,
  CreditCard,
  TrendingUp,
  List as ListIcon,
  Assessment,
  History,
  AccountBalanceWallet
} from '@mui/icons-material';
// Import GDP logo
import GDPLogo from '../../assets/gdp.png';
import './Dashboard.css';

const DetteInterieurDashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = React.useState(null);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [debtStatistics, setDebtStatistics] = useState([]);
  const [activeAdjudicationsCount, setActiveAdjudicationsCount] = useState(0);
  const [activeBondsCount, setActiveBondsCount] = useState(0);
  const [loadingStats, setLoadingStats] = useState(true);
  const [integrationStatus, setIntegrationStatus] = useState(null);
  const [loadingIntegration, setLoadingIntegration] = useState(true);
  const [userActivityHistory, setUserActivityHistory] = useState([]);

  const handleMenu = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    handleClose();
  };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  // Initialize with mock data for demonstration
  useEffect(() => {
    // Initialize dashboard data
    fetchNotifications();
    fetchCalculationData();
    fetchIntegrationData();
    initializeUserActivityHistory();
  }, []);

  const initializeUserActivityHistory = () => {
    // Initialize with login event
    const loginEvent = {
      id: Date.now(),
      timestamp: new Date().toISOString(),
      action: 'Connexion au tableau de bord',
      details: `Utilisateur ${user?.username} connecté`
    };
    const history = [loginEvent];
    setUserActivityHistory(history);
  };

  const fetchNotifications = async () => {
    try {
      // Simulate fetching notifications
      const mockNotifications = [
        { id: 1, message: "Nouvelle adjudication créée: ADJ1000", time: "2 heures ago", read: false },
        { id: 2, message: "Commission mise à jour: COM2000", time: "5 heures ago", read: false },
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
      
      // Simulate fetching data
      setTimeout(() => {
        // Mock statistics data
        setDebtStatistics([
          ['MAD', '8500000.00', 8],
          ['EUR', '1200000.00', 3],
          ['USD', '2500000.00', 5]
        ]);
        
        setActiveAdjudicationsCount(12);
        setActiveBondsCount(8);
        setLoadingStats(false);
      }, 1000);
    } catch (error) {
      console.error('Error fetching calculation data:', error);
      // Fallback to mock data in case of error
      setDebtStatistics([
        ['MAD', '8500000.00', 8],
        ['EUR', '1200000.00', 3],
        ['USD', '2500000.00', 5]
      ]);
      setActiveAdjudicationsCount(12);
      setActiveBondsCount(8);
      setLoadingStats(false);
    }
  };

  const fetchIntegrationData = async () => {
    try {
      setLoadingIntegration(true);
      
      // Simulate fetching integration status
      setTimeout(() => {
        setIntegrationStatus({
          maroclear: { status: 'connected' },
          bam: { status: 'connected' },
          timestamp: new Date().toISOString()
        });
        setLoadingIntegration(false);
      }, 1000);
    } catch (error) {
      console.error('Error fetching integration data:', error);
      setLoadingIntegration(false);
    }
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
          to="/dette-interieur" 
          selected={location.pathname === '/dette-interieur'}
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
          to="/dette-interieur/adjudications" 
          selected={location.pathname === '/dette-interieur/adjudications'}
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
          to="/dette-interieur/avis-adjudication" 
          selected={location.pathname === '/dette-interieur/avis-adjudication'}
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
          <ListItemText primary="Avis Adjudication" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/dette-interieur/bon-equipement" 
          selected={location.pathname === '/dette-interieur/bon-equipement'}
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
          to="/dette-interieur/avis-bon-equipement" 
          selected={location.pathname === '/dette-interieur/avis-bon-equipement'}
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
            <CreditCard sx={{ color: '#FF6B35' }} />
          </ListItemIcon>
          <ListItemText primary="Avis Bons Équipement" />
        </ListItem>
        <ListItem 
          component={Link} 
          to="/dette-interieur/commissions" 
          selected={location.pathname === '/dette-interieur/commissions'}
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
          to="/dette-interieur/interet-depot" 
          selected={location.pathname === '/dette-interieur/interet-depot'}
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
        
      </List>
    </div>
  );

  // Show dashboard home content when on the main dashboard route
  const isDashboardHome = location.pathname === '/dette-interieur' || location.pathname === '/dette-interieur/';

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
                    Bienvenue, {user?.username || 'Utilisateur'}!
                  </Typography>
                  <Typography variant="body1">
                    Tableau de bord de gestion de la dette intérieure
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
                    label={user?.roles?.includes('dette interieur') 
                      ? 'Gestionnaire de la Dette Intérieure' 
                      : (user?.roles ? user.roles.join(', ') : 'Rôle non défini')} 
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
          {/* Calculation Services Section */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid size={12}>
              <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Assessment sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    Statistiques de la Dette
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
                          <Gavel sx={{ fontSize: 30, color: '#FF6B35', mr: 1 }} />
                          <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            Adjudications Actives
                          </Typography>
                        </Box>
                        <Typography variant="h3" sx={{ color: '#FF6B35', fontWeight: 'bold', mt: 'auto' }}>
                          {activeAdjudicationsCount}
                        </Typography>
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
                          <Build sx={{ fontSize: 30, color: '#4caf50', mr: 1 }} />
                          <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            Bons Actifs
                          </Typography>
                        </Box>
                        <Typography variant="h3" sx={{ color: '#4caf50', fontWeight: 'bold', mt: 'auto' }}>
                          {activeBondsCount}
                        </Typography>
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
                          <AccountBalanceWallet sx={{ fontSize: 30, color: '#2196f3', mr: 1 }} />
                          <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            Devises
                          </Typography>
                        </Box>
                        <Typography variant="h3" sx={{ color: '#2196f3', fontWeight: 'bold', mt: 'auto' }}>
                          {debtStatistics.length}
                        </Typography>
                      </Paper>
                    </Grid>
                    
                    {/* Debt by Currency Table */}
                    {debtStatistics.length > 0 && (
                      <Grid size={12}>
                        <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0', mt: 2 }}>
                          <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 'bold', color: '#333' }}>
                            Dette par Devise
                          </Typography>
                          <TableContainer>
                            <Table>
                              <TableHead>
                                <TableRow sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
                                  <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Devise</TableCell>
                                  <TableCell sx={{ fontWeight: 'bold', color: '#333' }} align="right">Montant Total</TableCell>
                                  <TableCell sx={{ fontWeight: 'bold', color: '#333' }} align="right">Nombre d'Éléments</TableCell>
                                </TableRow>
                              </TableHead>
                              <TableBody>
                                {debtStatistics.map((stat, index) => (
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
                                    <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }}>{stat[0]}</TableCell>
                                    <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }} align="right">{parseFloat(stat[1]).toLocaleString('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</TableCell>
                                    <TableCell sx={{ fontWeight: index % 2 === 0 ? 500 : 400 }} align="right">{stat[2]}</TableCell>
                                  </TableRow>
                                ))}
                              </TableBody>
                            </Table>
                          </TableContainer>
                        </Paper>
                      </Grid>
                    )}
                  </Grid>
                )}
              </Paper>
            </Grid>
          </Grid>
          
          {/* Integration Status Section */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid size={12}>
              <Paper elevation={0} sx={{ p: 2, borderRadius: 2, border: '1px solid #e0e0e0' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Notifications sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    Statut des Intégrations Externes
                  </Typography>
                </Box>
                {loadingIntegration ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                  </Box>
                ) : (
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <Paper elevation={0} sx={{ p: 2, borderRadius: 2, height: '100%', border: '1px solid #e0e0e0' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <Box sx={{ 
                            width: 12, 
                            height: 12, 
                            borderRadius: '50%', 
                            bgcolor: integrationStatus?.maroclear?.status === 'connected' ? '#4caf50' : '#f44336',
                            mr: 1 
                          }} />
                          <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                            Maroclear
                          </Typography>
                        </Box>
                        <Typography variant="body2" color="textSecondary">
                          {integrationStatus?.maroclear?.status === 'connected' 
                            ? 'Connecté et fonctionnel' 
                            : 'Déconnecté ou en erreur'}
                        </Typography>
                        <Typography variant="caption" color="textSecondary">
                          Dernière vérification: {integrationStatus?.timestamp 
                            ? new Date(integrationStatus.timestamp).toLocaleString('fr-FR') 
                            : 'N/A'}
                        </Typography>
                      </Paper>
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <Paper elevation={0} sx={{ p: 2, borderRadius: 2, height: '100%', border: '1px solid #e0e0e0' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <Box sx={{ 
                            width: 12, 
                            height: 12, 
                            borderRadius: '50%', 
                            bgcolor: integrationStatus?.bam?.status === 'connected' ? '#4caf50' : '#f44336',
                            mr: 1 
                          }} />
                          <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                            Bank Al-Maghrib (BAM)
                          </Typography>
                        </Box>
                        <Typography variant="body2" color="textSecondary">
                          {integrationStatus?.bam?.status === 'connected' 
                            ? 'Connecté et fonctionnel' 
                            : 'Déconnecté ou en erreur'}
                        </Typography>
                        <Typography variant="caption" color="textSecondary">
                          Dernière vérification: {integrationStatus?.timestamp 
                            ? new Date(integrationStatus.timestamp).toLocaleString('fr-FR') 
                            : 'N/A'}
                        </Typography>
                      </Paper>
                    </Grid>
                    <Grid size={12}>
                      <Paper elevation={0} sx={{ p: 2, borderRadius: 2, bgcolor: '#f5f5f5', border: '1px solid #e0e0e0' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <Box sx={{ 
                            width: 16, 
                            height: 16, 
                            borderRadius: '50%', 
                            bgcolor: (integrationStatus?.maroclear?.status === 'connected' && integrationStatus?.bam?.status === 'connected') 
                              ? '#4caf50' : '#f44336',
                            mr: 1 
                          }} />
                          <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                            Statut Global: {
                              (integrationStatus?.maroclear?.status === 'connected' && integrationStatus?.bam?.status === 'connected') 
                                ? 'Tous les systèmes sont opérationnels' 
                                : 'Problème de connectivité détecté'}
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
                  <History sx={{ fontSize: 24, color: '#FF6B35', mr: 1 }} />
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
            <DashboardIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Gestion de la Dette Intérieure
          </Typography>
          <IconButton color="inherit">
            <Notifications />
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
          <Button color="inherit" onClick={handleLogout} startIcon={<Logout />}>
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

      {isDashboardHome ? (
        <DashboardContent />
      ) : (
        // Render the individual component based on the route
        <Box component="main" sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - ${drawerWidth}px)` } }}>
          <Toolbar />
          <Outlet />
        </Box>
      )}
    </Box>
  );
};

export default DetteInterieurDashboard;