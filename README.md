# Finance Dashboard Backend

A production-aware REST backend for a finance dashboard system built with Java 21
and Spring Boot 4.0.5. The system supports multi-role user management, financial
record operations, dashboard-level analytics with role-aware data scoping, and
several real-world backend features like idempotency, rate limiting.

---

## Live Demo

Swagger UI: https://finance-dashboard-system-backend-production.up.railway.app/swagger-ui/index.html

### Test Credentials

| Role    | Email              | Password    |
|---------|--------------------|-------------|
| Admin   | admin@test.com     | Admin@123    |
| Analyst | analyst@test.com   | Analyst@123  |
| Viewer  | viewer@test.com    | Viewer@123   |

---

## Tech Stack

| Layer         | Technology                        |
|---------------|-----------------------------------|
| Language      | Java 21                           |
| Framework     | Spring Boot 4.0.5                 |
| Database      | PostgreSQL (Railway)              |
| Security      | Spring Security + JWT (jjwt 0.12.6)|
| Rate Limiting | Bucket4j 8.10.1                   |
| Documentation | SpringDoc OpenAPI (Swagger UI)    |
| Build Tool    | Maven                             |
| Deployment    | Railway                           |

---

## Features

### Core
- JWT Authentication with BCrypt password encoding
- Role Based Access Control — three roles (Admin, Analyst, Viewer)
  with method level enforcement using @PreAuthorize
- Financial Records CRUD with filtering by type, category, and amount range
- Soft delete preserves audit trail and referential integrity
- Dashboard APIs with role aware data scoping

### Security
- Rate limiting with Bucket4j — 5 requests/minute on auth endpoints,
  20 requests/minute on general APIs
- Real client IP detection behind Railway proxy using X-Forwarded-For header
- Idempotency on record creation — prevents duplicate submissions on retries
  using request hashing and 10 minute key expiry

### Analytics
- Dashboard summary — total income, expense, net balance
- Category wise totals
- Monthly trends by year
- Recent 5 records activity

### Additional
- Duplicate record detection on business fields
- Pagination on record listing and user record
- Scheduled cleanup of expired idempotency keys every hour
- Global exception handling with proper HTTP status codes
- Swagger UI with JWT authorization support
- Integration tests

---

## Role Permission Matrix

| Action            | Viewer | Analyst | Admin |
|-------------------|--------|---------|-------|
| View records      | ✅     | ✅      | ✅    |
| Create record     | ❌     | ✅      | ✅    |
| Update record     | ❌     | ✅      | ✅    |
| Delete record     | ❌     | ❌      | ✅    |
| View dashboard    | ✅     | ✅      | ✅    |
| View trends       | ❌     | ✅      | ✅    |
| Manage users      | ❌     | ❌      | ✅    |

---

## API Overview

| Module    | Method | Endpoint                        | Access          |
|-----------|--------|---------------------------------|-----------------|
| Auth      | POST   | /api/auth/register              | Public          |
| Auth      | POST   | /api/auth/login                 | Public          |
| Users     | GET    | /api/users                      | Admin           |
| Users     | GET    | /api/users/{id}                 | Admin           |
| Users     | GET    | /api/users/me                   | All roles       |
| Users     | PUT    | /api/users/{id}                 | Admin           |
| Users     | DELETE | /api/users/{id}                 | Admin           |
| Records   | POST   | /api/record                     | Admin, Analyst  |
| Records   | GET    | /api/record/All_Record          | Admin           |
| Records   | GET    | /api/record/{id}                | All roles       |
| Records   | GET    | /api/record/user/{id}           | Admin           |
| Records   | PUT    | /api/record/updateRecord/{id}   | Admin, Analyst  |
| Records   | DELETE | /api/record/delete/{id}         | Admin           |
| Dashboard | GET    | /api/dashboard/summary          | All roles       |
| Dashboard | GET    | /api/dashboard/by-category      | All roles       |
| Dashboard | GET    | /api/dashboard/trends           | Admin, Analyst  |
| Dashboard | GET    | /api/dashboard/recent           | All roles       |

---

## Local Setup

### Prerequisites
- Java 21+
- Maven
- PostgreSQL

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/MOHDJUNAID70/Finance-System---Spring-Boot.git
cd finance-dashboard-backend
```

**2. Create PostgreSQL database**
```sql
CREATE DATABASE finance_dashboard;
```

**3. Configure `application.properties`**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_dashboard
spring.datasource.username=your_username
spring.datasource.password=use your DB password
```

**4. Run the application**
```bash
mvn spring-boot:run
```

**5. Open Swagger UI**
```
http://localhost:9090/swagger-ui/index.html
```

---

## Project Structure
```
src/main/java/zorvyn/assessment/
├── Config/          # SecurityConfig, SwaggerConfig
├── Controller/      # Auth, User, Record, Dashboard, Audit
├── Service/         # Business logic layer
├── Repository/      # JPA repositories
├── Model/           # JPA entities
├── DTO/             # Request and Response DTOs
├── JWT/             # JwtFilter, JwtUtil
├── Exception/       # GlobalExceptionHandler, custom exceptions, Exception Response
├── Idempotency/     # Idempotency key cleanup, Idempotency key entity, Idempotency Service
├── Mapper/          # UserResponse, RecordResponse
├── Specification/             # Record, user
└── Enums/           # Role, RecordType, UserStatus
```

---

## Design Decisions

- **Soft delete** used instead of hard delete to preserve audit trail
  and maintain referential integrity with user records
- **Idempotency keys** expire after 10 minutes. Same key with different
  request data returns 400 to prevent misuse. Same key with same data
  returns cached response to prevent duplicate record creation
- **Dashboard is role aware** — Admins see global totals across all users,
  Analysts and Viewers see only their own financial summary
- **Rate limiting is per IP** using Bucket4j with X-Forwarded-For header
  support for accurate IP detection behind Railway's reverse proxy
- **Duplicate record detection** — two records with same amount, type,
  category, date and user are considered duplicates and rejected

---

## Assumptions

- Two records with same amount, type, category, date and user
  are considered duplicates
- Idempotency keys expire after 10 minutes
- Inactive users cannot login
- Viewers can view all records but only see their own dashboard summary
- Dashboard trends are restricted to Admin and Analyst as they
  are analytical insights not basic summaries
- Analysts can create and update only their own records.
  Admins have full access to all records.

---

## Trade-offs

- **In-memory rate limiting** using Bucket4j resets on app restart.
  Production would use Redis backed Bucket4j for persistence
- **JWT is stateless** — tokens cannot be invalidated before expiry.
  Production would use a token blacklist or refresh token mechanism
  
---

## Testing

### Run Tests
```bash
mvn test
```

### Test Coverage
- Integration tests — Auth endpoints, Record endpoints with
  role based access control verification
