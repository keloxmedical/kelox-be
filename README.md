# Kelox Medical App - Backend

This is the backend service for the Kelox Medical Application, built with Spring Boot.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway** (Database Migrations)
- **Gradle**

## Prerequisites

- Java 17 or higher
- PostgreSQL database
- Gradle (included via wrapper)

## Configuration

The application is configured to connect to PostgreSQL. Update the database credentials in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kelox_db
    username: postgres
    password: postgres
```

## Setting Up PostgreSQL

### Option 1: Using Docker (Recommended)

1. Make sure Docker is installed and running
2. Start PostgreSQL with Docker Compose:
   ```bash
   docker-compose up -d
   ```
3. The database `kelox_db` will be created automatically
4. To stop: `docker-compose down`

### Option 2: Local PostgreSQL Installation

1. Install PostgreSQL:
   ```bash
   brew install postgresql@15
   brew services start postgresql@15
   ```

2. Create the database:
   ```bash
   createdb kelox_db
   ```

## Running the Application

1. Make sure PostgreSQL is running (either via Docker or local installation)

2. Run the application using Gradle wrapper:
   ```bash
   ./gradlew bootRun
   ```

3. The application will start on `http://localhost:8080/api`

4. Test the health endpoint:
   ```bash
   curl http://localhost:8080/api/health
   ```

## Building the Application

```bash
./gradlew build
```

## Running Tests

```bash
./gradlew test
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/kelox/backend/
│   │       └── KeloxApplication.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/
```

## Database Migrations

This project uses **Flyway** for database version control. 

- Migration files: `src/main/resources/db/migration/`
- Naming: `V{VERSION}__{DESCRIPTION}.sql`
- Migrations run automatically on application startup

**See [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) for detailed workflow and best practices.**

### Quick Commands

```bash
# Check migration status
./gradlew flywayInfo

# Apply migrations manually
./gradlew flywayMigrate

# Validate migrations
./gradlew flywayValidate
```

## API Documentation

All API endpoints are available at: `http://localhost:8080/api`

**See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference with examples.**

### Quick API Overview

#### Admin Endpoints (Require `X-Admin-Secret` header)

**Users:**
- `POST /admin/users` - Create user
- `GET /admin/users` - Get all users
- `GET /admin/users/{id}` - Get user by ID

**Hospitals:**
- `POST /admin/hospitals` - Create hospital profile
- `PUT /admin/hospitals/{id}/assign-owner` - Assign owner to hospital
- `GET /admin/hospitals` - Get all hospitals
- `GET /admin/hospitals/{id}` - Get hospital by ID

**Public:**
- `GET /health` - Health check (no auth required)

### Admin Secret

Default admin secret (change in production!):
```
X-Admin-Secret: kelox-admin-secret-2024
```

Configure in `application.yml`:
```yaml
admin:
  secret-code: your-secret-here
```

