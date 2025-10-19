# Login Service

## Overview
The Login Service is responsible for user authentication and JWT token generation. It validates user credentials and issues secure tokens that can be used by other services to authenticate requests. This service acts as the entry point for user access to the system.

## Features
- User authentication with username and password
- JWT token generation and validation
- Integration with User Service for user data
- Password encryption
- Role-based access control
- Session management

## Technology Stack
- Spring Boot 3.1.5
- Spring Security
- Spring Cloud OpenFeign
- Java 17+
- JWT (JSON Web Tokens)

## Configuration
The Login Service runs on port 8083 and integrates with the User Service to validate user credentials. It uses a shared JWT secret for token generation and validation.

### Key Configuration Properties
```yaml
server:
  port: 8083

jwt:
  secret: [JWT_SECRET]
  expiration: 86400000 # 24 hours

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
```

## How It Works
1. User submits login credentials (username and password)
2. Login Service validates credentials by calling the User Service
3. If credentials are valid, Login Service generates a JWT token
4. Token is returned to the client for use in subsequent requests
5. Other services validate the JWT token to authenticate requests

## API Endpoints
- `POST /api/auth/login` - Authenticate user and generate JWT token
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/profile` - Get authenticated user profile
- `POST /api/auth/logout` - Invalidate user session

## JWT Token Structure
The JWT tokens contain the following claims:
- `sub` - Subject (user identifier)
- `roles` - User roles/authorities
- `iat` - Issued at timestamp
- `exp` - Expiration timestamp

## Security
- Passwords are encrypted using BCrypt
- JWT tokens are signed with a secret key
- Tokens have a configurable expiration time
- CSRF protection
- CORS configuration

## Integration
The Login Service integrates with:
- User Service via Feign client for user validation
- Discovery Server for service registration
- Config Server for configuration management
- Gateway Server for API routing

## Feign Client
The Login Service uses Feign clients to communicate with the User Service:
```java
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @PostMapping("/api/users/validate")
    UserValidationResponse validateUser(@RequestBody UserValidationRequest request);
}
```

## Best Practices
- Use HTTPS in production environments
- Store JWT secrets securely
- Implement proper password policies
- Use short-lived tokens with refresh token mechanisms
- Log authentication attempts for security monitoring
- Implement rate limiting for login attempts