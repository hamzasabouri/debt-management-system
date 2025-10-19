# Dette Intérieur Service

## Overview
The Dette Intérieur Service is a microservice that manages internal debt operations within the financial system. It handles adjudications (auctions), equipment bonds, commissions, and deposit interests.

## Features

### 1. Adjudications (Auctions)
- Management of auction processes
- Tracking of auction status (In Progress, Closed, Cancelled)
- Association with credit/debit notices

### 2. Equipment Bonds
- Management of equipment bond subscriptions
- Tracking of bond status (Subscribed, Repaid, Rejected, In Progress, Expired)
- Association with advice notices

### 3. Commissions
- Handling of Maroclear and BAM commissions
- Tracking of commission status (Pending, In Progress, Paid, Rejected)
- Workflow management for complete processing

### 4. Deposit Interests
- Calculation and management of deposit interests
- Tracking of interest status (Calculated, Processed, Accounted, Transmitted, Rejected)
- Support for different fund types (Local Authorities, Treasury Deposits)

### 5. Notification System
- Real-time monitoring of service connections
- Alert system for critical events
- Notification history and statistics

## Technology Stack
- Java 17+
- Spring Boot 3.1.5
- Spring Cloud 2022.0.4
- Spring Data JPA
- Spring Security
- Maven 3.6+
- PostgreSQL (or specified database)

## Entity Model

### Adjudication
- `id` - Unique identifier
- `numeroAdjud` - Auction number
- `dateAdjud` - Auction date
- `montantTotal` - Total amount
- `statut` - Status (EN_COURS, CLOTUREE, ANNULEE)

### Equipment Bond (BonEquipement)
- `id` - Unique identifier
- `numeroBon` - Bond number
- `dateSouscription` - Subscription date
- `montant` - Amount
- `statut` - Status (SOUSCRIT, REMBOURSE, REJETE, EN_COURS, EXPIRE)

### Commission
- `id` - Unique identifier
- `typeCommission` - Commission type (MAROCLEAR, BAM)
- `montant` - Amount
- `statut` - Status (EN_ATTENTE, EN_COURS_TRAITEMENT, PAYE, REJETE)

### Deposit Interest (InteretDepot)
- `id` - Unique identifier
- `typeFonds` - Fund type (COLLECTIVITE_LOCALE, DEPOT_TRESOR)
- `numeroCompte` - Account number
- `montant` - Interest amount
- `statut` - Status (CALCULE, PRIS_EN_CHARGE, COMPTABILISE, TRANSMIS, REJETE)

### Notification
- `id` - Unique identifier
- `module` - Module (DETTE_INTERIEUR, DETTE_TRESOR, MEDA, etc.)
- `typeNotification` - Notification type (AVIS_CREDIT, AVIS_DEBIT, etc.)
- `message` - Notification message
- `statut` - Status (NON_LU, LU, TRAITE)

## API Endpoints
See [API_ENDPOINTS.md](API_ENDPOINTS.md) for detailed API documentation.

## Dashboard
The service includes a comprehensive React-based dashboard for managing all aspects of internal debt operations:
- Real-time data visualization
- Interactive tables for all entities
- Notification management system
- Role-based access control

## Setup and Installation
1. Ensure Java 17+ and Maven 3.6+ are installed
2. Clone the repository
3. Navigate to the dette-interieur-service directory
4. Run `mvn clean install` to build the project
5. Run `mvn spring-boot:run` to start the service

## Configuration
The service is configured through the Config Server using the `dette-interieur-service.yml` file in the config-repo.

## Integration
This service integrates with:
- Config Server for centralized configuration
- Discovery Server for service registration
- Gateway Server for API routing
- Other microservices in the ecosystem

## Security
- JWT-based authentication
- Role-based authorization
- Secure API endpoints

## Monitoring
- Health checks via Spring Boot Actuator
- Logging and monitoring capabilities
- Notification system for service events