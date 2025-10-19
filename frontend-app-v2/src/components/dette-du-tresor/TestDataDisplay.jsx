import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText
} from '@mui/material';
import { getAllLoans, setAuthToken } from '../../services/dette-tresor/loansService';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const TestDataDisplay = () => {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // Set auth token for API requests
    const token = localStorage.getItem('token');
    if (token) {
      setAuthToken(token);
    } else {
      // Redirect to login if no token
      navigate('/');
      return;
    }

    const fetchLoans = async () => {
      try {
        setLoading(true);
        setError(null);
        console.log('Fetching loans with user:', user);
        const data = await getAllLoans();
        console.log('Received loans data:', data);
        setLoans(data);
      } catch (err) {
        console.error('Error fetching loans:', err);
        setError(err.message || 'Failed to fetch loans');
      } finally {
        setLoading(false);
      }
    };

    fetchLoans();
  }, [user, navigate]);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Test Data Display
      </Typography>
      
      <Paper sx={{ p: 2, mb: 2 }}>
        <Typography variant="h6" gutterBottom>
          User Information:
        </Typography>
        {user ? (
          <List>
            <ListItem>
              <ListItemText primary="Username" secondary={user.username} />
            </ListItem>
            <ListItem>
              <ListItemText 
                primary="Roles" 
                secondary={user.roles ? user.roles.join(', ') : 'No roles'} 
              />
            </ListItem>
          </List>
        ) : (
          <Typography color="error">Not logged in</Typography>
        )}
      </Paper>

      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Loans Data:
        </Typography>
        
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', my: 2 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Box>
            <Typography color="error">Error: {error}</Typography>
            <Typography>Make sure you're logged in with a user that has the "dette du tresor" role.</Typography>
          </Box>
        ) : loans && loans.length > 0 ? (
          <List>
            {loans.map((loan) => (
              <ListItem key={loan.id}>
                <ListItemText 
                  primary={`Loan #${loan.numeroPret}`} 
                  secondary={`Organization: ${loan.organismeBailleur}, Amount: ${loan.montantTotal} ${loan.devise}`} 
                />
              </ListItem>
            ))}
          </List>
        ) : (
          <Typography>No loans found in the database.</Typography>
        )}
      </Paper>
    </Box>
  );
};

export default TestDataDisplay;