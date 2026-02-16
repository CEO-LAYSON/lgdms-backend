# LPG Gas Distribution Management System (LGDMS)

## Overview
Enterprise-grade system for managing LPG distribution operations including inventory, sales, empty cylinder tracking, and credit management.

## Features
- Multi-location inventory management (HQ, Branches, Vehicles)
- Empty cylinder tracking with refill compliance
- Role-based access control with 6 user types
- Credit management with limit enforcement
- Real-time dashboard and alerts
- Comprehensive reporting suite

## Tech Stack
- Java 17
- Spring Boot 3.x
- PostgreSQL/MySQL
- Redis (caching)
- JWT Security
- Flyway Migrations
- Docker

## Prerequisites
- JDK 17
- Docker & Docker Compose
- Maven 3.8+

## Quick Start
```bash
# Clone and run
./mvnw clean install
docker-compose up -d
./mvnw spring-boot:run
