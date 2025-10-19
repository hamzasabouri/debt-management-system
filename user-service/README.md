# User Service

## Overview
The User Service manages user accounts, roles, and permissions within the system. It provides CRUD operations for users and roles, handles password encryption, and supports user authentication validation for other services.

## Features
- User management (Create, Read, Update, Delete)
- Role and permission management
- Password encryption using BCrypt
- User authentication validation
- Search and filtering capabilities
- User activation/deactivation
- Audit logging

## Technology Stack
- Spring Boot 3.1.5
- Spring Data JPA
- Spring Security
- MySQL database
- Java 17+

## Configuration
The User Service runs on port 8082 and uses a MySQL database for data persistence. It integrates with the Discovery Server for service registration and the Config Server for configuration management.

### Key Configuration Properties
```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:mysql://localhost:3307/user_management_db
    username: root
    password: 22122002
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

## Database Schema
The User Service uses the following database entities:

### User
- `id` - Unique identifier
- `username` - User's login name
- `email` - User's email address
- `password` - Encrypted password
- `firstName` - User's first name
- `lastName` - User's last name
- `active` - Account status (active/inactive)
- `roles` - Collection of assigned roles

### Role
- `id` - Unique identifier
- `name` - Role name
- `description` - Role description

## API Endpoints
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create a new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/search?searchText={text}` - Search users
- `POST /api/users/{id}/activate` - Activate user
- `POST /api/users/{id}/deactivate` - Deactivate user
- `GET /api/roles` - Get all roles
- `POST /api/users/validate` - Validate user credentials (for Login Service)

## Security
- Passwords are encrypted using BCrypt
- Role-based access control
- Integration with Spring Security
- Secure communication with other services

## Integration
The User Service integrates with:
- Login Service for user authentication validation
- Discovery Server for service registration
- Config Server for configuration management
- Gateway Server for API routing

## Data Initialization
The service supports SQL data initialization for setting up default users and roles. The initialization scripts are located in the `src/main/resources` directory.

## Best Practices
- Use strong password policies
- Regularly audit user accounts and permissions
- Implement proper data backup and recovery procedures
- Monitor user activity for security purposes
- Use connection pooling for database connections
- Implement proper error handling and logging