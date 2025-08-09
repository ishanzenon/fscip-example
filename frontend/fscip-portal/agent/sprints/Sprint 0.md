## Sprint 0 – Foundation Setup (1 week)

1. **Backend Repository & Module Skeleton**

   * Initialize monorepo with core modules (`identity`, `account`, `common-lib`, etc.), Maven parent POM and basic directory layout.

2. **Database Schema & ERD Migrations**

   * Translate the ERD into SQL `CREATE TABLE` scripts for core entities (`users`, `accounts`, `transactions`, `otp_codes`, etc.) and version them with Flyway or Liquibase .

3. **Spring Boot App Skeleton & Security Stub**

   * Add `application.properties` with DB connection settings.
   * Wire in a bare-bones Spring Security filter chain with pre-auth stub (JWT stubbed), ready to extend for OTP and SSO later.

4. **React + TypeScript Shell**

   * Scaffold a Vite-based React app.
   * Configure React-Router for routing, global state (Context or Redux Toolkit), and an Axios/fetch client that can store JWTs and handle interceptors.

5. **OTP-Based Email Authentication Prototype**

   * Stub endpoints for `/auth/otp/request` and `/auth/otp/verify` using in-memory storage.
   * Integrate a simple email provider mock so the “OTP sent” flow can be demoed.

6. **Local Dev Environment via Docker Compose**

   * Compose services: PostgreSQL, Keycloak (or JWT stub), Kafka stub (e.g., Redpanda), Elasticsearch, MinIO for S3 .
   * Provide `make dev` or `pnpm dev` scripts to launch FE and BE against this stack.

7. **Backlog & Story-Pointing Setup**

   * Populate Epics A–E in Jira (or Azure DevOps) with high-level stories.
   * Define Sprint 0 tasks as above and assign rough story points, so we can start sprint planning immediately.

8. **Developer Onboarding Documentation**

   * README covering clone → dev environment → run instructions.
   * Link to key docs: Implementation Roadmap, Epic Backlog, ERD.

9. **CI/CD Milestone Plan (Deferred)**

   * Record a placeholder in our roadmap: full GitHub Actions + Helm deploy goes in Epic 2.
   * No actual CI workflow in Sprint 0—this keeps focus on demo-ready foundations.

