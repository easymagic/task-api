# task-api

Companion starter for the book **Spring Boot for Laravel Developers: A Friendly Crash Course in Java Backend Development** by Nnamdi Alexander Akamu.

A small Spring Boot 3.5 (Java 21) REST API for learning backend Java, aimed at someone who already writes PHP/Laravel and has used Java on Android.

The domain is deliberately simple: users can register, log in, and manage their own tasks. Around that you get the pieces a typical Java backend role expects — JPA, Flyway, Bean Validation, JWT security, Kafka events, Actuator, tests and Docker.

```bash
git clone https://github.com/easymagic/task-api.git
cd task-api
```

## How this repo maps to the book

The book is built around this one project. Each chapter points at real classes here, then asks you to change or run something. Work on `main` — that is the finished starter the book walks through.

| Chapter | What to open in this repo |
| --- | --- |
| 1. Modern Java essentials | Learning tests in `src/test/java/com/example/taskapi/learn/`; records in `dto/`; virtual threads in `application.yml` |
| 2. Maven and project structure | `pom.xml`, the `./mvnw` wrapper, and the `src/main/java/com/example/taskapi/` package layout |
| 3. Spring core | `TaskApiApplication`, constructor injection in the services, `application.yml` and the `dev` / `test` profiles |
| 4. REST, DTOs, validation and errors | `web/AuthController`, `web/TaskController`, `dto/`, `web/GlobalExceptionHandler` |
| 5. JPA, Hibernate and Flyway | `domain/`, `repository/`, `src/main/resources/db/migration/` |
| 6. Spring Security with JWT | `config/SecurityConfig`, `security/` |
| 7. Testing | `src/test/java` — unit (`TaskServiceTest`), MockMvc (`*ControllerTest`), Testcontainers (`TaskApiIntegrationTest`) |
| 8. Kafka with Spring Kafka | `kafka/`, `service/TaskEventPublisher`, Kafka settings in `application.yml` and `docker-compose.yml` |
| 9. Docker, Actuator and logging | `Dockerfile`, `docker-compose.yml`, Actuator settings in `application.yml` |
| 10. Interview questions | In the book; the answers point back at the classes above |
| 11. Cheat sheet | In the book; this README's Laravel map covers the same ground |

A longer six-week study path, with extra exercises, is in [LEARNING_PLAN.md](LEARNING_PLAN.md).

## What is in the box

| Piece | Where to look |
| --- | --- |
| REST CRUD for `Task` (owned by a `User`) | `TaskController`, `TaskService` |
| Spring Data JPA + PostgreSQL | `domain/`, `repository/` |
| Flyway migrations | `src/main/resources/db/migration/` |
| Bean Validation (`@Valid`) | `dto/RegisterRequest`, `dto/TaskRequest` |
| Global error handling | `web/GlobalExceptionHandler` |
| Spring Security + JWT | `config/SecurityConfig`, `security/` |
| Kafka `TaskCreated` event + logging consumer | `kafka/` (off by default) |
| Actuator + SLF4J | `/actuator/health`, service `log.info` calls |
| Profiles | `application.yml`, `application-dev.yml`, `application-test.yml` |
| Unit, MockMvc and Testcontainers tests | `src/test/java` |
| Docker Compose (Postgres + Kafka KRaft + app) | `docker-compose.yml`, `Dockerfile` |

## Prerequisites

- JDK 21
- Maven 3.9+ (or just use the `./mvnw` wrapper in this folder)
- Docker, if you want Postgres / Kafka / the Testcontainers test

## How to run

### 1. Start PostgreSQL

```bash
docker compose up -d postgres
```

### 2. Run the API (Kafka off — this is the week 3–4 setup)

```bash
./mvnw spring-boot:run
```

The default profile is `dev`. The API listens on [http://localhost:8080](http://localhost:8080). Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health).

### 3. Week 5: also start Kafka and turn the producer on

```bash
docker compose up -d postgres kafka
APP_KAFKA_ENABLED=true ./mvnw spring-boot:run
```

When a task is created you should see a `Published TaskCreated` log and then a `Consumed TaskCreated event` log.

### 4. Or run the whole stack in Docker

```bash
docker compose up --build
```

The Compose file starts Postgres, a single-node Kafka in KRaft mode (no ZooKeeper) and the app. Inside the network the app talks to `kafka:19092`; on your machine the advertised listener is `localhost:9092`.

## Tests

```bash
# Unit tests, MockMvc tests, and the week 1–2 Java 21 exercises.
# The Testcontainers test is skipped cleanly if Docker is not available.
# You may see a Testcontainers ERROR log about a missing Docker socket;
# that is the skip check, not a failed test. The summary will say Skipped: 3.
./mvnw test

# Compile only
./mvnw -q compile

# Run just the Postgres integration test (needs Docker)
./mvnw test -Dtest=TaskApiIntegrationTest
```

## Endpoints, with curl

### Register

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"secret123","name":"Alice"}'
```

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "email": "alice@example.com",
  "name": "Alice"
}
```

