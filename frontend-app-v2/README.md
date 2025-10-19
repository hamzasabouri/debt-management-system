# Frontend Application (V2)

## Overview
The Frontend Application is a React-based web interface that provides a unified user experience for accessing all services in the financial system. It implements role-based access control and provides specialized dashboards for different user roles.

## Features
- Role-based access control
- Responsive design for all device sizes
- Material-UI components for modern UI
- JWT token management
- Real-time data visualization
- Interactive data tables
- Form validation
- Error handling and user feedback

## Technology Stack
- React 18
- Vite
- Material-UI
- React Router
- Axios
- JavaScript (ES6+)

## Prerequisites
- Node.js 16+
- npm 8+

## Installation
```bash
cd frontend-app-v2
npm install
```

## Development
To start the development server:
```bash
npm run dev
```
The application will be available at `http://localhost:5173`

## Build
To create a production build:
```bash
npm run build
```

## Preview
To preview the production build locally:
```bash
npm run preview
```

## Project Structure
```
src/
├── assets/          # Static assets
├── components/      # React components
│   ├── admin/       # Admin dashboard components
│   ├── dette-du-tresor/  # Treasury debt components
│   ├── dette-interieur/  # Interior debt components
│   ├── meda-service/     # MEDA service components
│   ├── Dashboard.jsx     # Main dashboard
│   ├── Login.jsx         # Login component
│   └── ProtectedRoute.jsx # Route protection component
├── context/         # React context providers
├── pages/           # Page components
├── services/        # API service clients
├── utils/           # Utility functions
├── App.jsx          # Main application component
├── main.jsx         # Application entry point
└── index.css        # Global styles
```

## Routing
The application uses React Router for navigation with the following routes:

### Public Routes
- `/` - Login page
- `/login` - Login page

### Protected Routes
- `/dashboard` - Main dashboard (accessible to all authenticated users)
- `/admin/*` - Admin dashboard (admin role required)
- `/dette-du-tresor/*` - Treasury debt dashboard (dette du tresor role required)
- `/meda/*` - MEDA service dashboard (MEDA role required)
- `/dette-interieur/*` - Interior debt dashboard (dette interieur role required)

### Interior Debt Sub-Routes
- `/dette-interieur/adjudications` - Adjudications list
- `/dette-interieur/bon-equipement` - Equipment bonds list
- `/dette-interieur/avis-bon-equipement` - Equipment bond advices list
- `/dette-interieur/commissions` - Commissions list
- `/dette-interieur/interet-depot` - Deposit interests list
- `/dette-interieur/notifications` - Notifications list
- `/dette-interieur/avis-adjudication` - Auction advices list

## Authentication
The application implements JWT-based authentication:
1. User logs in with username and password
2. Server validates credentials and returns JWT token
3. Token is stored in localStorage
4. Token is included in Authorization header for all API requests
5. Token is validated by backend services

## Role-Based Access Control
The application supports the following user roles:
- `admin` - Full system access
- `dette du tresor` - Access to treasury debt features
- `MEDA` - Access to MEDA program features
- `dette interieur` - Access to interior debt features

## Services
The application uses service classes to communicate with backend APIs:
- `authService.js` - Authentication service
- `baseService.js` - Base service with common functionality
- Role-specific services in respective directories

## Styling
The application uses Material-UI with a custom theme:
- Primary color: Orange (#FF6B35)
- Secondary color: Dark gray (#2D3748)
- Responsive design using Material-UI grid system
- Consistent spacing and typography

## Error Handling
The application implements comprehensive error handling:
- Network error detection
- API error message display
- Form validation
- User feedback through alerts and notifications

## Best Practices
- Component-based architecture
- Reusable components
- Proper state management
- Efficient API calls
- Accessibility considerations
- Performance optimization