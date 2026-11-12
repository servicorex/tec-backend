# Tirumala Engineering College (TEC) - Management Portal Backend

Official backend for the Tirumala Engineering College institutional management portal, built using **Java 21 LTS** and **Spring Boot 3.3.x**.

---

## 1. Technology Stack

* **Language / Runtime:** Java 21 LTS (Oracle / Temurin OpenJDK)
* **Framework:** Spring Boot 3.3.5
* **Build System:** Apache Maven 3.9+ / Maven Wrapper (`./mvnw`)
* **Security & Auth:** Spring Security 6, BCrypt (Work Factor 12), Stateless JWT (HMAC-SHA256 via JJWT)
* **Database & Persistence:** Supabase PostgreSQL, Spring Data JPA / Hibernate
* **Schema Management:** Flyway Migrations
* **CSV Processing:** Apache Commons CSV
* **Documentation:** Springdoc OpenAPI (Swagger UI)

---

## 2. Environment Setup

### Prerequisites
1. **Java JDK 21 LTS** installed and configured on your `PATH`.
2. **Maven 3.9+** (or use the included `./mvnw` / `mvnw.cmd` wrapper).

### Environment Configuration
Copy `.env.example` to `.env` and fill in your development credentials:

```bash
cp .env.example .env
```

Required environment variables:
* `SPRING_DATASOURCE_URL`: Supabase PostgreSQL JDBC connection string
* `SPRING_DATASOURCE_USERNAME`: Database username (`postgres`)
* `SPRING_DATASOURCE_PASSWORD`: Supabase database password
* `JWT_SECRET`: 256-bit secret string for token signing
* `JWT_EXPIRATION_HOURS`: Token validity period (e.g., `12`)
* `CORS_ALLOWED_ORIGINS`: Comma-separated frontend URLs

---

## 3. Build & Run Instructions

### Verify Build
```bash
# Windows
.\mvnw.cmd clean verify -DskipTests

# Linux / macOS
./mvnw clean verify -DskipTests
```

### Run Locally
```bash
.\mvnw.cmd spring-boot:run
```

---

## 4. Development Architecture

* **Role Model:** Student and HOD (with designated Superadmin HOD).
* **Identity Model:** Native Roll Number authentication (`Username = Roll No`, `Password = Roll No default`).
* **Zero-Trust Scope:** Department and user identities derived strictly from cryptographically verified JWT claims.
* **Database Relational Integrity:** Single PostgreSQL database on Supabase hosting credentials and academic records.

