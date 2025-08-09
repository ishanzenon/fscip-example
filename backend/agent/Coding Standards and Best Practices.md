## Backend

* **Tech Stack & Project Structure**

  * **Spring Boot 3 + Java 17** with Maven multi-module layout: identity, account, rules, search, notification, document, common-lib.
  * Standardize code style with **Google Java Format** + Spotless plugin.

* **Layered Architecture**

  * Controller → Service → Repository.
  * Use **MapStruct** for DTO ↔ entity mapping; keep controllers thin.

* **Exception Handling & API Errors**

  * Define a **central exception base class** `BaseException` (extends `RuntimeException`).
  * Implement **custom exceptions** like `ResourceNotFoundException`, `ValidationException`, `AuthException`, `ServiceException`.
  * Create a **`GlobalExceptionHandler`** annotated with `@ControllerAdvice` to catch:

    * `BaseException` and map to its defined HTTP status.
    * `Exception` (all other/unknown) and return 500 Internal Server Error.

* **HTTP Codes & Error Responses**

  * Map each custom exception to a REST-appropriate status: 400 (validation), 401 (auth), 403 (authorization), 404 (not found), 409 (conflict), 500 (server error).
  * Standardize on an `ErrorResponse` DTO containing: `timestamp`, `status`, `error`, `message`, `path`.
  * Provide clear, non-sensitive `message` texts; avoid internal details or stack traces.

* **Configurable Constants & Properties**

  * Extract all mutable values to **`application.properties`** or **`application.yml`**:

    * Server port, context path, CORS origins.
    * External service base URLs and timeouts.
    * Exception messages (keyed under `messages.*`).
    * Database credentials, feature toggles, JWT secrets.
  * Structure properties file with **commented sections**: `# Server`, `# Datasource`, `# Security`, `# Messages`.
  * Bind to POJOs via `@ConfigurationProperties` or inject via `@Value`.
  * Keep the codebase free of hardcoded literals; reference constants from configuration.

* **API Design & Contracts**

  * Define all REST endpoints with **OpenAPI/Swagger**; auto-generate client stubs.
  * Enforce **contract tests** (Pact) for service-to-service interactions.

* **Security & Performance**

  * Use **Spring Security + Keycloak** for OIDC auth; secure endpoints via roles and scopes.
  * Rate-limit critical endpoints with **Bucket4j**.
  * Instrument with **OpenTelemetry**; expose metrics on `/actuator/prometheus`.

* **Testing & CI/CD**

  * Unit: JUnit 5 + Mockito (≥ 80% coverage).
  * Integration: Spring Boot Test with Testcontainers.
  * Contract Tests: Pact provider and consumer.
  * Scan with OWASP ZAP and load-test with Gatling in CI.
  * Pipeline: GitHub Actions per module → build, test, Docker, push, Helm deploy.

---
