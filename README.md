# BankCore API

BankCore API is a realistic banking backend portfolio project built with Java 21, Spring Boot, PostgreSQL and Docker. It models the parts of a banking system that matter for backend engineering: authenticated users, customer KYC, bank accounts, atomic financial operations, immutable transaction history, audit logs, suspicious activity alerts and role/property-based access control.

This is not a CRUD demo. The balance of an account is changed only inside transaction-safe services, every balance change creates a bank transaction, and customers are protected from Broken Object Level Authorization by explicit access policies.

## Features

- JWT authentication with BCrypt password hashing.
- Roles: `ROLE_CUSTOMER`, `ROLE_BANK_EMPLOYEE`, `ROLE_ADMIN`.
- Customer onboarding and KYC status management.
- Bank account creation for `VERIFIED` customers only.
- Administrative deposits and withdrawals.
- Customer or staff initiated internal transfers.
- Pessimistic locking for balance-changing operations.
- Frozen and closed account rules.
- Immutable bank transaction records.
- Audit logs for critical actions.
- Suspicious activity alerts for large transfers, transfer bursts, failed attempts and unusual withdrawals.
- Admin dashboard metrics.
- Flyway PostgreSQL migrations and demo seed data.
- Swagger/OpenAPI at `/swagger-ui` and `/api-docs`.
- Unit tests plus Testcontainers PostgreSQL integration test.

## Stack

- Java 21
- Spring Boot 3.4.x
- Maven with `./mvnw`
- Spring Web, Spring Data JPA, Hibernate
- Spring Security and JWT
- PostgreSQL
- Flyway
- Bean Validation
- Lombok
- JUnit 5, Mockito, Testcontainers
- Docker and Docker Compose
- Springdoc OpenAPI
- SLF4J and Logback
- Actuator

## Architecture

```text
src/main/java/com/bankcore
  config
  security
  auth
  user
  customer
  account
  transaction
  alert
  audit
  dashboard
  common
```

Controllers handle HTTP only. Business rules live in services. Repositories own data access. DTOs isolate the API from JPA entities. `AccountAccessPolicy` and `CustomerAccessPolicy` enforce property-level access checks so changing a UUID in a URL does not expose another customer account.

## Database Model

Simplified schema:

```text
users
  id, email, password_hash, role, status, created_at, updated_at, last_login_at

customer_profiles
  id, user_id, identity/contact fields, kyc_status, created_at, updated_at

bank_accounts
  id, customer_id, iban, account_number, currency, balance, status,
  account_type, daily_transfer_limit, version, created_at, updated_at, closed_at

bank_transactions
  id, type, status, source_account_id, target_account_id, amount, currency,
  description, reference, failure_reason, created_at, completed_at, initiated_by_user_id

audit_logs
  id, actor_user_id, action, entity_type, entity_id, ip_address, user_agent, metadata, created_at

suspicious_activity_alerts
  id, account_id, transaction_id, alert_type, severity, message, status,
  created_at, reviewed_at, reviewed_by
```

Flyway migrations live in `src/main/resources/db/migration`.

## Business Rules

- Accounts can be opened only for existing `VERIFIED` customers.
- Account creation generates a unique account number and a realistic Swiss IBAN starting with `CH`.
- Money uses `BigDecimal` and an explicit `CurrencyCode`.
- Deposits and withdrawals require `ROLE_BANK_EMPLOYEE` or `ROLE_ADMIN`.
- Withdrawals and outgoing transfers are blocked on frozen or closed accounts.
- Closed accounts cannot receive future operations.
- Closing an account requires a zero balance.
- Internal transfers require different source and target accounts, matching currencies, sufficient funds and daily limit compliance.
- Transfers create two transaction records: `TRANSFER_OUT` and `TRANSFER_IN`.
- Financial operations are atomic through `@Transactional`.
- Balance-changing account rows are loaded with `PESSIMISTIC_WRITE` locks.

## Suspicious Activity

Thresholds are configurable in `application.yml` under `bankcore.suspicious`.

Alerts are created when:

- A transfer is greater than `10000.00`.
- More than 5 outgoing transfers occur from the same account in 10 minutes.
- More than 3 failed operations occur on the same account in 15 minutes.
- A withdrawal exceeds 50% of the available balance before withdrawal.

Suspicious operations are not automatically blocked in this MVP. Valid transactions can complete and create an alert for employee/admin review.

## Security

- Stateless JWT access tokens.
- Refresh token endpoint without DB persistence for the MVP.
- BCrypt password hashing.
- Method-level security with `@PreAuthorize`.
- Role-level rules for customer, employee and admin workflows.
- Property-level account/customer policies.
- Standard JSON errors for 400/401/403/404/409/500.
- Sensitive Actuator endpoints require `ROLE_ADMIN`.
- CORS is configured from `bankcore.cors.allowed-origins`.

## Local Setup

Requirements:

- Docker Desktop or compatible Docker engine.
- Java 21+ if running outside Docker.

Start the full stack:

```bash
cp .env.example .env
docker compose up --build
```

Open:

- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Run Locally Without Docker App Container

Start PostgreSQL with Docker:

```bash
cp .env.example .env
docker compose up postgres
```

Run the app:

```bash
./mvnw spring-boot:run
```

## Tests

Run all tests:

```bash
./mvnw test
```

Unit tests run without Docker. The integration test uses Testcontainers PostgreSQL and is skipped automatically when Docker is not available.

Note: Docker Engine 29+ rejects the legacy API version that docker-java requests by default (`400 Bad Request` during Docker detection). The build pins `api.version=1.41` (Surefire system property, mirrored in the integration test for IDE runs), which is compatible with Docker 20.10 through 29+.

Coverage report:

```text
target/site/jacoco/index.html
```

## Demo Accounts

