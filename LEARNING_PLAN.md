# 6-week learning plan

A practical path from "I write Laravel and Android Java" to "I can talk about, and demo, a Spring Boot backend." Use this repository as the home ground. Each week lists goals, exercises tied to files here, and free resources.

Time box: about 6–10 hours a week is enough. Type the code, do not just read it. Keep notes in a way you could later paste into a GitHub README or a CV bullet.

---

## Weeks 1–2 — Modern Java 17/21

You already know Java from Android. The language has moved on: records, `var`, streams, `Optional`, switch expressions and virtual threads show up constantly in Spring Boot 3 codebases (and in this project).

### Week 1 — Records, var, streams, collections

**Goals**

- Read and write Java 16+ records confidently.
- Use `var` for local variables without losing track of types.
- Rebuild the collection pipelines you would write in PHP (`array_filter`, `array_map`, `collect()`) with the Stream API.
- Be fluent with `List`, `Map`, `Set` and common collectors.

**Exercises (tied to this repo)**

1. Open `src/test/java/com/example/taskapi/learn/Week01RecordsStreamsCollectionsTest.java` and run just that class:

   ```bash
   ./mvnw test -Dtest=Week01RecordsStreamsCollectionsTest
   ```

2. Break `recordsGiveYouAccessorsEqualsAndToString` on purpose (change a title) and look at the failure. Then read `src/main/java/com/example/taskapi/dto/TaskResponse.java` and `TaskRequest.java` — these are the same idea in production code.

3. In `TaskService.listMyTasks()` (`src/main/java/com/example/taskapi/service/TaskService.java`) the list is built with `.stream().map(TaskResponse::from).toList()`. Rewrite that pipeline on paper as Laravel-style `collect()`, then add a new method `listOpenTitles()` that returns only `TODO` titles, using a stream. Cover it in `TaskServiceTest`.

4. Add a grouping exercise of your own to the week 1 test: count tasks per `TaskStatus` (the file already has one example — write a second, e.g. a `Map<Long, List<String>>` of owner id to titles).

**Resources**

