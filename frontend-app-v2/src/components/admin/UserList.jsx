import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  IconButton,
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Snackbar,
  Alert,
  Chip,
  Tooltip,
  CircularProgress,
  Container,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Checkbox,
  ListItemText,
  OutlinedInput,
  InputAdornment
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  Check as CheckIcon,
  Close as CloseIcon,
  Person as PersonIcon,
  Email as EmailIcon,
  Security as SecurityIcon,
  History as HistoryIcon
} from '@mui/icons-material';
import { useAuth } from '../../context/AuthContext';
import userService, { roleService, setUserAuthToken } from '../../services/admin/userService';
import { handle403Error } from '../../utils/authUtils';

const UserList = () => {
  const [users, setUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [openDialog, setOpenDialog] = useState(false);
  const [openUserFormDialog, setOpenUserFormDialog] = useState(false);
  const [openRolesDialog, setOpenRolesDialog] = useState(false);
  const [openAssignRolesDialog, setOpenAssignRolesDialog] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [userFormData, setUserFormData] = useState({
    username: '',
    email: '',
    password: '',
    nomComplet: '',
    statut: 'ACTIF'
  });
  const [allRoles, setAllRoles] = useState([]);
  const [selectedRoleIds, setSelectedRoleIds] = useState([]);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
  const { user: currentUser } = useAuth();

  const [error, setError] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      console.log('Setting auth token for admin service');
      setUserAuthToken(token);
    }
    
    fetchUsers();
    fetchAllRoles();
  }, []);

  useEffect(() => {
    filterUsers();
  }, [searchTerm, users]);

  const handleError = (error) => {
    console.error('Error in UserList component:', error);
    setError(error?.message || 'An unexpected error occurred');
    setLoading(false);
  };

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      // Try to fetch users with roles first
      let response;
      try {
        response = await userService.getAllUsersWithRoles();
        console.log('Users with roles response:', response);
      } catch (error) {
        console.log('Failed to fetch users with roles, falling back to regular users fetch:', error);
        response = await userService.getAllUsers();
        console.log('Users response:', response);
      }
      
      // Ensure we have an array of users
      let userData = [];
      if (Array.isArray(response)) {
        userData = response;
      } else if (response && typeof response === 'object' && response.data) {
        if (Array.isArray(response.data)) {
          userData = response.data;
        } else if (response.data.data && Array.isArray(response.data.data)) {
          userData = response.data.data;
        }
      }
      
      // Debugging: log the structure of the first few users if available
      if (userData.length > 0) {
        console.log('First user structure:', userData[0]);
        if (userData[0].userRoles) {
          console.log('First user roles structure:', userData[0].userRoles);
          // Log the type and structure of each role
          userData[0].userRoles.forEach((role, index) => {
            console.log(`Role ${index} type:`, typeof role);
            console.log(`Role ${index} structure:`, role);
          });
        } else if (userData[0].roles) {
          console.log('First user roles structure (roles property):', userData[0].roles);
          // Log the type and structure of each role
          userData[0].roles.forEach((role, index) => {
            console.log(`Role ${index} type:`, typeof role);
            console.log(`Role ${index} structure:`, role);
          });
        } else {
          console.log('First user has no userRoles or roles property');
        }
      }
      
      console.log('Processed user data:', userData);
      setUsers(userData);
      setFilteredUsers(userData);
    } catch (error) {
      handleError(error);
      showSnackbar('Erreur lors du chargement des utilisateurs', 'error');
      setUsers([]);
      setFilteredUsers([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchAllRoles = async () => {
    try {
      console.log('Fetching all roles...');
      const response = await roleService.getAllRoles();
      console.log('Roles fetched successfully:', response);
      
      // Ensure we have an array of roles
      let roleData = [];
      if (Array.isArray(response)) {
        roleData = response;
      } else if (response && typeof response === 'object' && response.data) {
        if (Array.isArray(response.data)) {
          roleData = response.data;
        } else if (response.data.data && Array.isArray(response.data.data)) {
          roleData = response.data.data;
        }
      }
      
      console.log('Processed role data:', roleData);
      setAllRoles(roleData);
    } catch (error) {
      console.error('Error fetching roles:', error);
      console.error('Error response:', error.response);
      
      if (error.response && error.response.status === 403) {
        const fixesApplied = handle403Error();
        if (!fixesApplied) {
          // Show error message instead of redirecting
          showSnackbar('Accès refusé. Veuillez vérifier vos permissions avec un administrateur.', 'error');
          // Removed automatic redirect to login page
          // setTimeout(() => {
          //   showSnackbar('Accès refusé. Reconnexion en cours...', 'warning');
          //   localStorage.removeItem('token');
          //   localStorage.removeItem('userInfo');
          //   window.location.href = '/';
          // }, 2000);
        }
      } else {
        showSnackbar('Erreur lors du chargement des rôles: ' + (error.response?.data?.message || error.message), 'error');
      }
    }
  };

  const filterUsers = () => {
    if (!searchTerm) {
      setFilteredUsers(users);
      return;
    }

    const term = searchTerm.toLowerCase();
    const filtered = users.filter(user =>
      (user && (
        user.username?.toLowerCase().includes(term) ||
        user.email?.toLowerCase().includes(term) ||
        user.nomComplet?.toLowerCase().includes(term) ||
        user.statut?.toLowerCase().includes(term) ||
        (user.userRoles && Array.isArray(user.userRoles) && user.userRoles.some(ur => {
          if (ur && ur.role && ur.role.nomRole) {
            return ur.role.nomRole.toLowerCase().includes(term);
          }
          if (ur && ur.nomRole) {
            return ur.nomRole.toLowerCase().includes(term);
          }
          return false;
        }))
      ))
    );

    setFilteredUsers(filtered);
  };

  const handleSearch = (event) => {
    setSearchTerm(event.target.value);
  };

  const handleDeleteUser = async (userId) => {
    try {
      const response = await userService.deleteUser(userId);
      let deleteData = null;
      if (response && typeof response === 'object') {
        if (response.data) {
          deleteData = response.data;
        } else {
          deleteData = response;
        }
      }
      
      showSnackbar('Utilisateur supprimé avec succès', 'success');
      fetchUsers();
    } catch (error) {
      handleError(error);
      showSnackbar('Erreur lors de la suppression de l\'utilisateur', 'error');
    }
  };

  const handleOpenDeleteDialog = (user) => {
    setSelectedUser(user);
    setOpenDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDialog(false);
    setSelectedUser(null);
  };

  const confirmDeleteUser = () => {
    if (selectedUser) {
      handleDeleteUser(selectedUser.id);
    }
    handleCloseDeleteDialog();
  };

  const showSnackbar = (message, severity) => {
    setSnackbar({ open: true, message, severity });
  };

  const handleSnackbarClose = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const getStatusChip = (status, userId) => {
    const isActive = status === 'ACTIF';
    return (
      <Chip
        icon={isActive ? <CheckIcon /> : <CloseIcon />}
        label={isActive ? 'Actif' : 'Inactif'}
        color={isActive ? 'success' : 'error'}
        size="small"
        variant="outlined"
        onClick={() => handleToggleUserStatus({ id: userId, statut: status })}
        sx={{ cursor: 'pointer' }}
      />
    );
  };

  const getUserRoles = (userRoles) => {
    // Debugging: log the userRoles structure
    console.log('getUserRoles called with:', userRoles);
    
    // Handle case where userRoles is undefined but the user object might have roles in a different property
    if (!userRoles || !Array.isArray(userRoles) || userRoles.length === 0) {
      return 'Aucun rôle';
    }
    
    const roleNames = userRoles.map(ur => {
      // Debugging: log each user role structure
      console.log('Processing user role:', ur);
      
      // Handle different possible structures
      if (ur && ur.role && ur.role.nomRole) {
        return ur.role.nomRole;
      }
      else if (ur && ur.nomRole) {
        return ur.nomRole;
      }
      else if (ur && ur.roleId) {
        return `Rôle ID: ${ur.roleId}`;
      }
      else if (ur && ur.role && ur.role.id) {
        return `Rôle ID: ${ur.role.id}`;
      }
      else if (ur && ur.id && ur.nomRole) {
        return ur.nomRole;
      }
      else if (ur && ur.role_name) {
        return ur.role_name;
      }
      else if (ur && ur.role_id) {
        return `Rôle ID: ${ur.role_id}`;
      }
      else if (ur && ur.name) {
        return ur.name;
      }
      else if (ur && ur.roleName) {
        return ur.roleName;
      }
      else if (ur && ur.title) {
        return ur.title;
      }
      // Additional cases for common data structures
      else if (ur && ur.role && ur.role.name) {
        return ur.role.name;
      }
      else if (ur && ur.role && ur.role.title) {
        return ur.role.title;
      }
      else if (ur && ur.role && ur.role.description) {
        return ur.role.description;
      }
      // Handle case where ur itself might be a role object
      else if (ur && ur.id && ur.name) {
        return ur.name;
      }
      else if (ur && ur.id && ur.nomRole) {
        return ur.nomRole;
      }
      // Handle string roles directly
      else if (typeof ur === 'string') {
        return ur;
      }
      // Handle numeric role IDs
      else if (typeof ur === 'number') {
        return `Rôle ID: ${ur}`;
      }
      return 'Rôle inconnu';
    });
    
    // Debugging: log the extracted role names
    console.log('Extracted role names:', roleNames);
    
    const validRoles = roleNames.filter(name => name !== 'Rôle inconnu');
    if (validRoles.length === 0) {
      return 'Aucun rôle';
    }
    
    return validRoles.join(', ');
  };

  const handleToggleUserStatus = async (user) => {
    try {
      let response;
      if (user.statut === 'ACTIF') {
        response = await userService.deactivateUser(user.id);
      } else {
        response = await userService.activateUser(user.id);
      }
      
      let userData = null;
      if (response && typeof response === 'object') {
        if (response.data) {
          userData = response.data;
        } else {
          userData = response;
        }
      }
      
      showSnackbar(`Utilisateur ${user.statut === 'ACTIF' ? 'désactivé' : 'activé'} avec succès`, 'success');
      fetchUsers();
    } catch (error) {
      handleError(error);
      showSnackbar(
        `Erreur lors de la modification du statut: ${error.response?.data?.message || error.message}`,
        'error'
      );
    }
  };

  const handleOpenUserFormDialog = (user = null) => {
    if (user) {
      setUserFormData({
        username: user.username || '',
        email: user.email || '',
        password: '',
        nomComplet: user.nomComplet || '',
        statut: user.statut || 'ACTIF'
      });
      setSelectedUser(user);
    } else {
      setUserFormData({
        username: '',
        email: '',
        password: '',
        nomComplet: '',
        statut: 'ACTIF'
      });
      setSelectedUser(null);
    }
    setOpenUserFormDialog(true);
  };

  const handleCloseUserFormDialog = () => {
    setOpenUserFormDialog(false);
    setUserFormData({
      username: '',
      email: '',
      password: '',
      nomComplet: '',
      statut: 'ACTIF'
    });
    setSelectedUser(null);
  };

  const handleUserFormChange = (e) => {
    const { name, value } = e.target;
    setUserFormData({
      ...userFormData,
      [name]: value
    });
  };

  const handleUserFormSubmit = async () => {
    try {
      let response;
      if (selectedUser) {
        const userData = {...userFormData};
        if (!userData.password) delete userData.password;
        
        response = await userService.updateUser(selectedUser.id, userData);
      } else {
        response = await userService.createUser(userFormData);
      }
      
      let userData = null;
      if (response && typeof response === 'object') {
        if (response.data) {
          userData = response.data;
        } else {
          userData = response;
        }
      }
      
      showSnackbar(`Utilisateur ${selectedUser ? 'mis à jour' : 'créé'} avec succès`, 'success');
      handleCloseUserFormDialog();
      fetchUsers();
    } catch (error) {
      handleError(error);
      showSnackbar(
        `Erreur lors de ${selectedUser ? 'la mise à jour' : 'la création'} de l'utilisateur: ${error.response?.data?.message || error.message}`,
        'error'
      );
    }
  };

  const handleShowUserRoles = (user) => {
    setSelectedUser(user);
    setOpenRolesDialog(true);
  };
  
  const handleCloseRolesDialog = () => {
    setOpenRolesDialog(false);
  };

  const handleOpenAssignRolesDialog = (user) => {
    setSelectedUser(user);
    const currentUserRoleIds = (user.userRoles || user.roles)?.map(ur => getRoleProperty(ur, 'id')).filter(id => id) || [];
    setSelectedRoleIds(currentUserRoleIds);
    setOpenAssignRolesDialog(true);
  };

  const handleCloseAssignRolesDialog = () => {
    setOpenAssignRolesDialog(false);
    setSelectedUser(null);
    setSelectedRoleIds([]);
  };

  const handleRoleSelectionChange = (event) => {
    const value = event.target.value;
    setSelectedRoleIds(value);
  };

  const handleAssignRoles = async () => {
    try {
      const assignmentDTO = {
        userId: selectedUser.id,
        roleIds: selectedRoleIds
      };
      
      const response = await userService.assignRolesToUser(assignmentDTO);
      let assignData = null;
      if (response && typeof response === 'object') {
        if (response.data) {
          assignData = response.data;
        } else {
          assignData = response;
        }
      }
      
      showSnackbar('Rôles assignés avec succès', 'success');
      handleCloseAssignRolesDialog();
      fetchUsers();
    } catch (error) {
      handleError(error);
      showSnackbar('Erreur lors de l\'assignation des rôles', 'error');
    }
  };

  const hasUserRoles = (userRoles) => {
    if (!userRoles || !Array.isArray(userRoles) || userRoles.length === 0) {
      return false;
    }
    
    return userRoles.some(ur => {
      if (ur && ur.role && (ur.role.nomRole || ur.role.id || ur.role.description)) {
        return true;
      }
      if (ur && (ur.nomRole || ur.id || ur.description)) {
        return true;
      }
      if (ur && (ur.roleId || ur.role_id)) {
        return true;
      }
      if (ur && ur.role_name) {
        return true;
      }
      return false;
    });
  };

  const getRoleProperty = (userRole, property) => {
    if (!userRole) return null;
    
    // Handle case where userRole is directly a role object
    if (typeof userRole === 'object' && !userRole.role) {
      if (userRole[property]) {
        return userRole[property];
      }
      // Check for alternative property names directly on the object
      const snakeCaseProperty = property.replace(/([A-Z])/g, '_$1').toLowerCase();
      if (userRole[snakeCaseProperty]) {
        return userRole[snakeCaseProperty];
      }
      
      // Check property map for alternatives
      const propertyMap = {
        'nomRole': ['name', 'roleName', 'title'],
        'name': ['nomRole', 'roleName', 'title'],
        'roleName': ['nomRole', 'name', 'title'],
        'title': ['nomRole', 'name', 'roleName'],
        'description': ['desc', 'role_description']
      };
      
      if (propertyMap[property]) {
        for (const altProperty of propertyMap[property]) {
          if (userRole[altProperty]) {
            return userRole[altProperty];
          }
          const snakeCaseAlt = altProperty.replace(/([A-Z])/g, '_$1').toLowerCase();
          if (userRole[snakeCaseAlt]) {
            return userRole[snakeCaseAlt];
          }
        }
      }
      
      return null;
    }
    
    if (userRole[property]) {
      return userRole[property];
    }
    
    if (userRole.role && userRole.role[property]) {
      return userRole.role[property];
    }
    
    const snakeCaseProperty = property.replace(/([A-Z])/g, '_$1').toLowerCase();
    if (userRole[snakeCaseProperty]) {
      return userRole[snakeCaseProperty];
    }
    
    if (property === 'id') {
      if (userRole.roleId) return userRole.roleId;
      if (userRole.role_id) return userRole.role_id;
      if (userRole.role && userRole.role.id) return userRole.role.id;
    }
    
    if (property === 'nomRole' && userRole.role_name) {
      return userRole.role_name;
    }
    
    if (property === 'description' && userRole.role_description) {
      return userRole.role_description;
    }
    
    const propertyMap = {
      'nomRole': ['name', 'roleName', 'title'],
      'name': ['nomRole', 'roleName', 'title'],
      'roleName': ['nomRole', 'name', 'title'],
      'title': ['nomRole', 'name', 'roleName'],
      'description': ['desc', 'role_description']
    };
    
    if (propertyMap[property]) {
      for (const altProperty of propertyMap[property]) {
        if (userRole[altProperty]) {
          return userRole[altProperty];
        }
        if (userRole.role && userRole.role[altProperty]) {
          return userRole.role[altProperty];
        }
        const snakeCaseAlt = altProperty.replace(/([A-Z])/g, '_$1').toLowerCase();
        if (userRole[snakeCaseAlt]) {
          return userRole[snakeCaseAlt];
        }
      }
    }
    
    return null;
  };

  if (error) {
    return (
      <Container maxWidth="xl">
        <Box 
          display="flex" 
          flexDirection="column" 
          justifyContent="center" 
          alignItems="center" 
          height="300px"
          sx={{ textAlign: 'center' }}
        >
          <Typography variant="h6" color="error" gutterBottom>
            Une erreur est survenue
          </Typography>
          <Typography variant="body1" color="textSecondary">
            {error}
          </Typography>
          <Button 
            variant="contained" 
            color="primary" 
            sx={{ mt: 2 }}
            onClick={() => {
              setError(null);
              fetchUsers();
            }}
          >
            Réessayer
          </Button>
        </Box>
      </Container>
    );
  }

  if (loading) {
    return (
      <Container maxWidth="xl">
        <Box display="flex" justifyContent="center" alignItems="center" height="200px">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#333', fontWeight: 'bold' }}>
            Gestion des Utilisateurs
          </Typography>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenUserFormDialog()}
            sx={{ 
              bgcolor: '#FF6B35',
              '&:hover': {
                bgcolor: '#e55a2b'
              },
              borderRadius: 2,
              boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)'
            }}
          >
            Nouvel Utilisateur
          </Button>
        </Grid>
      </Grid>

      {error && (
        <Box sx={{ mb: 3 }}>
          <Paper sx={{ p: 3, bgcolor: '#ffebee', borderRadius: 2, border: '1px solid #e0e0e0' }}>
            <Typography color="error" variant="h6">Erreur: API request failed</Typography>
            <Typography color="error">{error}</Typography>
            <Button 
              variant="outlined" 
              color="primary" 
              sx={{ mt: 2, borderRadius: 2 }}
              onClick={() => {
                setError(null);
                fetchUsers();
              }}
            >
              Réessayer
            </Button>
          </Paper>
        </Box>
      )}

      <Paper sx={{ p: 2, mb: 3, borderRadius: 2, border: '1px solid #e0e0e0' }}>
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Rechercher des utilisateurs..."
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
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Utilisateur</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Email</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Rôles</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date de création</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredUsers && filteredUsers.length > 0 ? (
              filteredUsers.map((user) => (
                <TableRow 
                  key={user.id}
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
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <PersonIcon sx={{ mr: 1, color: '#FF6B35' }} />
                      <Box>
                        <Typography variant="subtitle2">{user.username || 'N/A'}</Typography>
                        <Typography variant="body2" color="textSecondary">
                          {user.nomComplet || 'Non spécifié'}
                        </Typography>
                      </Box>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <EmailIcon sx={{ mr: 1, color: '#FF6B35' }} />
                      {user.email || 'N/A'}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Tooltip title={`Rôles: ${getUserRoles(user.userRoles || user.roles)}`} arrow placement="top">
                      <Chip
                        label={`${(user.userRoles?.length || user.roles?.length) || 0} rôle(s)`}
                        size="small"
                        onClick={() => handleShowUserRoles(user)}
                        sx={{ 
                          bgcolor: '#e3f2fd',
                          color: '#1976d2',
                          fontWeight: 'bold',
                          borderRadius: 1,
                          cursor: 'pointer'
                        }}
                      />
                    </Tooltip>
                  </TableCell>
                  <TableCell>
                    {user.statut ? getStatusChip(user.statut, user.id) : (
                      <Chip 
                        label="N/A" 
                        size="small" 
                        sx={{ 
                          bgcolor: '#f5f5f5',
                          borderRadius: 1
                        }} 
                      />
                    )}
                  </TableCell>
                  <TableCell>
                    {user.createdAt ? new Date(user.createdAt).toLocaleDateString('fr-FR') : 'N/A'}
                  </TableCell>
                  <TableCell>
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenUserFormDialog(user)}
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
                      onClick={() => handleOpenAssignRolesDialog(user)}
                      sx={{ 
                        mr: 1,
                        '&:hover': {
                          bgcolor: 'rgba(33, 150, 243, 0.1)'
                        }
                      }}
                    >
                      <SecurityIcon />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenDeleteDialog(user)}
                      disabled={user.username === currentUser?.username}
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
            ) : (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography variant="body1" color="textSecondary">
                    Aucun utilisateur trouvé
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={openDialog}
        onClose={handleCloseDeleteDialog}
        aria-labelledby="delete-user-dialog-title"
        aria-describedby="delete-user-dialog-description"
      >
        <DialogTitle 
          id="delete-user-dialog-title"
          sx={{ bgcolor: '#f44336', color: 'white', fontWeight: 'bold' }}
        >
          Confirmer la suppression
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText id="delete-user-dialog-description">
            Êtes-vous sûr de vouloir supprimer l'utilisateur <strong>{selectedUser?.username}</strong> ?
            Cette action est irréversible.
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseDeleteDialog} sx={{ borderRadius: 2 }}>
            Annuler
          </Button>
          <Button 
            onClick={confirmDeleteUser} 
            variant="contained" 
            color="error"
            sx={{ borderRadius: 2 }}
          >
            Supprimer
          </Button>
        </DialogActions>
      </Dialog>

      {/* User Form Dialog (Create/Edit) */}
      <Dialog
        open={openUserFormDialog}
        onClose={handleCloseUserFormDialog}
        aria-labelledby="user-form-dialog-title"
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle 
          id="user-form-dialog-title"
          sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}
        >
          {selectedUser ? 'Modifier l\'utilisateur' : 'Nouvel utilisateur'}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            {selectedUser 
              ? 'Modifiez les informations de l\'utilisateur.' 
              : 'Remplissez les informations pour créer un nouvel utilisateur.'}
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                required
                label="Nom d'utilisateur"
                name="username"
                value={userFormData?.username || ''}
                onChange={handleUserFormChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                required
                label="Email"
                name="email"
                type="email"
                value={userFormData?.email || ''}
                onChange={handleUserFormChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                required={!selectedUser}
                label={selectedUser ? "Mot de passe (laisser vide pour ne pas modifier)" : "Mot de passe"}
                name="password"
                type="password"
                value={userFormData?.password || ''}
                onChange={handleUserFormChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Nom complet"
                name="nomComplet"
                value={userFormData?.nomComplet || ''}
                onChange={handleUserFormChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Statut</InputLabel>
                <Select
                  name="statut"
                  value={userFormData?.statut || 'ACTIF'}
                  onChange={handleUserFormChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="ACTIF">Actif</MenuItem>
                  <MenuItem value="INACTIF">Inactif</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseUserFormDialog} sx={{ borderRadius: 2 }}>
            Annuler
          </Button>
          <Button 
            onClick={handleUserFormSubmit} 
            variant="contained"
            sx={{ 
              bgcolor: '#FF6B35',
              '&:hover': {
                bgcolor: '#e55a2b'
              },
              borderRadius: 2
            }}
          >
            {selectedUser ? 'Mettre à jour' : 'Créer'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* User Role Information Dialog */}
      <Dialog
        open={openRolesDialog}
        onClose={handleCloseRolesDialog}
        aria-labelledby="roles-dialog-title"
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle 
          id="roles-dialog-title"
          sx={{ bgcolor: '#2196f3', color: 'white', fontWeight: 'bold' }}
        >
          Rôles de l'utilisateur: {selectedUser?.username || ''}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          {selectedUser && (selectedUser.userRoles || selectedUser.roles) && Array.isArray(selectedUser.userRoles || selectedUser.roles) && (selectedUser.userRoles || selectedUser.roles).length > 0 ? (
            <TableContainer component={Paper} elevation={0}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>ID du rôle</TableCell>
                    <TableCell>Nom du rôle</TableCell>
                    <TableCell>Description</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(selectedUser.userRoles || selectedUser.roles).map((userRole, index) => (
                    <TableRow key={index}>
                      <TableCell>{getRoleProperty(userRole, 'id') || 'N/A'}</TableCell>
                      <TableCell>{getRoleProperty(userRole, 'nomRole') || 'Non spécifié'}</TableCell>
                      <TableCell>{getRoleProperty(userRole, 'description') || 'Aucune description'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Typography variant="body1" color="textSecondary" align="center" sx={{ py: 3 }}>
              Cet utilisateur n'a aucun rôle assigné.
            </Typography>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseRolesDialog} sx={{ borderRadius: 2 }}>
            Fermer
          </Button>
        </DialogActions>
      </Dialog>

      {/* Assign Roles Dialog */}
      <Dialog
        open={openAssignRolesDialog}
        onClose={handleCloseAssignRolesDialog}
        aria-labelledby="assign-roles-dialog-title"
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle 
          id="assign-roles-dialog-title"
          sx={{ bgcolor: '#2196f3', color: 'white', fontWeight: 'bold' }}
        >
          Assigner des rôles à: {selectedUser?.username || ''}
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            Assigner des rôles à l'utilisateur "{selectedUser?.username}"
          </DialogContentText>
          <FormControl fullWidth sx={{ borderRadius: 2 }}>
            <InputLabel id="roles-select-label">Sélectionner les rôles</InputLabel>
            <Select
              labelId="roles-select-label"
              multiple
              value={selectedRoleIds}
              onChange={handleRoleSelectionChange}
              input={<OutlinedInput label="Sélectionner les rôles" />}
              renderValue={(selected) => {
                const selectedRoles = allRoles.filter(role => selected.includes(role.id));
                return selectedRoles.map(role => role.nomRole).join(', ');
              }}
              sx={{ borderRadius: 2 }}
            >
              {allRoles.map((role) => (
                <MenuItem key={role.id} value={role.id}>
                  <Checkbox checked={selectedRoleIds.indexOf(role.id) > -1} />
                  <ListItemText primary={role.nomRole} secondary={role.description || ''} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseAssignRolesDialog} sx={{ borderRadius: 2 }}>
            Annuler
          </Button>
          <Button 
            onClick={handleAssignRoles} 
            variant="contained"
            sx={{ 
              bgcolor: '#2196f3',
              '&:hover': {
                bgcolor: '#1976d2'
              },
              borderRadius: 2
            }}
          >
            Assigner
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert 
          onClose={handleSnackbarClose} 
          severity={snackbar.severity} 
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default UserList;