Below is the **central guidance document** your AI software engineering team should use as the single source of truth for FSCIP’s implementation. It points you to the detailed backlog and the database schema so you always know where to look.

---

## Introduction

This document outlines our **overall approach**, architecture decisions, and project structure for the Financial Services Customer Interaction Portal (FSCIP). Treat this as your mission control:

- **High-Level Solution Approach** explains our chosen tech stack and why.
    
- **Technical Reasoning** drills into each major requirement from a frontend, backend, and integration perspective.
    
- **Project & Codebase Design** shows the repo layout, CI/CD, testing, and coding standards.
    
- **Backlog & Stories** have been moved to the separate **Epic Backlog Document**—refer there for all user stories, subtasks, and definitions of done.
    
- **Database Design** is covered in the **ERD Document**—see that for table diagrams, column definitions, and relationships.
    

Use this doc to understand _how_ we’re building FSCIP; dive into the others to see _what_ to build and _where_ data lives.

---

## 1 High-Level Solution Approach

|Layer|Technology Choice|Why & Key Notes|
|---|---|---|
|**Frontend**|**React 18 + TypeScript** (Vite)|Familiar to most engineers; TS adds safety; Vite speeds up builds.|
|**Backend**|**Spring Boot 3 (Java 17)** – Maven|Meets requirement, mature ecosystem (Security, Data JPA, Cloud).|
|**API Protocols**|REST + JSON; async via Apache Kafka|Keeps external contracts simple; Kafka decouples heavy or delayed workflows.|
|**Data Stores**|PostgreSQL 16; S3 (files); Elasticsearch 8; Redis (cache)|Best-of-breed for relational, object, search, and ephemeral data.|
|**Rule Engine**|Drools 8 in dedicated service module|Hot-reloadable DRL, strong audit features.|
|**Auth / IAM**|Spring Security + Keycloak (OIDC)|Out-of-the-box 2FA, SSO, role management.|
|**CI/CD**|GitHub Actions → Docker → Kubernetes|Preview env per PR; promote from dev → qa → prod via helm.|
|**Observability**|OpenTelemetry, Grafana, Loki, Tempo|End-to-end tracing, metrics & log aggregation to hit SLAs.|

---

## 2 Per-Requirement Technical Reasoning

> Here we break each high-level feature down into frontend, backend, and integration pieces, covering happy paths, errors, caching, retries, and testing approaches.

_(Contents unchanged—please refer to the original reasoning for full detail.)_

---

## 3 Project & Codebase Design

```text
fscip-root/
 ├─ .github/workflows/
 ├─ infra/ (Helm, Terraform)
 ├─ backend/
 │   ├─ pom.xml (parent)
 │   ├─ identity-service/
 │   ├─ account-service/
 │   ├─ rules-service/
 │   ├─ search-service/
 │   ├─ notification-service/
 │   └─ common-lib/
 ├─ frontend/
 │   ├─ package.json (workspace)
 │   ├─ apps/portal/
 │   └─ libs/ui-components/
 └─ docs/ (ADR, OpenAPI, ERD, diagrams)
```

- **Architecture**: MVC in Spring, React component library, shared utils.
    
- **Testing**: JUnit 5, Mockito, REST-assured, Cypress, Gatling, OWASP ZAP.
    
- **Environments**: `local`, `dev`, `qa`, `prod` via Spring profiles & Kubernetes secrets.
    
- **Code Style**: Google Java Format; ESLint/Prettier; commit hooks.
    

---

## 4 Backlog & Stories

All granular epics, user stories, subtasks, and Definitions of Done have been consolidated into the **Epic Backlog Document**.  
Please **do not** search here for individual stories—instead:

1. Open **Epic Backlog Document**.
    
2. Find your epic (A through E) and story ID (e.g., B1-3).
    
3. Follow the “Sub-Tasks” list for FE, BE, and QA steps.
    
4. Refer back here for architectural context and coding guidelines.
    

---

## 5 Validation & Testing Strategy

|Level|Tools|Coverage Target|
|---|---|---|
|Unit|JUnit 5, Mockito, RTL|80 % lines / 100 % critical|
|API|REST-assured, Pact-consumer/provider|Contract test for each endpoint|
|UI|Cypress 12|Critical journeys: registration, apply product, raise ticket|
|Perf|Gatling (10k VU)|Search < 2 s 95th percentile; rule validate < 300 ms|
|Security|OWASP ZAP, dependency-check|Zero high CVEs allowed|

Automated test packs run on every PR; nightly scheduled performance & security tests against **qa** environment.

---

## 6 Exception & Edge-Case Coverage Checklist

- **Partial failure of external services** (e.g., SMS gateway): retry with exponential backoff; move to DLQ after 5 attempts; show “We’ll email you shortly” to user.
    
- **File upload aborted mid-way**: FE cancels; no orphan S3 objects because uploads go to a temp bucket prefix and only commit to DB once complete.
    
- **Duplicate service request within 24 h**: DB unique constraint `(customer_id, type, submitted_date)`.
    
- **Search index lag**: display “Results may be up to 5 min old” banner if Kafka lag > threshold.
    
- **Rules service hot reload fails**: fallback to previous rule-set id; circuit breaker returns 503 to FE with user-friendly banner.
    

---

## 7 On-Boarding Cheatsheet for Junior Engineers

1. **Clone** repository & run `make dev` – spins up Docker compose of PostgreSQL, Keycloak, Kafka, Elasticsearch, local-stack S3.
    
2. **Frontend**: `pnpm install && pnpm dev`.
    
3. **Backend**: `./mvnw -pl identity-service,account-service -am spring-boot:run`.
    
4. **Test**: `pnpm test` (FE) or `mvn test` (BE).
    
5. Open `http://localhost:8080/swagger-ui.html` for live API docs.
    
6. Use seeded admin credentials (`admin@example.com / Admin!234`).
    

---

## 8 Next Steps & Governance

1. **Sprint 0 (1 week)** – spin up repos, infra, CICD skeleton, story pointing.
    
2. **Sprints 1-2** – deliver Epic A (IAM) MVP to QA.
    
3. **Quarterly architecture reviews** – update ADRs, dependency upgrades.
    
4. **Change-control** – any modification to rule DSL or data model requires ADR + security review.
    
5. **Regulatory compliance** – map portal features to ISO 27001, PCI-DSS controls; schedule external audit six weeks pre-launch.

---

> **Database Schema:** For the full entity-relationship design (tables, columns, keys, constraints) that underpins these APIs and stories, see the **ERD Document**.

---

Keep this document bookmarked as your **implementation roadmap**, then drill into:

- **Epic Backlog Document** → detailed build tasks
    
- **ERD Document** → data design and SQL schema

