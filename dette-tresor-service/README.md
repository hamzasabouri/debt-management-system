# Dette Trésor Service

## Overview
The Dette Trésor Service manages treasury debt operations within the financial system. It handles various aspects of treasury debt including loans, payment orders, credit and debit advices, settlement letters, and echeanciers (payment schedules).

## Features
- Loan management
- Payment order processing
- Credit and debit advice handling
- Settlement letter generation
- Echeancier (payment schedule) management
- PDF document generation
- Integration with external systems (BAM, DTFE)
- Comprehensive reporting capabilities

## Technology Stack
- Spring Boot 3.1.5
- Spring Data JPA
- Spring Cloud OpenFeign
- PostgreSQL database
- Java 17+

## Configuration
The Dette Trésor Service runs on port 8084 and uses a PostgreSQL database for data persistence. It includes configurations for JWT validation, PDF generation, and integration with external systems.

### Key Configuration Properties
```yaml
server:
  port: 8084

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dette_tresor_db2
    username: postgres
    password: 22122002
  jpa:
    hibernate:
      ddl-auto: update

pdf:
  output-directory: ./pdf-output
  template-directory: ./pdf-templates

integration:
  bam:
    base-url: http://localhost:9001
    timeout: 30000
  dtfe:
    base-url: http://localhost:9002
    timeout: 30000
```

## Database Entities
The service manages several key entities:

### Loan (Prêt)
- `id` - Unique identifier
- `loanNumber` - Loan number
- `borrowerName` - Borrower name
- `amount` - Loan amount
- `interestRate` - Interest rate
- `startDate` - Loan start date
- `endDate` - Loan end date
- `status` - Loan status

### Payment Order (Ordre de Paiement)
- `id` - Unique identifier
- `orderNumber` - Order number
- `loanId` - Associated loan
- `amount` - Payment amount
- `paymentDate` - Payment date
- `status` - Payment status

### Credit Advice (Avis de Crédit)
- `id` - Unique identifier
- `adviceNumber` - Advice number
- `loanId` - Associated loan
- `amount` - Credit amount
- `issueDate` - Issue date
- `status` - Advice status

### Debit Advice (Avis de Débit)
- `id` - Unique identifier
- `adviceNumber` - Advice number
- `loanId` - Associated loan
- `amount` - Debit amount
- `issueDate` - Issue date
- `status` - Advice status

### Settlement Letter (Lettre d'Ordre)
- `id` - Unique identifier
- `letterNumber` - Letter number
- `loanId` - Associated loan
- `amount` - Settlement amount
- `issueDate` - Issue date
- `status` - Letter status

### Echeancier (Payment Schedule)
- `id` - Unique identifier
- `loanId` - Associated loan
- `installmentNumber` - Installment number
- `dueDate` - Due date
- `amount` - Installment amount
- `status` - Installment status

## API Endpoints
- `GET /api/dette-tresor/loans` - Get all loans
- `GET /api/dette-tresor/loans/{id}` - Get loan by ID
- `POST /api/dette-tresor/loans` - Create a new loan
- `PUT /api/dette-tresor/loans/{id}` - Update loan
- `DELETE /api/dette-tresor/loans/{id}` - Delete loan

- `GET /api/dette-tresor/payment-orders` - Get all payment orders
- `GET /api/dette-tresor/payment-orders/{id}` - Get payment order by ID

- `GET /api/dette-tresor/credit-advices` - Get all credit advices
- `GET /api/dette-tresor/credit-advices/{id}` - Get credit advice by ID

- `GET /api/dette-tresor/debit-advices` - Get all debit advices
- `GET /api/dette-tresor/debit-advices/{id}` - Get debit advice by ID

- `GET /api/dette-tresor/settlement-letters` - Get all settlement letters
- `GET /api/dette-tresor/settlement-letters/{id}` - Get settlement letter by ID

- `GET /api/dette-tresor/echeanciers` - Get all echeanciers
- `GET /api/dette-tresor/echeanciers/{id}` - Get echeancier by ID

## PDF Generation
The service can generate PDF documents for various entities:
- Loan agreements
- Payment orders
- Credit and debit advices
- Settlement letters
- Echeanciers

## Integration
The Dette Trésor Service integrates with:
- External systems (BAM, DTFE) via REST APIs
- Discovery Server for service registration
- Config Server for configuration management
- Gateway Server for API routing

## Security
- JWT token validation for authentication
- Role-based access control
- Secure communication with external systems
- Data encryption for sensitive information

## Best Practices
- Regular database backups
- Monitor integration with external systems
- Implement proper error handling and logging
- Validate data integrity for financial operations
- Use connection pooling for database connections
- Implement audit trails for all financial transactions