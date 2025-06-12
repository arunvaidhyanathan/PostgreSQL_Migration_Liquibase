# ReadMe.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven-based Spring Boot 3.2.0 application using Java 17 for PostgreSQL database migrations with Liquibase. The project provides RESTful API control over database migrations with multi-environment support.

**Key Features:**

- API-controlled database migrations for Dev/UAT/Prod environments
- Comprehensive logging and audit trail
- SpringDoc OpenAPI integration with Swagger UI
- Environment-specific configuration via YAML
- Postman collection for API testing

## Build and Development Commands

### Maven Commands

- `mvn clean compile` - Compile the project
- `mvn spring-boot:run` - Run the Spring Boot application (default port 8080)
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev` - Run with dev profile
- `mvn spring-boot:run -Dspring-boot.run.profiles=uat` - Run with UAT profile
- `mvn spring-boot:run -Dspring-boot.run.profiles=prod` - Run with production profile
- `mvn test` - Run all tests
- `mvn package` - Build JAR file
- `mvn clean install` - Full build with dependency installation

### API Endpoints

The application provides the following REST endpoints:

- `GET /api/v1/migration/health` - Health check
- `GET /api/v1/migration/status` - Get migration status
- `GET /api/v1/migration/validate` - Validate changelog
- `POST /api/v1/migration/run?contexts=dev` - Run migration for specific environment
- `POST /api/v1/migration/rollback?count=1` - Rollback migrations
- `GET /swagger-ui.html` - Swagger UI documentation
- `GET /api-docs` - OpenAPI specification

### Traditional Liquibase Commands (via Maven)

- `mvn liquibase:update` - Apply pending database changes
- `mvn liquibase:rollback -Dliquibase.rollbackCount=1` - Rollback last changeset
- `mvn liquibase:status` - Show pending changesets
- `mvn liquibase:validate` - Validate changelog syntax

## Environment Configuration

The project uses `application.yml` with profile-specific configurations:

- **dev**: Add PostgreSQL database with specific schema for Dev
- **uat**: Add PostgreSQL database with specific schema for UAT with environment variables support
- **prod**: Add PostgreSQL database with specific schema for Production database with strict security and production contexts

**Current Database Configuration:**

- **Database**: PostgreSQL
- **Schema**: Schema Name
- **SSL**: Required for all environments
- **Connection pooling**: Via HikariCP (Spring Boot default)

## Logging Strategy

- **General logs**: `logs/migration.log` - Application logs with 30-day retention
- **Migration logs**: `logs/liquibase-migration.log` - Detailed migration logs with 90-day retention
- **Console output**: Real-time logging during development
- **Environment-specific log levels**: DEBUG for dev, INFO for UAT, WARN for prod

## Changelog Management

Changesets are organized by version in XML format:

- Context-aware changesets (dev, uat, production)
- Rollback instructions for each changeset
- Comprehensive commenting and documentation
- Foreign key constraints and indexes

## Architecture Notes

### Migration Service Architecture

- **LiquibaseMigrationService**: Core service that manages Liquibase operations programmatically
- **Resource Management**: Uses try-with-resources for proper Connection and Liquibase object lifecycle
- **Error Handling**: Comprehensive error capture with stack traces logged to separate migration log files
- **Thread Safety**: Each operation manages its own database connection lifecycle

### API Layer Design

- **MigrationController**: REST endpoints with OpenAPI documentation
- **Environment-specific operations**: Context parameter controls which changesets execute
- **Standardized responses**: All endpoints return consistent JSON response format with status, message, and timestamp

### Configuration Strategy

- **Liquibase auto-run disabled**: Manual control via API prevents accidental migrations on startup
- **Profile-based database configuration**: Different connection settings per environment
- **Logging separation**: Migration operations logged separately from application logs

## Important Development Notes

- **Liquibase API Compatibility**: Service uses current Liquibase API methods (avoid deprecated `listUnrunChangeSets(Contexts)`)
- **Resource Management**: Always use try-with-resources for Liquibase and Connection objects to prevent resource leaks
- **Error Logging**: Migration failures are captured with full stack traces in dedicated log files
- **Database Connection**: Service obtains connections from DataSource rather than managing connections directly
- **Logback Configuration**: Uses `SizeAndTimeBasedRollingPolicy` for proper log rotation with both time and size constraints
- **Context-aware Migrations**: Changesets can be targeted to specific environments using contexts (dev, uat, production)

## Technology Stack

- Java 17
- Spring Boot 3.2.0 (Web, JPA, Security, Actuator)
- PostgreSQL 42.7.1 driver
- Liquibase 4.32.0 with SLF4J logging
- SpringDoc OpenAPI 2.2.0 for API documentation
- Jackson YAML for configuration
- Logback for logging
- Maven build system

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- API docs: <http://localhost:8080/api-docs>
- All migration endpoints: <http://localhost:8080/api/v1/migration/>*
