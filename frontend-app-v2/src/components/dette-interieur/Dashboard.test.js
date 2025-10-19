import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../../context/AuthContext';
import DetteInterieurDashboard from './Dashboard';

// Mock the useAuth hook
jest.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    user: { username: 'testuser', roles: ['dette interieur'] },
    logout: jest.fn(),
  }),
  AuthProvider: ({ children }) => <div>{children}</div>,
}));

// Mock useNavigate
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => jest.fn(),
}));

describe('DetteInterieurDashboard', () => {
  test('renders dashboard title', () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <DetteInterieurDashboard />
        </AuthProvider>
      </BrowserRouter>
    );
    
    expect(screen.getByText('Tableau de Bord - Dette Intérieur')).toBeInTheDocument();
    expect(screen.getByText('Bienvenue, testuser!')).toBeInTheDocument();
  });

  test('renders all tabs', () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <DetteInterieurDashboard />
        </AuthProvider>
      </BrowserRouter>
    );
    
    expect(screen.getByText('Notifications')).toBeInTheDocument();
    expect(screen.getByText('Adjudications')).toBeInTheDocument();
    expect(screen.getByText('Bons d\'Équipement')).toBeInTheDocument();
    expect(screen.getByText('Commissions')).toBeInTheDocument();
    expect(screen.getByText('Intérêts')).toBeInTheDocument();
  });

  test('renders statistics cards', () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <DetteInterieurDashboard />
        </AuthProvider>
      </BrowserRouter>
    );
    
    expect(screen.getByText('Adjudications')).toBeInTheDocument();
    expect(screen.getByText('Bons Équipement')).toBeInTheDocument();
    expect(screen.getByText('Commissions')).toBeInTheDocument();
    expect(screen.getByText('Intérêts')).toBeInTheDocument();
  });
});