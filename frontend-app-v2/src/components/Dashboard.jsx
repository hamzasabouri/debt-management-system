import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
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
  Divider
} from '@mui/material';
import { 
  Dashboard as DashboardIcon, 
  AccountCircle, 
  Logout, 
  Person,
  Assignment,
  Payment,
  Description,
  Mail,
  Notifications,
  AccountBalance,
  Group,
  Security,
  AdminPanelSettings
} from '@mui/icons-material';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = React.useState(null);

  useEffect(() => {
    // Check if user has admin role and redirect to admin dashboard
    if (user?.roles?.includes('admin')) {
      navigate('/admin');
    }
  }, [user, navigate]);

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

  const handleNavigateToMEDA = () => {
    navigate('/meda');
  };

  const handleNavigateToDetteTresor = () => {
    navigate('/dette-du-tresor');
  };

  const handleNavigateToDetteInterieur = () => {
    navigate('/dette-interieur');
  };

  const handleNavigateToUsers = () => {
    navigate('/admin/users');
  };

  // Check if user has admin role
  const hasAdminRole = user?.roles?.includes('admin');

  // If user is admin, don't show the regular dashboard content
  if (hasAdminRole) {
    return null; // or a loading spinner while redirecting
  }

  return (
    <Box sx={{ flexGrow: 1 }}>
      {/* En-tête avec barre de navigation */}
      <AppBar position="static" sx={{ backgroundColor: '#fff', boxShadow: '0 2px 10px rgba(0,0,0,0.1)' }}>
        <Toolbar>
          <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1 }}>
            <DashboardIcon sx={{ color: '#FF6B35', mr: 1 }} />
            <Typography variant="h6" component="div" sx={{ color: '#2D3748', fontWeight: 'bold' }}>
              Tableau de Bord
            </Typography>
          </Box>
          
          <IconButton
            size="large"
            aria-label="notifications"
            color="inherit"
            sx={{ mr: 2, color: '#2D3748' }}
          >
            <Notifications />
          </IconButton>
          
          <IconButton
            size="large"
            aria-label="account of current user"
            aria-controls="menu-appbar"
            aria-haspopup="true"
            onClick={handleMenu}
            color="inherit"
            sx={{ color: '#2D3748' }}
          >
            <AccountCircle />
          </IconButton>
          
          <Menu
            id="menu-appbar"
            anchorEl={anchorEl}
            anchorOrigin={{
              vertical: 'top',
              horizontal: 'right',
            }}
            keepMounted
            transformOrigin={{
              vertical: 'top',
              horizontal: 'right',
            }}
            open={Boolean(anchorEl)}
            onClose={handleClose}
          >
            <MenuItem>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Person sx={{ mr: 1 }} />
                <Box>
                  <Typography variant="subtitle2">{user?.username}</Typography>
                  <Typography variant="body2" color="textSecondary">
                    {user?.roles?.join(', ')}
                  </Typography>
                </Box>
              </Box>
            </MenuItem>
            <Divider />
            <MenuItem onClick={handleLogout}>
              <Logout sx={{ mr: 1 }} />
              Déconnexion
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      {/* Contenu principal */}
      <Box sx={{ p: 3 }}>
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" component="h1" sx={{ color: '#2D3748', fontWeight: 'bold', mb: 1 }}>
            Bienvenue, {user?.username}!
          </Typography>
          <Typography variant="subtitle1" sx={{ color: '#4A5568' }}>
            Voici un aperçu de votre activité récente.
          </Typography>
        </Box>

        {/* Cartes de statistiques */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card sx={{ 
              height: '100%', 
              borderRadius: 2, 
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
              border: '1px solid #E2E8F0',
              transition: 'transform 0.2s, box-shadow 0.2s',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
              }
            }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Avatar sx={{ bgcolor: '#FF6B35', mr: 2 }}>
                    <Assignment />
                  </Avatar>
                  <Typography variant="h6" sx={{ color: '#2D3748' }}>
                    Tâches
                  </Typography>
                </Box>
                <Typography variant="h4" sx={{ color: '#2D3748', fontWeight: 'bold', mb: 1 }}>
                  12
                </Typography>
                <Typography variant="body2" sx={{ color: '#4A5568' }}>
                  En cours
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card sx={{ 
              height: '100%', 
              borderRadius: 2, 
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
              border: '1px solid #E2E8F0',
              transition: 'transform 0.2s, box-shadow 0.2s',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
              }
            }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Avatar sx={{ bgcolor: '#38A169', mr: 2 }}>
                    <Payment />
                  </Avatar>
                  <Typography variant="h6" sx={{ color: '#2D3748' }}>
                    Paiements
                  </Typography>
                </Box>
                <Typography variant="h4" sx={{ color: '#2D3748', fontWeight: 'bold', mb: 1 }}>
                  8
                </Typography>
                <Typography variant="body2" sx={{ color: '#4A5568' }}>
                  À traiter
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card sx={{ 
              height: '100%', 
              borderRadius: 2, 
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
              border: '1px solid #E2E8F0',
              transition: 'transform 0.2s, box-shadow 0.2s',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
              }
            }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Avatar sx={{ bgcolor: '#3182CE', mr: 2 }}>
                    <Description />
                  </Avatar>
                  <Typography variant="h6" sx={{ color: '#2D3748' }}>
                    Documents
                  </Typography>
                </Box>
                <Typography variant="h4" sx={{ color: '#2D3748', fontWeight: 'bold', mb: 1 }}>
                  24
                </Typography>
                <Typography variant="body2" sx={{ color: '#4A5568' }}>
                  Nouveaux
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card sx={{ 
              height: '100%', 
              borderRadius: 2, 
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
              border: '1px solid #E2E8F0',
              transition: 'transform 0.2s, box-shadow 0.2s',
              '&:hover': {
                transform: 'translateY(-4px)',
                boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
              }
            }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Avatar sx={{ bgcolor: '#805AD5', mr: 2 }}>
                    <Mail />
                  </Avatar>
                  <Typography variant="h6" sx={{ color: '#2D3748' }}>
                    Messages
                  </Typography>
                </Box>
                <Typography variant="h4" sx={{ color: '#2D3748', fontWeight: 'bold', mb: 1 }}>
                  5
                </Typography>
                <Typography variant="body2" sx={{ color: '#4A5568' }}>
                  Non lus
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          {/* MEDA Service Card - Only show if user has MEDA role */}
          {user?.roles?.includes('MEDA') && (
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Card 
                sx={{ 
                  height: '100%', 
                  borderRadius: 2, 
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  border: '1px solid #E2E8F0',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
                    cursor: 'pointer'
                  }
                }}
                onClick={handleNavigateToMEDA}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Avatar sx={{ bgcolor: '#38A169', mr: 2 }}>
                      <AccountBalance />
                    </Avatar>
                    <Typography variant="h6" sx={{ color: '#2D3748' }}>
                      MEDA
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ color: '#4A5568', mb: 1 }}>
                    Programmes et Fonds de Trésorerie
                  </Typography>
                  <Button 
                    variant="outlined" 
                    size="small"
                    sx={{ 
                      borderColor: '#38A169',
                      color: '#38A169',
                      '&:hover': {
                        borderColor: '#2F855A',
                        backgroundColor: 'rgba(56, 161, 105, 0.04)',
                      },
                      borderRadius: 2,
                      mt: 1
                    }}
                  >
                    Accéder
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          )}

          {/* Dette Trésor Service Card - Only show if user has dette du tresor role */}
          {user?.roles?.includes('dette du tresor') && (
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Card 
                sx={{ 
                  height: '100%', 
                  borderRadius: 2, 
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  border: '1px solid #E2E8F0',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
                    cursor: 'pointer'
                  }
                }}
                onClick={handleNavigateToDetteTresor}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Avatar sx={{ bgcolor: '#FF6B35', mr: 2 }}>
                      <AccountBalance />
                    </Avatar>
                    <Typography variant="h6" sx={{ color: '#2D3748' }}>
                      Dette Trésor
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ color: '#4A5568', mb: 1 }}>
                    Gestion de la dette du trésor
                  </Typography>
                  <Button 
                    variant="outlined" 
                    size="small"
                    sx={{ 
                      borderColor: '#FF6B35',
                      color: '#FF6B35',
                      '&:hover': {
                        borderColor: '#E65A2B',
                        backgroundColor: 'rgba(255, 107, 53, 0.04)',
                      },
                      borderRadius: 2,
                      mt: 1
                    }}
                  >
                    Accéder
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          )}

          {/* Dette Intérieur Service Card - Only show if user has dette interieur role */}
          {user?.roles?.includes('dette interieur') && (
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Card 
                sx={{ 
                  height: '100%', 
                  borderRadius: 2, 
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  border: '1px solid #E2E8F0',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
                    cursor: 'pointer'
                  }
                }}
                onClick={handleNavigateToDetteInterieur}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Avatar sx={{ bgcolor: '#0088FE', mr: 2 }}>
                      <AccountBalance />
                    </Avatar>
                    <Typography variant="h6" sx={{ color: '#2D3748' }}>
                      Dette Intérieur
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ color: '#4A5568', mb: 1 }}>
                    Gestion de la dette intérieure
                  </Typography>
                  <Button 
                    variant="outlined" 
                    size="small"
                    sx={{ 
                      borderColor: '#0088FE',
                      color: '#0088FE',
                      '&:hover': {
                        borderColor: '#0066CC',
                        backgroundColor: 'rgba(0, 136, 254, 0.04)',
                      },
                      borderRadius: 2,
                      mt: 1
                    }}
                  >
                    Accéder
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          )}

          {/* Admin Service Cards - Only show if user has admin role */}
          {hasAdminRole && (
            <>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <Card 
                  sx={{ 
                    height: '100%', 
                    borderRadius: 2, 
                    boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                    border: '1px solid #E2E8F0',
                    transition: 'transform 0.2s, box-shadow 0.2s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
                      cursor: 'pointer'
                    }
                  }}
                  onClick={handleNavigateToUsers}
                >
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <Avatar sx={{ bgcolor: '#9c27b0', mr: 2 }}>
                        <Group />
                      </Avatar>
                      <Typography variant="h6" sx={{ color: '#2D3748' }}>
                        Utilisateurs
                      </Typography>
                    </Box>
                    <Typography variant="body2" sx={{ color: '#4A5568', mb: 1 }}>
                      Gestion des utilisateurs
                    </Typography>
                    <Button 
                      variant="outlined" 
                      size="small"
                      sx={{ 
                        borderColor: '#9c27b0',
                        color: '#9c27b0',
                        '&:hover': {
                          borderColor: '#7b1fa2',
                          backgroundColor: 'rgba(156, 39, 176, 0.04)',
                        },
                        borderRadius: 2,
                        mt: 1
                      }}
                    >
                      Gérer
                    </Button>
                  </CardContent>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <Card 
                  sx={{ 
                    height: '100%', 
                    borderRadius: 2, 
                    boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                    border: '1px solid #E2E8F0',
                    transition: 'transform 0.2s, box-shadow 0.2s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
                      cursor: 'pointer'
                    }
                  }}
                  onClick={handleNavigateToRoles}
                >
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <Avatar sx={{ bgcolor: '#ff9800', mr: 2 }}>
                        <Security />
                      </Avatar>
                      <Typography variant="h6" sx={{ color: '#2D3748' }}>
                        Rôles
                      </Typography>
                    </Box>
                    <Typography variant="body2" sx={{ color: '#4A5568', mb: 1 }}>
                      Gestion des rôles
                    </Typography>
                    <Button 
                      variant="outlined" 
                      size="small"
                      sx={{ 
                        borderColor: '#ff9800',
                        color: '#ff9800',
                        '&:hover': {
                          borderColor: '#f57c00',
                          backgroundColor: 'rgba(255, 152, 0, 0.04)',
                        },
                        borderRadius: 2,
                        mt: 1
                      }}
                    >
                      Gérer
                    </Button>
                  </CardContent>
                </Card>
              </Grid>
            </>
          )}
        </Grid>

        {/* Section des actions rapides */}
        <Box sx={{ mb: 4 }}>
          <Typography variant="h5" sx={{ color: '#2D3748', fontWeight: 'bold', mb: 2 }}>
            Actions Rapides
          </Typography>
          <Grid container spacing={2}>
            <Grid size={12}>
              <Button 
                variant="contained" 
                sx={{ 
                  bgcolor: '#FF6B35',
                  '&:hover': {
                    bgcolor: '#E65A2B',
                  },
                  borderRadius: 2,
                  py: 1.5,
                  px: 3
                }}
              >
                Nouvelle Tâche
              </Button>
            </Grid>
            <Grid size={12}>
              <Button 
                variant="outlined" 
                sx={{ 
                  borderColor: '#E2E8F0',
                  color: '#2D3748',
                  '&:hover': {
                    borderColor: '#CBD5E0',
                    backgroundColor: 'rgba(0, 0, 0, 0.02)',
                  },
                  borderRadius: 2,
                  py: 1.5,
                  px: 3
                }}
              >
                Voir les Rapports
              </Button>
            </Grid>
            <Grid size={12}>
              <Button 
                variant="outlined" 
                sx={{ 
                  borderColor: '#E2E8F0',
                  color: '#2D3748',
                  '&:hover': {
                    borderColor: '#CBD5E0',
                    backgroundColor: 'rgba(0, 0, 0, 0.02)',
                  },
                  borderRadius: 2,
                  py: 1.5,
                  px: 3
                }}
              >
                Paramètres
              </Button>
            </Grid>
          </Grid>
        </Box>
      </Box>
    </Box>
  );
};

export default Dashboard;