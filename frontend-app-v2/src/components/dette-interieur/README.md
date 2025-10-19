# Dette Intérieur Dashboard

This dashboard provides a comprehensive interface for managing internal debt operations including:

## Features

1. **Notifications Management**
   - Real-time monitoring of service connections
   - Alert system for critical events
   - Notification history and statistics

2. **Adjudications (Auctions)**
   - View and manage auction processes
   - Track auction status (In Progress, Closed, Cancelled)
   - Monitor associated credit/debit notices

3. **Equipment Bonds**
   - Manage equipment bond subscriptions
   - Track bond status (Subscribed, Repaid, Rejected, In Progress, Expired)
   - Monitor associated advice notices

4. **Commissions**
   - Handle Maroclear and BAM commissions
   - Track commission status (Pending, In Progress, Paid, Rejected)
   - Workflow management for complete processing

5. **Deposit Interests**
   - Calculate and manage deposit interests
   - Track interest status (Calculated, Processed, Accounted, Transmitted, Rejected)
   - Support for different fund types (Local Authorities, Treasury Deposits)

## Data Tables

### Adjudications Table
- **Columns**: Auction Number, Date, Total Amount, Status
- **Actions**: View details, update status

### Equipment Bonds Table
- **Columns**: Bond Number, Subscription Date, Amount, Status
- **Actions**: View details, process payments

### Commissions Table
- **Columns**: Commission Type, Amount, Status
- **Actions**: Track workflow, mark as paid

### Deposit Interests Table
- **Columns**: Account Number, Amount, Currency, Calculation Date, Status
- **Actions**: Process, account, transmit

### Notifications Table
- **Columns**: Date, Module, Type, Message, Recipient, Status
- **Actions**: Mark as read, archive

## Technical Implementation

The dashboard is built using:
- React with Material-UI components
- Axios for API communication
- JWT-based authentication
- Responsive design for all device sizes

## API Services

All data is fetched from the Dette Intérieur microservice through the following endpoints:
- `/api/dette-interieur/notifications` - Notification management
- `/api/dette-interieur/adjudications` - Auction management
- `/api/dette-interieur/bons-equipement` - Equipment bond management
- `/api/dette-interieur/commissions` - Commission management
- `/api/dette-interieur/interets-depot` - Deposit interest management

## Access Control

The dashboard is protected and only accessible to users with the "dette interieur" role.