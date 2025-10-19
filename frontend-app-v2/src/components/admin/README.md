# Admin Components

This directory contains components for admin functionality in the application.

## Components

1. **Dashboard.jsx** - Main admin dashboard with navigation sidebar and overview statistics
2. **UserList.jsx** - Component for managing users (CRUD operations)

## Services

1. **userService.js** - Service for interacting with the user and role APIs

## Features

- User management (create, read, update, delete)
- Search functionality for users
- User activation/deactivation
- System statistics overview
- Activity history tracking

## API Endpoints Used

### User Service
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create a new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/search?searchText={text}` - Search users
- `POST /api/users/{id}/activate` - Activate user
- `POST /api/users/{id}/deactivate` - Deactivate user

## Styling

All admin components follow the same styling pattern as the dette-tresor dashboard:
- Professional layout with sidebar navigation
- User profile section with permissions display
- Statistics cards with icons and metrics
- Activity history tracking
- Consistent Material UI styling with proper spacing and color scheme