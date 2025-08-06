## Frontend

* **Tech Stack & Tooling**

  * Use **React 18 + TypeScript** with Vite for fast builds and strong typing.
  * Enforce code style via **ESLint** (Airbnb or custom config) and **Prettier** on save/commit.
  * Adopt a **mono-repo with workspaces**: split into `apps/portal` and `libs/ui-components` for shared UI.

* **Component Architecture**

  * Build **presentational** (dumb) and **container** (stateful) components.
  * Organize by **feature folder**:

    ```
    src/
      features/
        accountOverview/
          components/
          hooks/
          styles/
        secureMessaging/
        …
    ```
  * Use **Tailwind CSS** with design tokens for consistent theming.

* **State Management & Data Fetching**

  * Global state via **Redux Toolkit** or Context where appropriate.
  * Side effects handled through **RTK Query** or **React Query** for caching and automatic retries.
  * Always abort stale requests and show skeleton loaders for pending states.

* **Exception Handling**

  * Define a **central exception class** (e.g., `AppError` extending `Error`) to represent all application errors.
  * Create **custom exception subclasses** for distinct scenarios: `NetworkError`, `ValidationError`, `AuthError`, etc.
  * Use **Error Boundaries** at the application root to catch render-time errors and show fallback UI.
  * Wrap unknown or third-party errors in the central `AppError` to enforce a uniform shape and messaging.

* **Configurable Constants & Environment**

  * Extract all changeable values to **`.env` files** (e.g., `.env.development`, `.env.production`) using Vite’s `import.meta.env` convention.
  * Variables include: API base URLs (`VITE_API_BASE_URL`), feature flags, request timeouts, third-party keys.
  * Maintain a **`.env.example`** with documented sections (API, Features, UI Limits) so new developers can onboard quickly.
  * Avoid hardcoding any values in source; reference `import.meta.env.VITE_…` directly.

* **Accessibility & Internationalization**

  * Enforce **WCAG 2.1 AA**: keyboard navigation, ARIA labels, focus management, color contrast checks.
  * Externalize all UI strings with **react-i18next**; support dynamic locale selection.

* **Testing**

  * **Unit Tests**: Jest + React Testing Library, > 90% coverage on critical components.
  * **Integration/E2E**: Cypress for end-to-end flows.
  * **Visual Regression**: Percy or Cypress snapshot testing on key UI components.

* **Performance & Best Practices**

  * Lazy-load routes and heavy components via **React.lazy** + **Suspense**.
  * Optimize bundle size with code splitting and tree shaking.
  * Use `React.memo`, `useMemo`, `useCallback` judiciously.

---

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

## Database

* **Tech Stack & Versioning**

  * **PostgreSQL 16**; **Flyway** for migrations.

* **Naming Conventions**

  * **snake\_case** for tables/columns; singular names.
  * Logical schemas per domain (identity, account, rules).

* **Schema Design & Partitioning**

  * Normalize to 3NF; JSONB for flexible metadata.
  * Partition large tables (e.g., transactions) by date.

* **Constraints & Indexes**

  * PK, FK, UNIQUE constraints.
  * B-tree for predicates; GIN on JSONB.

* **Transactions & Locking**

  * Default **READ COMMITTED**; use **SERIALIZABLE** sparingly.
  * Keep transactions short.

* **Auditing & History**

  * Audit log table via triggers or application.
  * History tables for compliance.

* **Performance Tuning & DR**

  * Analyze `pg_stat_statements`; use EXPLAIN ANALYZE.
  * Backup: daily pg\_dump + physical snapshots; read replicas for HA; archive cold data.
