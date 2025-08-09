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
