# PostPulse Backend

![Build Status](https://github.com/AbhishekSharma-99/PostPulse-Backend/actions/workflows/ci-cd.yml/badge.svg)
![Java Version](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)
![Status](https://img.shields.io/badge/Status-Active-success)

A RESTful blogging API built with **Java 21 and Spring Boot 3**, designed to demonstrate backend engineering fundamentals in a real-world context — secure authentication, layered architecture, database migration management, automated testing, and a fully containerized CI/CD pipeline.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3, Spring Security, Spring Data JPA |
| Database | MySQL 8.0 |
| Migrations | Flyway |
| Auth | JWT (jjwt 0.13) |
| Mapping | ModelMapper |
| Documentation | Swagger UI / OpenAPI 3.0 |
| Testing | JUnit 5, Mockito, H2 (in-memory), JaCoCo |
| DevOps | Docker, Docker Compose, GitHub Actions |

---

## Key Features

- **JWT Authentication** — Stateless token-based auth with role-based access control (`ADMIN` / `USER`)
- **Full CRUD** — Posts, Categories, and Comments with proper relationship management
- **Flyway Migrations** — All schema changes are versioned SQL files; the database builds and seeds itself on first startup
- **Pagination & Sorting** — Configurable page number, page size, sort field, and sort direction on all list endpoints
- **Keyword Search** — Search across blog post content via query parameter
- **Input Validation** — Request payloads validated with Jakarta Bean Validation (`@NotEmpty`, `@Size`)
- **Global Exception Handling** — Consistent `ErrorDetails` response format across all endpoints
- **Swagger UI** — Interactive API documentation available at `/swagger-ui/index.html`
- **Dockerized** — Single `docker-compose up` starts the entire stack

---

## Architecture

```
Request → JwtAuthenticationFilter → Controller → Service → Repository → MySQL
                ↑
         SecurityConfig (role-based endpoint protection)
```

- **Strict layered separation** — Controller → Service → Repository, no layer bypasses
- **DTO / Payload pattern** — The API never exposes internal entity structure directly; all I/O goes through `payload` classes
- **Flyway-first schema management** — Hibernate is set to `validate` only; Flyway owns all DDL. Any entity-to-table mismatch fails fast at startup
- **Profile-based configuration** — `dev` profile for local development, `prod` profile for Docker deployment

---

## Database Migrations

Schema is managed entirely by Flyway. On startup, Flyway runs any pending migrations in order before the application accepts traffic.

```
resources/db/migration/
├── V1__Initial_Schema_and_Roles.sql   — creates all tables, seeds ROLE_ADMIN and ROLE_USER
└── V2__Seed_Dummy_Data.sql            — seeds demo users, categories, posts, and comments
```

Hibernate is configured with `ddl-auto=validate` — it verifies entity-to-table mapping at startup but never touches the schema itself.

---

## Getting Started

### Prerequisites

- Docker and Docker Compose, **or**
- Java 21 and MySQL 8.0 running locally

---

### Option 1: Docker (Recommended)

```bash
git clone https://github.com/AbhishekSharma-99/PostPulse-Backend.git
cd PostPulse-Backend

cp .env.example .env
# Edit .env with your values (see Environment Variables section below)

docker-compose up -d
```

The API starts at `http://localhost:8080`. Flyway runs migrations automatically — the database is fully seeded on first startup.

---

### Option 2: Local Development

1. Ensure MySQL 8.0 is running and create a database named `postpulse`
2. Copy `.env.example` to `.env` and fill in your local credentials
3. Update `SPRING_DATASOURCE_URL` to use `localhost` instead of `mysql`

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values. Never commit `.env` — it is already in `.gitignore`.

```env
# MySQL container configuration (used by Docker Compose)
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=postpulse
MYSQL_USER=your_db_user
MYSQL_PASSWORD=your_db_password

# Spring Boot datasource
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/postpulse
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# JWT
JWT_SECRET_KEY=your_minimum_32_character_secret_key_here
JWT_EXPIRATION_MILLISECONDS=604800000
```

> `SPRING_DATASOURCE_URL` uses `mysql` as the hostname — this is the Docker Compose service name. For local development without Docker, change it to `localhost`.

---

## Demo Credentials

The V2 migration seeds the following accounts for testing. All share the same password.

| Username | Email | Role | Password |
|----------|-------|------|----------|
| `abhishek_dev` | `abhishek@postpulse.com` | ADMIN + USER | `Password@123` |
| `priya_writes` | `priya@postpulse.com` | USER | `Password@123` |
| `rohan_tech` | `rohan@postpulse.com` | USER | `Password@123` |

Use the `/api/v1/auth/login` endpoint to obtain a JWT, then click **Authorize** in Swagger UI and paste the token.

---

## API Documentation

Interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

### Key Endpoints

| Feature | Method | Endpoint |
|---------|--------|----------|
| Register | `POST` | `/api/v1/auth/register` |
| Login | `POST` | `/api/v1/auth/login` |
| Get all posts (paginated) | `GET` | `/api/v1/posts?pageNo=0&pageSize=10&sortBy=id&sortDir=asc` |
| Get post by ID | `GET` | `/api/v1/posts/{id}` |
| Create post | `POST` | `/api/v1/posts` |
| Update post | `PUT` | `/api/v1/posts/{id}` |
| Delete post | `DELETE` | `/api/v1/posts/{id}` |
| Get posts by category | `GET` | `/api/v1/posts/category/{categoryId}` |
| Search posts | `GET` | `/api/v1/posts/search?query=spring` |
| Get all categories | `GET` | `/api/v1/categories` |
| Add comment to post | `POST` | `/api/v1/posts/{postId}/comments` |

> Admin-only endpoints (create, update, delete posts and categories) require a JWT from an `ADMIN` account.

---

## CI/CD Pipeline

Every push to `main` triggers the GitHub Actions pipeline:

```
Push to main
     │
     ▼
Build & Test (Maven + JDK 21)
     │── Runs all JUnit tests against H2 in-memory DB
     │── Generates JaCoCo coverage report
     │── Uploads report as build artifact
     │
     ▼ (on success)
Docker Build & Push
     │── Builds image from Dockerfile
     │── Pushes two tags to Docker Hub:
     │       :latest
     │       :<git-sha>  (for full traceability)
     └── Uses GitHub Actions layer caching for faster builds
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/postpulse/
│   │   ├── config/         — SecurityConfig (Spring Security, JWT wiring, Swagger security scheme)
│   │   ├── controller/     — REST controllers
│   │   ├── service/        — business logic interfaces
│   │   │   └── impl/       — service implementations
│   │   ├── repository/     — Spring Data JPA repositories
│   │   ├── entity/         — JPA entities
│   │   ├── payload/        — Data Transfer Objects (DTOs) for all API I/O
│   │   ├── security/       — JWT filter, UserDetails service, authentication entry point
│   │   ├── exception/      — global exception handler and custom exception classes
│   │   └── utils/          — AppConstants and dev utilities
│   └── resources/
│       ├── db/migration/   — Flyway SQL migration files
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── test/
    ├── java/com/postpulse/
    │   ├── repository/     — repository layer tests
    │   └── service/impl/   — service implementation tests
    └── resources/
        └── application.properties  — H2 in-memory config, Flyway disabled for tests
```

---

## Maintained by [Abhishek Sharma](https://github.com/AbhishekSharma-99)