- [Dev.java — Records](https://dev.java/learn/records/)
- [Dev.java — New Java features](https://dev.java/learn/) (browse "First Steps" and "API & Language")
- [Baeldung — The Java Stream API](https://www.baeldung.com/java-8-streams)
- [Baeldung — Guide to Java 10 var](https://www.baeldung.com/java-10-local-variable-type-inference)

### Week 2 — Optional, switch expressions, virtual threads

**Goals**

- Return and consume `Optional` instead of nullable values, and know when *not* to wrap everything.
- Write exhaustive switch expressions over enums.
- Explain what a virtual thread is, and where this app turns them on.

**Exercises**

1. Run `src/test/java/com/example/taskapi/learn/Week02OptionalSwitchVirtualThreadsTest.java`:

   ```bash
   ./mvnw test -Dtest=Week02OptionalSwitchVirtualThreadsTest
   ```

2. Follow `Optional` through the real app: `UserRepository.findByEmail` and `TaskRepository.findByIdAndOwnerId` return `Optional`. See how `TaskService.requireOwnedTask` uses `orElseThrow`. Add a test in `TaskServiceTest` if you want more practice with the "missing row" path (one already exists).

3. Extend `labelFor` in the week 2 test, or add a `TaskStatus#label()` method on the enum (`src/main/java/com/example/taskapi/domain/TaskStatus.java`) implemented as a switch expression. If you add a new enum constant, the compiler will force you to update the switch — that is the point.

4. Read `spring.threads.virtual.enabled` in `src/main/resources/application.yml`. Start the app (week 3 is fine) and confirm in the logs that virtual threads are enabled. Skim the virtual-thread test so you have seen `Executors.newVirtualThreadPerTaskExecutor()`.

**Resources**

- [Dev.java — Optional](https://dev.java/learn/api/utilities/optional/)
- [Dev.java — Switch](https://dev.java/learn/language/switch/)
- [Dev.java — Virtual threads](https://dev.java/learn/virtual-threads/)
- [Baeldung — Optional](https://www.baeldung.com/java-optional)
- [Baeldung — Java 21 virtual threads](https://www.baeldung.com/java-21-virtual-threads)
- [Spring Boot docs — Virtual threads](https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.virtual-threads)

---

## Weeks 3–4 — Spring Boot REST, JPA, security and tests

This is the core of a Java backend role. Use this project as both a map and a thing you modify.

### Week 3 — REST, JPA, Flyway, validation, error handling

**Goals**

- Explain the request path: controller → service → repository → PostgreSQL.
- Change a migration and an entity together, and know why `ddl-auto=validate` is set.
- Add a validated field and see the 400 body from `GlobalExceptionHandler`.

**Exercises**

1. Start Postgres and the app (see the README). Walk the register + task curl examples. Read, in this order:
   - `TaskApiApplication.java`
   - `web/TaskController.java`
   - `service/TaskService.java`
   - `repository/TaskRepository.java`
   - `domain/Task.java`
   - `db/migration/V2__create_tasks.sql`
   - `web/GlobalExceptionHandler.java`

2. Add a `dueDate` (optional `LocalDate`) to tasks:
   - new Flyway file `V3__add_tasks_due_date.sql`
   - field + getter/setter on `Task`
   - field on `TaskRequest` / `TaskResponse`
   - a `@FutureOrPresent` (or similar) constraint
   - unit and MockMvc coverage

   If you skip the migration and only add the field, the app will fail at startup because Hibernate's `validate` mode notices the missing column. That failure is worth seeing once.

3. Trigger validation on purpose (`POST /api/tasks` with an empty title) and read the JSON. Then read how `MethodArgumentNotValidException` is handled.

4. Compare each step with the Laravel column in the README mapping table.

**Resources**

- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa)
- [Baeldung — Spring Bean Validation](https://www.baeldung.com/spring-boot-bean-validation)
- [Baeldung — @ControllerAdvice](https://www.baeldung.com/exception-handling-for-rest-with-spring)
- [Baeldung — Flyway](https://www.baeldung.com/database-migrations-with-flyway)
- [Spring Data JPA docs](https://spring.io/projects/spring-data-jpa)

### Week 4 — JWT security and tests

**Goals**

- Describe the login/register flow and how a request is authenticated afterwards.
- Write a Mockito unit test and a MockMvc test without copy-pasting blindly.
- Run the Testcontainers integration test if you have Docker.

**Exercises**

1. Read `config/SecurityConfig.java`, `security/JwtService.java`, `security/JwtAuthenticationFilter.java`, `security/AppUserDetailsService.java` and `service/AuthService.java`. Sketch the sequence for `POST /api/auth/login` and for `GET /api/tasks` with a `Bearer` token.

2. Run the tests you will be copying from in interviews:

   ```bash
   ./mvnw test -Dtest=TaskServiceTest,AuthControllerTest,TaskControllerTest
   ```

   Then add one new behaviour (for example: updating a missing task throws `ResourceNotFoundException`, or register rejects a short password with 400) and cover it at the service or MockMvc layer.

3. If Docker is running, execute `TaskApiIntegrationTest`. Read how `@ServiceConnection` injects the Postgres container and how the test proves that Bob cannot see Alice's task.

   ```bash
   ./mvnw test -Dtest=TaskApiIntegrationTest
   ```

4. Call `/api/tasks` without a token and with a junk token; you should get 401. Find where that status is written (`SecurityConfig`'s authentication entry point, not the `@RestControllerAdvice` — filters run earlier).

**Resources**

- [Spring Security official docs](https://spring.io/projects/spring-security)
- [Baeldung — JWT with Spring Security](https://www.baeldung.com/spring-security-oauth-jwt) and [JWT + Spring Boot 3](https://www.baeldung.com/spring-boot-3-security-oauth)
- [Baeldung — @WebMvcTest / MockMvc](https://www.baeldung.com/spring-boot-testing)
- [Baeldung — Mockito](https://www.baeldung.com/mockito-series)
- [Baeldung — Testcontainers in Spring Boot](https://www.baeldung.com/spring-boot-testcontainers)
- [Spring guide — Testing the Web Layer](https://spring.io/guides/gs/testing-web)

---

## Weeks 5–6 — Kafka, Docker, Actuator, CV polish

### Week 5 — Kafka and Docker

**Goals**

- Publish a domain event when a task is created, and consume it.
- Run the app with Compose, and be able to explain the multi-stage `Dockerfile`.
- Know why `TaskEventPublisher` is an interface (testing, and running without Kafka).

**Exercises**

1. Read `service/TaskEventPublisher.java`, `service/NoOpTaskEventPublisher.java`, `kafka/KafkaTaskEventPublisher.java` and `kafka/TaskCreatedConsumer.java`. Note the `@ConditionalOnProperty(name = "app.kafka.enabled")` switch.

2. Start Postgres + Kafka and run with the flag on:

   ```bash
   docker compose up -d postgres kafka
   APP_KAFKA_ENABLED=true ./mvnw spring-boot:run
   ```

   Create a task and confirm both the publish and consume log lines. Then read `docker-compose.yml` and be able to explain the two Kafka listeners (`kafka:19092` inside Compose, `localhost:9092` on your machine).

3. Add a second event, `TaskCompleted`, published when a task's status becomes `DONE`. Add a consumer that logs it. Keep it behind the same `app.kafka.enabled` flag.

4. Build and run with Compose end-to-end:

   ```bash
   docker compose up --build
   ```

   Walk through the `Dockerfile`: stage 1 compiles with the JDK, stage 2 copies `task-api.jar` into a JRE image and runs as a non-root user.

**Resources**

- [Spring Kafka reference](https://docs.spring.io/spring-kafka/reference/html/)
- [Baeldung — Spring Kafka](https://www.baeldung.com/spring-kafka)
- [Spring Boot with Docker guide](https://spring.io/guides/gs/spring-boot-docker)
- [Baeldung — Dockerizing a Spring Boot app](https://www.baeldung.com/dockerizing-spring-boot-application)
- [Apache Kafka KRaft overview](https://kafka.apache.org/documentation/#kraft)

### Week 6 — Actuator, logging, GitHub / CV polish

**Goals**

- Use Actuator as a talking point (health, metrics, info).
- Clean the README as if this were a public repo.
- Be ready to walk a hiring manager through the architecture in five minutes.

**Exercises**

1. With the app running, call `/actuator/health` (public) and `/actuator/metrics` (needs a JWT). Read the Actuator block in `application.yml`. Add one custom item to `/actuator/info` (for example the learning-plan week you are on) via `info.*` properties or an `InfoContributor` bean.

2. Find every `log.info` / `log.debug` / `log.error` in the services and the Kafka consumer. Change `logging.level.com.example.taskapi` in `application-dev.yml` and watch the difference. This is SLF4J + Logback, which Spring Boot configures for you.

3. Polish for GitHub:
   - Make sure `./mvnw test` still passes.
   - Put a real JWT secret in an environment variable, not in the file, if you deploy anywhere.
   - Write a short "What I built" paragraph in your own words at the top of the README.
   - Push the repo (no `target/`, no secrets — `.gitignore` already covers the build).

4. CV / interview talking points you can now back with this repo (phrase them in your own words):
   - Designed a Spring Boot 3 REST API with JWT auth, JPA and Flyway.
   - Covered it with unit, MockMvc and Testcontainers tests.
   - Published domain events with Spring Kafka and packaged the stack with Docker Compose.
   - You can also mention the Kafka work you have done in production, and how this project connects to it.

   Do not invent tools you did not use. If a job asks for Spring specifically, this repository is the evidence.

**Resources**

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html)
- [Baeldung — Spring Boot Actuator](https://www.baeldung.com/spring-boot-actuators)
- [Baeldung — Logging in Spring Boot](https://www.baeldung.com/spring-boot-logging)
- [Spring Boot reference](https://docs.spring.io/spring-boot/reference/) — skim "Core Features" and "I/O"

---

## If you get stuck

- Official starting points: [spring.io/guides](https://spring.io/guides), [dev.java/learn](https://dev.java/learn/), [baeldung.com](https://www.baeldung.com/).
- Prefer reading the production class next to a failing test over starting a new tutorial from scratch.
- When an exception is confusing, search for the *exception class name* plus "Spring Boot 3" on Baeldung or the official docs.

When you finish week 6, you should be able to start this API, walk through a request, point at the tests, and explain how you would add a field, an endpoint, or an event — which is exactly what a backend Java interview is trying to find out.
