# MEDA Service

## Overview
The MEDA Service manages MEDA program projects, advances, and supporting documents. MEDA (Moroccan Agency for Sustainable Development) focuses on development programs and working capital management. This service handles the complete lifecycle of MEDA projects and financial operations.

## Features
- MEDA program project management
- Advance management for projects
- Supporting document handling
- Integration with external systems (BAM, Comptabilite)
- Role-based access control
- Comprehensive reporting capabilities
- PDF document generation
- Audit logging

## Technology Stack
- Spring Boot 3.1.5
- Spring Data JPA
- Spring Cloud OpenFeign
- PostgreSQL database
- Java 17+

## Configuration
The MEDA Service runs on port 8085 and uses a PostgreSQL database for data persistence. It includes configurations for JWT validation, integration with external systems, and business rules.

### Key Configuration Properties
```yaml
server:
  port: 8085

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/meda_db2
    username: postgres
    password: 22122002
  jpa:
    hibernate:
      ddl-auto: update

meda:
  integration:
    bam:
      enabled: true
      url: http://localhost:8090
      timeout: 30000
    comptabilite:
      enabled: true
      url: http://localhost:8091
      timeout: 30000
  business:
    validation:
      strict-currency-validation: true
      max-amount-per-document: 1000000
```

## Database Entities
The service manages several key entities:

### Project
- `id` - Unique identifier
- `projectNumber` - Project number
- `projectName` - Project name
- `description` - Project description
- `startDate` - Project start date
- `endDate` - Project end date
- `budget` - Project budget
- `status` - Project status

### Advance (Avenant)
- `id` - Unique identifier
- `advanceNumber` - Advance number
- `projectId` - Associated project
- `amount` - Advance amount
- `requestDate` - Request date
- `approvalDate` - Approval date
- `status` - Advance status

### Supporting Document (Pièce Justificative)
- `id` - Unique identifier
- `documentNumber` - Document number
- `advanceId` - Associated advance
- `documentType` - Type of document
- `amount` - Document amount
- `submissionDate` - Submission date
- `status` - Document status

## API Endpoints
- `GET /api/meda/projects` - Get all projects
- `GET /api/meda/projects/{id}` - Get project by ID
- `POST /api/meda/projects` - Create a new project
- `PUT /api/meda/projects/{id}` - Update project
- `DELETE /api/meda/projects/{id}` - Delete project

- `GET /api/meda/advances` - Get all advances
- `GET /api/meda/advances/{id}` - Get advance by ID
- `POST /api/meda/advances` - Create a new advance
- `PUT /api/meda/advances/{id}` - Update advance

- `GET /api/meda/documents` - Get all supporting documents
- `GET /api/meda/documents/{id}` - Get document by ID
- `POST /api/meda/documents` - Create a new document
- `PUT /api/meda/documents/{id}` - Update document

## Integration
The MEDA Service integrates with:
- External systems (BAM, Comptabilite) via REST APIs
- Discovery Server for service registration
- Config Server for configuration management
- Gateway Server for API routing

## Security
- JWT token validation for authentication
- Role-based access control
- Secure communication with external systems
- Data encryption for sensitive information

## Business Rules
- Strict currency validation
- Maximum amount limits per document
- Maximum number of documents per project
- Validation of project dates and budgets

## Best Practices
- Regular database backups
- Monitor integration with external systems
- Implement proper error handling and logging
- Validate data integrity for financial operations
- Use connection pooling for database connections
- Implement audit trails for all project and financial operations