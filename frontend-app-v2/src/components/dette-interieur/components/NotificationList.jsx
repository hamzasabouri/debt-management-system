import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Grid,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Archive as ArchiveIcon,
  Close as CloseIcon,
  Visibility as VisibilityIcon,
  MarkEmailRead as MarkEmailReadIcon,
} from '@mui/icons-material';
import { getNotifications, createNotification } from '../../../services/dette-interieur/notificationsService';
import { setAuthToken } from '../../../services/dette-interieur/notificationsService';

const NotificationList = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogMode, setDialogMode] = useState('create'); // 'create' or 'edit'
  const [currentNotification, setCurrentNotification] = useState({
    titre: '',
    message: '',
    dateEnvoi: '',
    statut: 'ENVOYEE',
    priorite: 'MOYENNE',
    destinataire: '',
    type: 'SYSTEME'
  });
  const [searchTerm, setSearchTerm] = useState('');

  const [filterType, setFilterType] = useState('ALL');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [selectedNotification, setSelectedNotification] = useState(null);
  const [bulkActionDialogOpen, setBulkActionDialogOpen] = useState(false);
  const [bulkActionType, setBulkActionType] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
      fetchNotifications();
    }
  }, []);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const data = await getNotifications();
      // Ensure data is an array
      const notificationsArray = Array.isArray(data) ? data : [];
      setNotifications(notificationsArray);
      setError(null);
    } catch (err) {
      setError('Failed to fetch notifications: ' + err.message);
      console.error('Error fetching notifications:', err);
      // Set empty array on error to prevent filter issues
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateDialog = () => {
    setDialogMode('create');
    setCurrentNotification({
      titre: '',
      message: '',
      dateEnvoi: '',
      statut: 'ENVOYEE',
      priorite: 'MOYENNE',
      destinataire: '',
      type: 'SYSTEME'
    });
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentNotification(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async () => {
    try {
      if (dialogMode === 'create') {
        await createNotification(currentNotification);
      }
      handleCloseDialog();
      fetchNotifications();
    } catch (err) {
      setError('Failed to save notification: ' + err.message);
      console.error('Error saving notification:', err);
    }
  };

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'ENVOYEE':
        return 'primary';
      case 'LU':
        return 'success';
      case 'NON_LU':
        return 'warning';
      case 'EXPIREE':
        return 'default';
      default:
        return 'default';
    }
  };

  const getPriorityColor = (priorite) => {
    switch (priorite) {
      case 'FAIBLE':
        return 'default';
      case 'MOYENNE':
        return 'primary';
      case 'ELEVEE':
        return 'warning';
      case 'URGENTE':
        return 'error';
      default:
        return 'default';
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'SYSTEME':
        return 'primary';
      case 'ALERTE':
        return 'warning';
      case 'INFORMATION':
        return 'success';
      case 'ERREUR':
        return 'error';
      default:
        return 'default';
    }
  };

  // Additional safety check to ensure we have an array
  const safeNotifications = Array.isArray(notifications) ? notifications : [];
  
  // Ensure notifications is always an array before filtering
  const filteredNotifications = safeNotifications.filter(notification =>
    notification && 
    (
      (notification.message && notification.message.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (notification.type && notification.type.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (notification.statut && notification.statut.toLowerCase().includes(searchTerm.toLowerCase()))
    )
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Additional safety check
  if (!Array.isArray(notifications)) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px' }}>
        <Typography color="error">Data format error. Please refresh the page.</Typography>
      </Box>
    );
  }

  const handleSearch = () => {
    // Implement search logic if needed
  };

  const handleResetFilters = () => {
    setSearchTerm('');
    setFilterType('ALL');
    setFilterStatus('ALL');
  };

  const handleArchiveAll = () => {
    // Implement archive all logic
  };

  const handleViewDetails = (notification) => {
    setSelectedNotification(notification);
    setDetailsDialogOpen(true);
  };

  const handleMarkAsRead = (id) => {
    // Implement mark as read logic
  };

  const handleArchive = (id) => {
    // Implement archive logic
  };

  const handleCloseDetailsDialog = () => {
    setDetailsDialogOpen(false);
    setSelectedNotification(null);
  };

  const handleBulkActionConfirm = () => {
    // Implement bulk action confirm logic
    setBulkActionDialogOpen(false);
  };

  const getNotificationTypeLabel = (type) => {
    switch (type) {
      case 'SYSTEME': return 'Système';
      case 'ALERTE': return 'Alerte';
      case 'INFORMATION': return 'Information';
      case 'ERREUR': return 'Erreur';
      default: return type;
    }
  };

  const getNotificationStatusLabel = (status) => {
    switch (status) {
      case 'ENVOYEE': return 'Envoyée';
      case 'LU': return 'Lu';
      case 'NON_LU': return 'Non Lu';
      case 'EXPIREE': return 'Expirée';
      default: return status;
    }
  };

  const getNotificationTypeColor = (type) => {
    switch (type) {
      case 'SYSTEME': return '#2196f3';
      case 'ALERTE': return '#ff9800';
      case 'INFORMATION': return '#4caf50';
      case 'ERREUR': return '#f44336';
      default: return '#9e9e9e';
    }
  };

  const getNotificationStatusColor = (status) => {
    switch (status) {
      case 'ENVOYEE': return '#2196f3';
      case 'LU': return '#4caf50';
      case 'NON_LU': return '#ff9800';
      case 'EXPIREE': return '#9e9e9e';
      default: return '#9e9e9e';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#333', fontWeight: 'bold' }}>
            Gestion des Notifications
          </Typography>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreateDialog}
            sx={{ 
              bgcolor: '#FF6B35',
              '&:hover': {
                bgcolor: '#e55a2b'
              },
              borderRadius: 2,
              boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)'
            }}
          >
            Nouvelle Notification
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
              onClick={() => fetchNotifications()}
            >
              Réessayer
            </Button>
          </Paper>
        </Box>
      )}

      <Paper sx={{ p: 2, mb: 3, borderRadius: 2, border: '1px solid #e0e0e0' }}>
        <Grid container spacing={3} alignItems="center">
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              variant="outlined"
              placeholder="Rechercher par message, type, statut..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
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
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <FormControl fullWidth variant="outlined" sx={{ borderRadius: 2 }}>
              <InputLabel>Type de Notification</InputLabel>
              <Select
                value={filterType}
                onChange={(e) => setFilterType(e.target.value)}
                label="Type de Notification"
                sx={{ borderRadius: 2 }}
              >
                <MenuItem value="ALL">Tous les types</MenuItem>
                <MenuItem value="INFO">Information</MenuItem>
                <MenuItem value="WARNING">Avertissement</MenuItem>
                <MenuItem value="ERROR">Erreur</MenuItem>
                <MenuItem value="SUCCESS">Succès</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <FormControl fullWidth variant="outlined" sx={{ borderRadius: 2 }}>
              <InputLabel>Statut</InputLabel>
              <Select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                label="Statut"
                sx={{ borderRadius: 2 }}
              >
                <MenuItem value="ALL">Tous les statuts</MenuItem>
                <MenuItem value="UNREAD">Non lu</MenuItem>
                <MenuItem value="READ">Lu</MenuItem>
                <MenuItem value="ARCHIVED">Archivé</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Button
              fullWidth
              variant="contained"
              color="primary"
              startIcon={<SearchIcon />}
              onClick={handleSearch}
              sx={{ height: '100%', borderRadius: 2, boxShadow: '0 2px 10px rgba(0, 0, 0, 0.2)' }}
            >
              Rechercher
            </Button>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Button
              fullWidth
              variant="outlined"
              color="primary"
              startIcon={<RefreshIcon />}
              onClick={handleResetFilters}
              sx={{ height: '100%', borderRadius: 2 }}
            >
              Réinitialiser
            </Button>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Button
              fullWidth
              variant="outlined"
              color="secondary"
              startIcon={<ArchiveIcon />}
              onClick={handleArchiveAll}
              disabled={notifications.length === 0}
              sx={{ height: '100%', borderRadius: 2 }}
            >
              Archiver tout
            </Button>
          </Grid>
        </Grid>
      </Paper>

      <TableContainer 
        component={Paper} 
        sx={{ 
          borderRadius: 2, 
          border: '1px solid #e0e0e0',
          boxShadow: '0 2px 10px rgba(0,0,0,0.05)'
        }}
      >
        <Table size="small">
          <TableHead>
            <TableRow sx={{ bgcolor: 'rgba(255, 107, 53, 0.1)' }}>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Date</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Message</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Type</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Statut</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Service</TableCell>
              <TableCell sx={{ fontWeight: 'bold', color: '#333' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredNotifications.map((notification) => (
              <TableRow 
                key={notification.id} 
                sx={{ 
                  '&:nth-of-type(odd)': {
                    bgcolor: 'rgba(0, 0, 0, 0.02)'
                  },
                  '&:hover': {
                    bgcolor: 'rgba(255, 107, 53, 0.05)',
                    transform: 'scale(1.01)',
                    transition: 'all 0.2s ease'
                  },
                  backgroundColor: notification.status === 'UNREAD' ? '#e3f2fd' : 'inherit'
                }}
              >
                <TableCell>
                  {new Date(notification.createdAt).toLocaleDateString('fr-FR', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </TableCell>
                <TableCell>
                  <Typography variant="body2" noWrap>
                    {notification.message}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Chip 
                    label={getNotificationTypeLabel(notification.type)}
                    size="small"
                    sx={{
                      bgcolor: getNotificationTypeColor(notification.type),
                      color: 'white',
                      fontWeight: 'bold',
                      borderRadius: 1
                    }}
                  />
                </TableCell>
                <TableCell>
                  <Chip 
                    label={getNotificationStatusLabel(notification.status)}
                    size="small"
                    sx={{
                      bgcolor: getNotificationStatusColor(notification.status),
                      color: 'white',
                      fontWeight: 'bold',
                      borderRadius: 1
                    }}
                  />
                </TableCell>
                <TableCell>
                  <Typography variant="body2">
                    {notification.sourceService}
                  </Typography>
                </TableCell>
                <TableCell>
                  <IconButton 
                    size="small" 
                    onClick={() => handleViewDetails(notification)}
                    sx={{ 
                      mr: 1,
                      '&:hover': {
                        bgcolor: 'rgba(255, 107, 53, 0.1)'
                      }
                    }}
                  >
                    <VisibilityIcon />
                  </IconButton>
                  <IconButton 
                    size="small" 
                    onClick={() => handleMarkAsRead(notification.id)}
                    disabled={notification.status === 'READ'}
                    sx={{
                      mr: 1,
                      '&:hover': {
                        bgcolor: 'rgba(244, 67, 54, 0.1)'
                      }
                    }}
                  >
                    <MarkEmailReadIcon />
                  </IconButton>
                  <IconButton 
                    size="small" 
                    onClick={() => handleArchive(notification.id)}
                    sx={{
                      '&:hover': {
                        bgcolor: 'rgba(244, 67, 54, 0.1)'
                      }
                    }}
                  >
                    <ArchiveIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
          Nouvelle Notification
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText sx={{ mb: 2 }}>
            Remplissez les informations pour créer une nouvelle notification.
          </DialogContentText>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Titre"
                name="titre"
                value={currentNotification.titre}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Message"
                name="message"
                value={currentNotification.message}
                onChange={handleInputChange}
                multiline
                rows={3}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Date d'Envoi"
                name="dateEnvoi"
                type="datetime-local"
                value={currentNotification.dateEnvoi}
                onChange={handleInputChange}
                InputLabelProps={{ shrink: true }}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Destinataire"
                name="destinataire"
                value={currentNotification.destinataire}
                onChange={handleInputChange}
                sx={{ borderRadius: 2 }}
                InputProps={{ sx: { borderRadius: 2 } }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Statut</InputLabel>
                <Select
                  name="statut"
                  value={currentNotification.statut}
                  onChange={handleInputChange}
                  label="Statut"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="ENVOYEE">Envoyée</MenuItem>
                  <MenuItem value="LU">Lu</MenuItem>
                  <MenuItem value="NON_LU">Non Lu</MenuItem>
                  <MenuItem value="EXPIREE">Expirée</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Priorité</InputLabel>
                <Select
                  name="priorite"
                  value={currentNotification.priorite}
                  onChange={handleInputChange}
                  label="Priorité"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="FAIBLE">Faible</MenuItem>
                  <MenuItem value="MOYENNE">Moyenne</MenuItem>
                  <MenuItem value="ELEVEE">Élevée</MenuItem>
                  <MenuItem value="URGENTE">Urgente</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <FormControl fullWidth sx={{ borderRadius: 2 }}>
                <InputLabel>Type</InputLabel>
                <Select
                  name="type"
                  value={currentNotification.type}
                  onChange={handleInputChange}
                  label="Type"
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="SYSTEME">Système</MenuItem>
                  <MenuItem value="ALERTE">Alerte</MenuItem>
                  <MenuItem value="INFORMATION">Information</MenuItem>
                  <MenuItem value="ERREUR">Erreur</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseDialog} sx={{ borderRadius: 2 }}>
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
            Créer
          </Button>
        </DialogActions>
      </Dialog>

      {/* Notification Details Dialog */}
      <Dialog open={detailsDialogOpen} onClose={handleCloseDetailsDialog} maxWidth="md" fullWidth>
        <DialogTitle sx={{ bgcolor: '#FF6B35', color: 'white', fontWeight: 'bold' }}>
          Détails de la Notification
          <IconButton
            aria-label="close"
            onClick={handleCloseDetailsDialog}
            sx={{
              position: 'absolute',
              right: 8,
              top: 8,
              color: 'white',
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers sx={{ mt: 2 }}>
          {selectedNotification && (
            <Grid container spacing={3}>
              <Grid size={{ xs: 12 }}>
                <Typography variant="h6" sx={{ mb: 2, fontWeight: 'bold' }}>
                  {selectedNotification.message}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography variant="body2" color="textSecondary">
                  <strong>Type:</strong> {getNotificationTypeLabel(selectedNotification.type)}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography variant="body2" color="textSecondary">
                  <strong>Statut:</strong> {getNotificationStatusLabel(selectedNotification.status)}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography variant="body2" color="textSecondary">
                  <strong>Date de Création:</strong> {new Date(selectedNotification.createdAt).toLocaleString('fr-FR')}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography variant="body2" color="textSecondary">
                  <strong>Date de Mise à Jour:</strong> {new Date(selectedNotification.updatedAt).toLocaleString('fr-FR')}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography variant="body2" color="textSecondary">
                  <strong>Service Source:</strong> {selectedNotification.sourceService}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography variant="body2" color="textSecondary">
                  <strong>ID de Référence:</strong> {selectedNotification.referenceId || 'N/A'}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography variant="body2" color="textSecondary" sx={{ fontWeight: 'bold', mb: 1 }}>
                  Données Supplémentaires:
                </Typography>
                <pre style={{ 
                  backgroundColor: '#f5f5f5', 
                  padding: '10px', 
                  borderRadius: '4px', 
                  overflowX: 'auto',
                  maxHeight: '200px'
                }}>
                  {JSON.stringify(selectedNotification.additionalData, null, 2)}
                </pre>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseDetailsDialog} sx={{ borderRadius: 2 }}>
            Fermer
          </Button>
        </DialogActions>
      </Dialog>
      
      {/* Bulk Actions Confirmation Dialog */}
      <Dialog open={bulkActionDialogOpen} onClose={() => setBulkActionDialogOpen(false)}>
        <DialogTitle sx={{ bgcolor: '#f44336', color: 'white', fontWeight: 'bold' }}>
          Confirmer l'action groupée
        </DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <DialogContentText>
            Êtes-vous sûr de vouloir {bulkActionType === 'markAsRead' ? 'marquer comme lues' : 'archiver'} 
            toutes les notifications sélectionnées ?
          </DialogContentText>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setBulkActionDialogOpen(false)} sx={{ borderRadius: 2 }}>
            Annuler
          </Button>
          <Button 
            onClick={handleBulkActionConfirm} 
            variant="contained" 
            color="primary"
            sx={{ borderRadius: 2 }}
          >
            Confirmer
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default NotificationList;