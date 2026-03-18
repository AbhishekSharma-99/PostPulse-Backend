# 🚀 PostPulse-Backend

![Build Status](https://github.com/AbhishekSharma-99/PostPulse-Backend/actions/workflows/ci-cd.yml/badge.svg)
![Java Version](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)
![Status](https://img.shields.io/badge/Status-Active-success)

**PostPulse** is a high-performance, scalable RESTful API built with **Java 21** and **Spring Boot 3.x**. It serves as the engine for a modern blogging ecosystem, featuring secure JWT authentication, role-based access control, and a fully automated CI/CD pipeline.

---

## 🛠️ Tech Stack & Tools

* **Backend:** Java 21, Spring Boot 3.x, Spring Data JPA, Spring Security (JWT)
* **Database:** MySQL 8.0
* **DevOps & CI/CD:** Docker, Docker Hub, GitHub Actions
* **Testing & Quality:** JUnit 5, Mockito, JaCoCo (Code Coverage)
* **Documentation:** Swagger UI / OpenAPI 3.0

---

## ✨ Key Features

* **🔐 Secure Authentication:** Stateless JWT-based login with Role-Based Access Control (Admin/User).
* **📝 Content Management:** Full CRUD operations for Posts, Categories, and Comments.
* **🔍 Search & Discovery:** Keyword-based search functionality for blog content.
* **⚙️ Automated Pipeline:** Every commit is automatically built, tested, and pushed to Docker Hub.
* **🐳 Containerized:** Fully dockerized environment with Docker Compose for seamless deployment and consistency.

---

## 🏗️ Architecture Highlights

* **Clean Architecture:** Strict separation of concerns (Controller → Service → Repository).
* **DTO Pattern:** Used Data Transfer Objects to ensure API security and decouple internal entities.
* **Exception Handling:** Centralized global exception handling for consistent API error responses.
* **Quality Gate:** Integrated **JaCoCo** into the CI/CD pipeline to monitor and maintain code coverage.

---

## 📸 API Documentation & Preview

> **Full Documentation:** Once the application is running, access the interactive Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

### Key Endpoints Highlight

| Feature | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/v1/auth/login` | Secure JWT Login |
| **Auth** | `POST` | `/api/v1/auth/register` | Register a new user |
| **Posts** | `GET` | `/api/v1/posts` | Paginated and Sorted Feed |
| **Posts** | `POST` | `/api/v1/posts` | Create a new post |
| **Search** | `GET` | `/api/v1/posts/search` | Dynamic Keyword Search |
| **Comments** | `POST` | `/api/v1/posts/{postId}/comments` | Add a comment to a post |
| **Categories** | `GET` | `/api/v1/categories` | List all categories |

*(Screenshots of Swagger UI coming soon!)*

---

## 🚀 Getting Started

### Prerequisites

* Docker & Docker Compose
* *OR* Java 21 & MySQL 8.0

---

### Option 1: Run with Docker (Recommended)

A `docker-compose.yml` is included in the repository — it spins up both the Spring Boot app and a MySQL 8.0 container automatically.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/AbhishekSharma-99/PostPulse-Backend.git
   cd PostPulse-Backend
   ```

2. **Create your `.env` file** in the project root (see [Environment Variables](#-environment-variables) below).

3. **Start the full stack:**
   ```bash
   docker-compose up -d
   ```

The API will be available at `http://localhost:8080`.

---

### Option 2: Local Development

1. Make sure MySQL 8.0 is running locally and a database is created.
2. Copy the Spring Boot and JWT variables from the `.env` section below into `src/main/resources/application.properties`, adjusting the datasource URL to point to `localhost` instead of `mysql`.
3. **Build and Run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

---

## 🔐 Environment Variables

Create a `.env` file in the project root before running with Docker. **Never commit this file — it is already listed in `.gitignore`.**

```env
# ──────────────────────────────────────────────
# MySQL Container Configuration (used by Docker)
# ──────────────────────────────────────────────
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=myblog
MYSQL_USER=user
MYSQL_PASSWORD=password

# ──────────────────────────────────────────────
# Spring Boot Application Configuration
# ──────────────────────────────────────────────
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/myblog
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# ──────────────────────────────────────────────
# Security Configuration
# ──────────────────────────────────────────────
JWT_SECRET_KEY=your_strong_secret_key_here
JWT_EXPIRATION_MILLISECONDS=604800000
```

> **Note:** `SPRING_DATASOURCE_URL` uses `mysql` as the hostname — this refers to the MySQL service name defined in `docker-compose.yml`. For local development without Docker, change it to `localhost`.

> ⚠️ **Security Reminder:** Replace `JWT_SECRET_KEY` with a securely generated secret before deploying to any non-local environment. Never use the example key in production.

---

## 🤖 CI/CD Workflow

This project utilizes a modern **GitHub Actions** pipeline to ensure code quality on every push:

1. **Build:** Compiles the project using Maven and JDK 21.
2. **Test:** Executes JUnit tests and generates a **JaCoCo** coverage report.
3. **Package:** Creates a Docker image tagged with the specific `git-sha` for full traceability.
4. **Deploy:** Pushes the verified image to **Docker Hub**.

---

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

**Maintained by [Abhishek Sharma](https://github.com/AbhishekSharma-99)**
