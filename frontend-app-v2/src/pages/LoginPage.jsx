import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  Box, 
  TextField, 
  Button, 
  Typography, 
  Paper, 
  Container,
  InputAdornment,
  IconButton,
  CircularProgress,
  Alert,
  Divider
} from '@mui/material';
import { Visibility, VisibilityOff, Lock, Person } from '@mui/icons-material';

// Import logo images
import TGRLogo from '../assets/tgr.png';
import GDPLogo from '../assets/gdp.png';

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      await login(username, password);
    } catch (err) {
      setError(err.message || 'Échec de la connexion');
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #FF6B35 0%, #F7931E 100%)',
        p: 3
      }}
    >
      <Container maxWidth="sm">
        <Paper 
          elevation={8} 
          sx={{
            p: { xs: 3, md: 6 },
            borderRadius: 3,
            background: '#ffffff',
            boxShadow: '0 15px 35px rgba(0, 0, 0, 0.2)',
            position: 'relative',
            overflow: 'hidden'
          }}
        >
          <Box textAlign="center" mb={3}>
            <Box display="flex" justifyContent="center" alignItems="center" gap={4} mb={3}>
              {/* TGR Logo */}
              <Box textAlign="center">
                <Box
                  component="img"
                  src={TGRLogo}
                  alt="TGR Logo"
                  sx={{
                    width: 100,
                    height: 100,
                    objectFit: 'contain',
                    mb: 1
                  }}
                />
                
              </Box>
              
              {/* GDP Logo */}
              <Box textAlign="center">
                <Box
                  component="img"
                  src={GDPLogo}
                  alt="GDP Logo"
                  sx={{
                    width: 180,
                    height: 180,
                    objectFit: 'contain',
                    mb: 1
                  }}
                />
               
              </Box>
            </Box>
          {/* Decorative elements */}
          <Box 
            sx={{
              position: 'absolute',
              top: -50,
              right: -50,
              width: 200,
              height: 200,
              bgcolor: 'primary.light',
              borderRadius: '50%',
              opacity: 0.1
            }}
          />
          <Box 
            sx={{
              position: 'absolute',
              bottom: -80,
              left: -30,
              width: 250,
              height: 250,
              bgcolor: 'primary.main',
              borderRadius: '50%',
              opacity: 0.1
            }}
          />
          
          {/* Logo Section */}
          
            
            <Divider sx={{ my: 2 }}>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                ACCÈS SÉCURISÉ
              </Typography>
            </Divider>
          </Box>

          {/* Header */}
          <Box textAlign="center" mb={3}>
            <Typography 
              variant="h4" 
              component="h1" 
              sx={{ 
                color: 'primary.main',
                fontWeight: 'bold',
                mb: 1,
                textShadow: '1px 1px 2px rgba(0,0,0,0.1)'
              }}
            >
              Portail de Connexion
            </Typography>
            <Typography 
              variant="body2" 
              sx={{ 
                color: 'text.secondary',
                maxWidth: '80%',
                mx: 'auto'
              }}
            >
              Accédez à votre espace de gestion financière sécurisé
            </Typography>
          </Box>

          {error && (
            <Alert 
              severity="error" 
              sx={{ 
                mb: 3,
                borderRadius: 2,
                boxShadow: '0 2px 10px rgba(255, 107, 53, 0.2)'
              }}
            >
              {error}
            </Alert>
          )}
          
          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Nom d'utilisateur"
              variant="outlined"
              margin="normal"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Person sx={{ color: 'primary.main' }} />
                  </InputAdornment>
                ),
              }}
              sx={{ 
                mb: 2,
                '& .MuiOutlinedInput-root': {
                  '&.Mui-focused fieldset': {
                    borderColor: 'primary.main',
                    borderWidth: '2px',
                  },
                },
              }}
              required
            />
            
            <TextField
              fullWidth
              label="Mot de passe"
              type={showPassword ? 'text' : 'password'}
              variant="outlined"
              margin="normal"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Lock sx={{ color: 'primary.main' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              sx={{ 
                mb: 3,
                '& .MuiOutlinedInput-root': {
                  '&.Mui-focused fieldset': {
                    borderColor: 'primary.main',
                    borderWidth: '2px',
                  },
                },
              }}
              required
            />
            
            <Button
              fullWidth
              type="submit"
              variant="contained"
              size="large"
              disabled={loading}
              sx={{
                py: 1.5,
                fontSize: '1.1rem',
                fontWeight: 600,
                mt: 1,
                mb: 2,
                background: 'linear-gradient(90deg, #FF6B35 0%, #F7931E 100%)',
                boxShadow: '0 4px 15px rgba(255, 107, 53, 0.4)',
                '&:hover': {
                  background: 'linear-gradient(90deg, #E65A2B 0%, #E08214 100%)',
                  boxShadow: '0 6px 20px rgba(255, 107, 53, 0.6)',
                },
                '&:disabled': {
                  background: 'rgba(255, 107, 53, 0.5)',
                  boxShadow: 'none',
                }
              }}
            >
              {loading ? <CircularProgress size={24} sx={{ color: 'white' }} /> : 'Se connecter'}
            </Button>
            
            <Box textAlign="center" mt={3}>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                &copy; {new Date().getFullYear()} Gestion de Trésor. Tous droits réservés.
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
};

export default LoginPage;