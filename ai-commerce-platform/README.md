# AI Commerce Platform

Multi-module Spring Boot e-commerce platform with AI-powered features.

## Project Structure

This project consists of two main services:
- **ecommerce-service**: Core e-commerce functionality (port 8082)
- **chat-service**: Chat/messaging functionality

## Documentation

### Domain Documentation
- [Coupon Domain](docs/coupon.md) - 쿠폰/할인 도메인 문서

## Getting Started

### Prerequisites
- Java 21
- Gradle
- MySQL 8.0+
- Redis (for distributed locks and cache)

### Build & Run

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run ecommerce-service
./gradlew :ecommerce-service:bootRun
```

For detailed build instructions and architecture guidelines, see [CLAUDE.md](CLAUDE.md).

## Tech Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Build Tool**: Gradle
- **Database**: MySQL 8.0
- **Cache/Lock**: Redis, Caffeine
- **Query**: QueryDSL
- **Documentation**: Spring REST Docs + OpenAPI 3

## Architecture

This project follows a clean layered architecture with clear separation of concerns:
- **Presentation Layer**: REST controllers
- **Application Layer**: Service orchestration and use cases
- **Domain Layer**: Core business entities and logic
- **Infrastructure Layer**: JPA, Redis, external integrations

See [CLAUDE.md](CLAUDE.md) for detailed architecture documentation.
