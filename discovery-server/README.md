# Discovery Server

## Overview
The Discovery Server is a service registry implementation using Netflix Eureka. It allows microservices to dynamically discover and communicate with each other without hardcoding hostnames and ports. This enables a truly dynamic and scalable microservices architecture.

## Features
- Service registration and discovery
- Health monitoring of registered services
- Load balancing support
- REST API for service management
- Web-based dashboard for monitoring

## Technology Stack
- Spring Boot 3.1.5
- Spring Cloud Netflix Eureka Server
- Java 17+

## Configuration
The Discovery Server runs on port 8761 and is configured to not register itself as a client. It provides a dashboard at `http://localhost:8761` for monitoring registered services.

### Application Properties
```yaml
server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka
```

## How It Works
1. Microservices register themselves with the Discovery Server at startup
2. Services periodically send heartbeats to indicate they are still alive
3. When a service needs to communicate with another service, it queries the Discovery Server for available instances
4. The Discovery Server returns the network locations of available service instances
5. Services can then communicate directly with the discovered instances

## Dashboard
The Discovery Server provides a web-based dashboard at `http://localhost:8761` that shows:
- List of registered applications
- Status of each service instance
- Metadata about each service
- System status information

## API Endpoints
- `/eureka/apps` - List all registered applications
- `/eureka/apps/{applicationName}` - Get details for a specific application
- `/eureka/apps/{applicationName}/{instanceId}` - Get details for a specific service instance
- `/actuator/health` - Health check endpoint

## Service Registration Process
1. Services include the `@EnableDiscoveryClient` annotation
2. Services specify the Discovery Server URL in their configuration
3. At startup, services register with the Discovery Server
4. Services send periodic heartbeats to maintain registration
5. Services are automatically deregistered if heartbeats stop

## Self-Preservation Mode
The Discovery Server has a self-preservation mode that protects registered services during network failures:
- Enabled by default
- Prevents removal of services during network issues
- Can be configured with `eureka.server.enable-self-preservation`

## Integration
The Discovery Server integrates with:
- All microservices in the system for service registration
- Config Server for configuration management
- Gateway Server for routing requests to services

## Best Practices
- Run multiple instances of the Discovery Server for high availability
- Configure appropriate heartbeat intervals
- Monitor service registrations and deregistrations
- Use appropriate timeouts for service health checks