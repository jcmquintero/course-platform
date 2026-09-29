# Course Platform

Backend for an online course platform built with Java 21 and Spring Boot.

The application manages courses, instructors, students and enrollments. Enrollment and payment processing use RabbitMQ for asynchronous communication, with PostgreSQL as the main database.

## Tech stack

- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Flyway
- RabbitMQ
- Spring Security with JWT
- Springdoc OpenAPI
- Spring Boot Actuator
- Micrometer
- Docker / Docker Compose
- JUnit 5
- Testcontainers

## Architecture

The project is implemented as a modular Spring Boot application. The main packages are organized around the domain: courses, categories, instructors, students, enrollments, payments and certificates.

HTTP requests follow the usual application flow:

```text
Controller -> Service -> Repository -> PostgreSQL
```

Controllers are responsible for the HTTP layer and validation. Business rules and transaction boundaries are kept in services, while repositories handle persistence.

Asynchronous operations use RabbitMQ:

```text
Service -> Outbox -> RabbitMQ -> Consumer -> Service
```

Messaging code is kept separate from the business logic so RabbitMQ consumers remain small and delegate processing to the corresponding service.

## Domain

A course belongs to a category and an instructor. Courses can be in `DRAFT`, `PUBLISHED` or `ARCHIVED` state and have a maximum number of seats.

An enrollment belongs to a student and a course and follows this lifecycle:

```text
PENDING_PAYMENT -> ACTIVE -> COMPLETED
                         \
                          -> CANCELLED
```

Only published courses can accept enrollments.

When an enrollment is created, a seat is reserved and a pending payment is created in the same transaction. Payment confirmation is processed asynchronously. When progress reaches 100%, the enrollment is completed and certificate generation is triggered through an event.

Cancelled enrollments release their reserved seat.

## Concurrency

Course capacity is protected with an atomic conditional update instead of reading the number of seats and updating it afterwards.

Conceptually, the reservation works as:

```sql
UPDATE courses
SET occupied_seats = occupied_seats + 1
WHERE id = ?
  AND status = 'PUBLISHED'
  AND occupied_seats < max_seats;
```

The affected row count determines whether the reservation succeeded.

This prevents two concurrent requests from both obtaining the last available seat. Seat reservation, enrollment creation and payment creation are part of the same transaction.

There is an integration test that executes two concurrent enrollment attempts against a course with one available seat and verifies that only one succeeds.

## Messaging

RabbitMQ is used for the asynchronous parts of the enrollment lifecycle.

The main events are:

- `EnrollmentCreated`
- `PaymentConfirmed`
- `EnrollmentCompleted`

The application uses the transactional outbox pattern. Business changes and the corresponding outbox event are stored in PostgreSQL in the same transaction. A publisher later sends pending events to RabbitMQ.

Consumers use the `processed_events` table to identify events that have already been processed. This makes message handling idempotent when RabbitMQ delivers the same event more than once.

Queues are configured with retry and dead-letter queues. Messages that continue failing after the configured retries are sent to their corresponding DLQ instead of being requeued indefinitely.

Events use dedicated message contracts rather than serializing JPA entities directly.

## Security

The API uses JWT Bearer authentication with three roles:

- `ADMIN`
- `INSTRUCTOR`
- `STUDENT`

`ADMIN` can manage the complete platform.

`INSTRUCTOR` operations on courses include an ownership check, so an instructor cannot modify or publish a course belonging to another instructor.

`STUDENT` operations on enrollments also include ownership checks. A student cannot create an enrollment for another student or update/cancel another student's enrollment.

The `/api/auth/token` endpoint is a simplified authentication mechanism for this technical exercise. It allows tokens to be generated for testing the different roles and is not intended to represent a production identity system.

## API

The API includes operations for categories, instructors, courses and enrollments.

Course search supports pagination, sorting and filters including:

- category
- level
- price range
- title
- seat availability

Invalid requests and domain conflicts are returned using `ProblemDetail`.

OpenAPI documentation is available through Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Database

PostgreSQL is used as the application database.

The schema is managed with Flyway migrations. Hibernate does not create or modify the schema:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Database constraints and indexes are also used to enforce invariants such as unique emails, idempotency keys and certificate uniqueness.

## Observability

Spring Boot Actuator exposes application health information, including PostgreSQL and RabbitMQ connectivity.

Available endpoints include:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
```

Micrometer counters are registered for:

```text
enrollments.created
payments.confirmed
certificates.issued
```

## Running the application

### Requirements

- Docker
- Docker Compose

The application, PostgreSQL and RabbitMQ can be started together with Docker Compose.

First, define the JWT secret.

PowerShell:

```powershell
$env:JWT_SECRET="<your-jwt-secret>"
```

Then build and start the environment:

```powershell
docker compose up --build
```

Or run it in the background:

```powershell
docker compose up -d --build
```

Once the containers are running:

| Service | URL / Port |
| --- | --- |
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |
| PostgreSQL | localhost:5433 |
| RabbitMQ | localhost:5672 |
| RabbitMQ Management | http://localhost:15672 |

To check the containers:

```powershell
docker compose ps
```

To stop the environment:

```powershell
docker compose down
```

## Running locally

PostgreSQL and RabbitMQ can be started separately:

```bash
docker compose up -d postgres rabbitmq
```

Then configure the JWT secret and run Spring Boot.

PowerShell:

```powershell
$env:JWT_SECRET="<your-jwt-secret>"
.\mvnw.cmd spring-boot:run
```

The default local database configuration uses port `5433`.

## Tests

Run the test suite with:

```powershell
.\mvnw.cmd test
```

Integration tests use Testcontainers for PostgreSQL and RabbitMQ.

The tests cover important parts of the application such as the enrollment lifecycle, concurrent seat reservation and idempotent processing of duplicated events.

## Main design decisions

A modular monolith was chosen because the domain does not require the operational complexity of separate microservices. RabbitMQ still provides asynchronous boundaries where they are useful.

Course capacity is handled at database level with an atomic update because checking capacity in Java before updating it would introduce a race condition.

The transactional outbox keeps database changes and event creation in the same transaction. Consumers are idempotent because message brokers can deliver a message more than once.

DTOs are kept separate from JPA entities so persistence details are not exposed directly through the API.