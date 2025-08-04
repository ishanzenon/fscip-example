## Executive Summary

The Financial Services Customer Interaction Portal (FSCIP) is designed as a cloud-native platform that begins with a **modular monolith** architecture today, with a clear, incremental path to a **microservices** landscape as scale and complexity grow. This approach balances early-stage simplicity and rapid delivery with a robust evolution strategy for long-term scalability and team autonomy.

---

## System Context

FSCIP serves both external users (Customers) and internal roles (Support Agents, Relationship Managers, Auditors, Administrators) and integrates with:

- **Identity & Access**: Keycloak (OIDC) for SSO, 2FA, and role management
    
- **Core Banking System**: Downstream account and transaction data via synchronous APIs and Kafka events
    
- **KYC Vendor**: External ID verification for uploaded documents
    
- **Notification Providers**: SMS/Email gateways for OTPs, alerts, and notifications
    
- **Data Stores**:
    
    - PostgreSQL 16 for transactional data and relational consistency
        
    - Redis for caching hot data (e.g., session tokens, rule-validation results)
        
    - Elasticsearch 8 for global and advanced search capabilities
        
    - S3 for document storage (statements, KYC docs, PDFs)
        
- **Messaging**: Apache Kafka for asynchronous decoupling of heavy workflows (e.g., audit logging, document indexing)
    
- **Observability**: OpenTelemetry, Grafana, Loki, and Tempo for metrics, logs, and tracing
    

---

## Initial Deployment Approach: Modular Monolith

### Rationale

- **Rapid Delivery & Simplicity**: A single deployable artifact (uber-jar/container) accelerates the build-test-deploy cycle.
    
- **Low Operational Overhead**: Service discovery, distributed tracing, and inter-service auth are deferred until later.
    
- **Strong Consistency**: Single ACID transactions span all modules, simplifying data integrity.
    
- **Team Alignment**: Early-stage teams can develop cross-functional features within a shared codebase, reducing context-switching.
    

### Structure

- **Multi-Module Layout** (Maven):
    
    - `identity-module`
        
    - `account-module`
        
    - `notification-module`
        
    - `document-module`
        
    - `rules-module`
        
    - `search-module`
        
    - `common-lib`
        
- **Single Spring Boot Application**: Exposes all APIs via a unified API Gateway internal router.
    
- **Shared Database**: One PostgreSQL schema, with clear table prefixes per module.
    

---

## Evolution Strategy: Strangler-Fig to Microservices

1. **Pilot Extraction**: Begin by carving out a low-risk domain (e.g., Search or Notifications) into an independent service.
    
    - Deploy side-by-side with monolith behind the same API Gateway.
        
    - Implement consumer-driven contracts (Pact) to guard API stability.
        
2. **Incremental Migration**: Use feature flags to route calls to new services or the monolith.
    
    - Dual-write transitional logic: data written to both monolith DB and new service DB.
        
    - Data migration scripts for zero-downtime table splits.
        
3. **Independent CI/CD**: Scaffold separate pipelines per service (build, test, Docker, deploy).
    
    - Maintain a monorepo with per-service directories and shared tooling templates.
        
4. **Distributed Patterns**: Adopt sagas or event-driven compensations for cross-service transactions.
    
    - Introduce Resilience4j for circuit breakers and retries.
        
5. **Full Microservices**: Over time, continue extracting modules until each bounded context is isolated, owning its own schema, service, and pipeline.
    

---

## Component Diagrams & Module Breakdown

_(Unchanged from prior document; each module described with integrations and tech stack.)_

---

## Data Model Overview

_(Central ERD remains the foundation; services will eventually migrate to their own schemas.)_

---

## Integration Flows

_(Monolith initially handles all flows; evolving to distributed calls via API Gateway.)_

---

## Non-Functional Requirements

_(Performance, Security, Observability apply across both phases.)_

---

## CI/CD Strategy

- **Monolith Pipeline**: Single GitHub Actions workflow for build → test → deploy.
    
- **Service Pipelines**: Template-based workflows introduced as each module is extracted.
    
- **Environment Parity**: All services and monolith run on the same Kubernetes cluster with Helm charts.
    

---

_This blended approach ensures high initial velocity and low risk, with a clear, practiced path to microservices as FSCIP scales._