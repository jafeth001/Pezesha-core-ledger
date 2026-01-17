# Pezesha Core Ledger

## Project Overview

Pezesha Core Ledger is a fintech accounting system designed to manage financial transactions, accounts, and loans with robust ledger capabilities.  
This core ledger system provides **double-entry bookkeeping** functionality suitable for financial institutions.

---

## Features

- Double-Entry Bookkeeping: Full accounting system with debits and credits
- Account Management: Create and manage various account types (Assets, Liabilities, Equity, Income, Expenses)
- Transaction Processing: Handle complex financial transactions with idempotency support
- Loan Management: Complete loan lifecycle management (application, disbursement, repayment, write-off)
- Financial Reporting: Trial balances, balance sheets, and aging reports
- Security: Role-based access control and authentication
- Caching: Multi-level caching for performance optimization
- Concurrency Control: Thread-safe operations with account-level locking

---

## Architecture Components

### Core Modules

#### Controllers
- **AccountController** – Account management endpoints
- **TransactionController** – Transaction processing endpoints
- **LoanController** – Loan lifecycle management endpoints
- **ReportingController** – Financial reporting endpoints

#### Services
- **AccountService** – Account business logic
- **TransactionService** – Transaction processing with concurrency control
- **LoanService** – Complete loan management
- **ReportingService** – Financial report generation

#### Models
- **Account** – Financial account entity
- **Transaction** – Double-entry transaction
- **TransactionEntry** – Debit/Credit transaction entries
- **Loan** – Loan entity

#### Configuration
- **SecurityConfig** – Authorization
- **CacheConfig** – Caching strategies
- **SwaggerConfig** – API documentation

---

## Technical Stack

- **Framework:** Spring Boot 3.x
- **Language:** Java 17
- **Database:** PostgreSQL
- **Security:** Spring Security
- **Documentation:** Swagger / OpenAPI
- **Caching:** Spring Cache Abstraction
- **Testing:** JUnit 5, Mockito

---

## Key Features

### Transaction Processing
- Idempotency support using idempotency keys
- Account-level locking to prevent race conditions
- Automatic validation of balanced transactions
- Full transaction reversal capability

### Account Management
- Parent–child account hierarchy
- Asset, Liability, Equity, Income, Expense account types
- Multi-currency support
- Active and inactive account lifecycle management

### Loan Operations
- Loan application workflow
- Secure disbursement with proper accounting entries
- Repayment processing (principal and interest)
- Loan write-off handling for bad debts

### Reporting
- Trial balance reports
- Balance sheet generation
- Loan aging analysis
- Full transaction history

---

## Security

- Role-based access control
- Endpoint-level authorization
- CSRF protection disabled for APIs (review for production use)

---

## Caching Strategy

- Cached account data for fast access
- Pre-calculated balances
- Cached financial reports
- Multi-level (L1 & L2) caching configuration

---

## Testing

The project includes:

- Unit tests for all services
- Controller integration tests
- Edge-case validation
- Concurrency scenario testing

---

## Running the Project

1. Clone the repository
2. Configure database connection in `application.yaml`
3. Run the application:

```bash
mvn spring-boot:run
