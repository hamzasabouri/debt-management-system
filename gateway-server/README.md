# Gateway Server

## Overview
The Gateway Server is an API gateway implementation using Spring Cloud Gateway. It acts as a single entry point for all client requests and routes them to the appropriate backend microservices. It provides cross-cutting concerns like security, monitoring, and rate limiting.

## Features
- Dynamic routing to backend services
- Path-based routing
- Load balancing
- Security filtering
- Request/response transformation
- Monitoring and metrics
- CORS support

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Gateway
- Java 17+

## Configuration
The Gateway Server runs on port 8080 and routes requests to various backend services based on path patterns. It integrates with the Discovery Server to locate service instances dynamically.

### Application Properties
```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
        # Additional routes for other services...
```

## How It Works
1. Client sends requests to the Gateway Server
2. Gateway Server matches the request path to configured routes
3. Gateway Server discovers the appropriate service instance through the Discovery Server
4. Gateway Server forwards the request to the backend service
5. Gateway Server returns the response to the client

## Route Configuration
The Gateway Server is configured with routes for each backend service:
- `/api/auth/**` → Login Service
- `/api/users/**` → User Service
- `/api/dette-tresor/**` → Dette Trésor Service
- `/api/dette-interieur/**` → Dette Intérieur Service
- `/api/meda/**` → MEDA Service

## Filters
The Gateway Server applies various filters to requests:
- `StripPrefix` - Removes path prefixes before forwarding to services
- Security filters - Validate JWT tokens
- Logging filters - Log request/response details
- Rate limiting filters - Control request frequency

## Load Balancing
The Gateway Server uses client-side load balancing to distribute requests across multiple instances of backend services:
- Integrates with Discovery Server for service instance discovery
- Uses round-robin algorithm by default
- Supports custom load balancing strategies

## Security
The Gateway Server can handle cross-cutting security concerns:
- JWT token validation
- Authentication and authorization
- SSL termination
- Request filtering

## Monitoring
The Gateway Server exposes management endpoints for monitoring:
- Health status
- Request metrics
- Performance statistics
- Route information

## Integration
The Gateway Server integrates with:
- Discovery Server for service discovery
- Config Server for configuration management
- All backend microservices for request routing

## Best Practices
- Configure appropriate timeouts for backend services
- Implement circuit breaker patterns for fault tolerance
- Use appropriate rate limiting to prevent abuse
- Monitor gateway performance and error rates
- Secure sensitive routes with authentication