### Login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"secret123"}'
```

Save the token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"secret123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
```

### Create a task

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Learn Spring Boot","description":"Build the starter API"}'
```

`status` is optional and defaults to `TODO`. Allowed values: `TODO`, `IN_PROGRESS`, `DONE`.

### List my tasks

```bash
curl -s http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN"
```

### Get one task

```bash
curl -s http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Update a task

```bash
curl -s -X PUT http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Learn Spring Boot","description":"Halfway","status":"IN_PROGRESS"}'
```

### Delete a task

```bash
curl -s -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
```

Returns HTTP 204 with an empty body.

### Health

```bash
curl -s http://localhost:8080/actuator/health
```

`/actuator/metrics` and `/actuator/info` need a JWT, because everything except `/api/auth/**` and `/actuator/health` is authenticated.

You only ever see your own tasks. A request for someone else's id returns 404, not 403, so the API does not reveal that the row exists.

## Laravel → Spring concept map

| Laravel | Spring Boot (this project) |
| --- | --- |
| Service container / service providers | Dependency injection. Annotate a class with `@Component`, `@Service`, `@Repository` or `@RestController`, inject collaborators through the constructor. Start at `TaskApiApplication`. |
| `routes/api.php` + controllers | `@RestController` + `@RequestMapping` / `@GetMapping` / `@PostMapping`. See `AuthController` and `TaskController`. |
| Eloquent models | JPA entities (`User`, `Task`) + Spring Data repositories (`UserRepository`, `TaskRepository`). Method names become queries. |
| `database/migrations` | Flyway SQL in `src/main/resources/db/migration/`. `spring.jpa.hibernate.ddl-auto=validate` means Hibernate does not change the schema. |
| Form Requests | Bean Validation annotations on DTO records, triggered by `@Valid` on the controller parameter. |
| `App\Exceptions\Handler` | `@RestControllerAdvice` in `GlobalExceptionHandler` (`@ControllerAdvice` + `@ResponseBody`). |
| Sanctum / Passport (API tokens) | Spring Security + JWT: `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`. |
| `.env` + `config/*.php` | `application.yml` plus profile files. Override with env vars such as `SPRING_DATASOURCE_URL` or `APP_JWT_SECRET` (relaxed binding). |
| `Hash::make` / bcrypt | `PasswordEncoder` bean (`BCryptPasswordEncoder`) in `SecurityConfig`. |
| `auth()->user()` | `CurrentUserService`, which reads `SecurityContextHolder`. |
| `DB::transaction` | `@Transactional` on service methods. |
| Events / jobs / queues | `TaskEventPublisher` + Spring Kafka (`KafkaTaskEventPublisher`, `TaskCreatedConsumer`). |
| PHPUnit + HTTP tests | JUnit 5 + Mockito (`TaskServiceTest`), MockMvc (`*ControllerTest`), Testcontainers (`TaskApiIntegrationTest`). |
| `composer.json` / Artisan | `pom.xml` / `./mvnw spring-boot:run`, `./mvnw test`. |

Code comments in the classes above repeat these analogies next to the real code.

## Suggested path

If you have the book, follow it chapter by chapter using the table above. If you want a longer self-study schedule, follow [LEARNING_PLAN.md](LEARNING_PLAN.md). Weeks 1–2 are modern Java, using the learning tests under `src/test/java/com/example/taskapi/learn/`. Weeks 3–4 work through this API (REST, JPA, security, tests). Weeks 5–6 add Kafka, Docker, Actuator and polish.

## License

This project is released under the [MIT License](LICENSE). Copyright (c) 2026 Nnamdi Alexander Akamu.

## Configuration cheatsheet

| Setting | Default | Env override |
| --- | --- | --- |
| HTTP port | `8080` | `SERVER_PORT` |
| Datasource URL | `jdbc:postgresql://localhost:5432/taskdb` | `SPRING_DATASOURCE_URL` |
| DB user / password | `taskuser` / `taskpass` | `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` |
| JWT secret | learning default in `application.yml` | `APP_JWT_SECRET` (must be ≥ 32 bytes) |
| JWT lifetime | 86400000 ms (1 day) | `APP_JWT_EXPIRATION_MS` |
| Kafka enabled | `false` | `APP_KAFKA_ENABLED=true` |
| Kafka bootstrap | `localhost:9092` | `SPRING_KAFKA_BOOTSTRAP_SERVERS` |
| Active profile | `dev` | `SPRING_PROFILES_ACTIVE` |

This is a learning project, not production configuration. Change the JWT secret before you deploy anything, and do not commit real secrets.

## Free references

- [Spring Boot official guides](https://spring.io/guides) — start with [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service) and [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa)
- [Dev.java — Learn Java](https://dev.java/learn/)
- [Baeldung](https://www.baeldung.com/)