All demo users use:

```text
Password123!
```

| Role | Email |
| --- | --- |
| Admin | `admin@bankcore.local` |
| Bank employee | `employee@bankcore.local` |
| Bank employee reviewer | `reviewer@bankcore.local` |
| Disabled employee | `disabled.employee@bankcore.local` |
| Customer Alice | `alice@bankcore.local` |
| Customer Bob | `bob@bankcore.local` |
| Customer Claire | `claire@bankcore.local` |
| Customer Diego | `diego@bankcore.local` |
| Customer Emma | `emma@bankcore.local` |
| Customer Noah, KYC pending | `noah@bankcore.local` |
| Customer Sofia | `sofia@bankcore.local` |
| Locked customer | `locked.customer@bankcore.local` |

Seeded accounts:

| Customer | Account ID | Type | Currency | Balance | Status |
| --- | --- | --- | --- | --- | --- |
| Alice | `20000000-0000-0000-0000-000000000001` | CHECKING | CHF | 38800.00 | ACTIVE |
| Alice | `20000000-0000-0000-0000-000000000002` | SAVINGS | EUR | 5000.00 | ACTIVE |
| Bob | `20000000-0000-0000-0000-000000000003` | CHECKING | CHF | 9200.00 | ACTIVE |
| Claire | `20000000-0000-0000-0000-000000000004` | BUSINESS | CHF | 108400.00 | ACTIVE |
| Claire | `20000000-0000-0000-0000-000000000005` | SAVINGS | USD | 18500.00 | ACTIVE |
| Diego | `20000000-0000-0000-0000-000000000006` | CHECKING | EUR | 12600.00 | ACTIVE |
| Diego | `20000000-0000-0000-0000-000000000007` | CHECKING | CHF | 1500.00 | FROZEN |
| Emma | `20000000-0000-0000-0000-000000000008` | SAVINGS | CHF | 1050.00 | ACTIVE |
| Emma | `20000000-0000-0000-0000-000000000009` | CHECKING | USD | 0.00 | CLOSED |
| Sofia | `20000000-0000-0000-0000-000000000010` | BUSINESS | EUR | 37000.00 | ACTIVE |

The expanded seed also includes completed deposits, withdrawals, internal transfer pairs, failed operations on a frozen account, reviewed/dismissed/open suspicious alerts, and audit log entries.

## Main Endpoints

Authentication:

```text
POST /api/v1/auth/register-customer
POST /api/v1/auth/login
POST /api/v1/auth/refresh
GET  /api/v1/auth/me
```

Customers and accounts:

```text
POST /api/v1/customers
GET  /api/v1/customers
GET  /api/v1/customers/{customerId}
PATCH /api/v1/customers/{customerId}/kyc-status
POST /api/v1/accounts
GET  /api/v1/accounts
GET  /api/v1/accounts/{accountId}
GET  /api/v1/customers/{customerId}/accounts
PATCH /api/v1/accounts/{accountId}/freeze
PATCH /api/v1/accounts/{accountId}/unfreeze
PATCH /api/v1/accounts/{accountId}/close
```

Operations:

```text
POST /api/v1/accounts/{accountId}/deposit
POST /api/v1/accounts/{accountId}/withdraw
POST /api/v1/transfers
```

Transactions, alerts, audit and dashboard:

```text
GET /api/v1/transactions
GET /api/v1/transactions/{transactionId}
GET /api/v1/accounts/{accountId}/transactions
GET /api/v1/accounts/{accountId}/statement
GET /api/v1/alerts
PATCH /api/v1/alerts/{alertId}/review
GET /api/v1/audit-logs
GET /api/v1/admin/dashboard
```

## Example Flow

1. Login as `employee@bankcore.local`.
2. Create a verified customer with `POST /api/v1/customers`.
3. Create an account for that customer with `POST /api/v1/accounts`.
4. Deposit funds using `POST /api/v1/accounts/{accountId}/deposit`.
5. Login as `alice@bankcore.local`.
6. Transfer CHF from Alice to Bob with `POST /api/v1/transfers`.
7. Login as `bob@bankcore.local` and confirm Bob cannot read Alice's account by ID.
8. Login as employee/admin and review `/api/v1/alerts` and `/api/v1/audit-logs`.

## Technical Choices

- Pessimistic locks are used for balance operations because the MVP favors explicit database consistency over optimistic retry complexity.
- The transaction history is append-only at API level; no endpoint deletes or mutates completed financial history.
- Failed operations can be recorded as `FAILED` transactions when the account exists, supporting suspicious activity detection.
- DTO records are used for API contracts. JPA entities remain internal.
- Refresh tokens are stateless for the MVP. Persisted refresh tokens are a planned improvement.

## Why This Project Matters For Backend Engineering

BankCore demonstrates backend skills that matter in enterprise and fintech systems:

- Transaction management for money movement.
- Role and ownership-based access control.
- Domain modeling beyond CRUD.
- Atomic financial operations.
- Auditability and operational traceability.
- Security defaults with JWT, BCrypt and structured errors.
- Testable business services.
- Clean package boundaries and maintainable architecture.

## MVP Limits

- Refresh tokens are not persisted or revocable in the database.
- Multi-currency transfers are intentionally blocked.
- Statements are JSON responses, not PDFs.
- Rate limiting is not implemented.
- The suspicious activity engine is rule-based and synchronous.
- No external KYC provider integration.

## Future Improvements

- Persisted refresh tokens and token revocation.
- Login rate limiting.
- PDF statement export.
- Pagination and filters for transactions.
- Soft delete and user lifecycle workflows.
- Prometheus/Grafana dashboards.
- GitHub Actions CI.
- Kubernetes manifests.
- Simulated exchange rates for multi-currency transfers.
- Transaction reversal workflow.
