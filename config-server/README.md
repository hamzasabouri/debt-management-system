# Config Server

## Overview
The Config Server is a centralized configuration management service for all microservices in the system. It provides a single source of truth for configuration properties and enables dynamic configuration updates without requiring service restarts.

## Features
- Centralized configuration management
- Native file system storage for configuration files
- Git integration support (disabled in current configuration)
- Configuration encryption and decryption capabilities
- Health monitoring and management endpoints

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Config Server
- Java 17+

## Configuration
The Config Server is configured to use native file system storage, with configuration files located in the `config-repo` directory. The server runs on port 8888.

### Application Properties
```yaml
spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: file:./config-repo/
        git:
          enabled: false
```

## How It Works
1. Services request their configuration from the Config Server at startup
2. Config Server retrieves the appropriate configuration file from the `config-repo` directory
3. Services receive their configuration properties and initialize accordingly
4. Configuration updates can be refreshed without restarting services

## API Endpoints
- `/{application}/{profile}` - Retrieve configuration for an application with a specific profile
- `/{application}/{profile}/{label}` - Retrieve configuration for an application with a specific profile and version label
- `/actuator/health` - Health check endpoint
- `/actuator/info` - Information endpoint

## Service Registration
The Config Server registers itself with the Discovery Server to enable other services to locate it dynamically.

## Security
Security is disabled in the development configuration to simplify setup. In production, authentication should be enabled to protect configuration data.

## Usage
Other services in the system are configured to fetch their configuration from this Config Server by specifying:
```yaml
spring:
  config:
    import: "optional:configserver:http://localhost:8888"
```

## Monitoring
The Config Server exposes management endpoints for monitoring:
- Health status
- Configuration refresh capabilities
- Audit trails for configuration access

## Refreshing Configuration
To refresh configuration for a client service without restarting it:
```bash
curl -X POST http://localhost:8080/actuator/refresh
```

## Integration
The Config Server integrates with:
- Discovery Server for service registration
- All other microservices for configuration distribution