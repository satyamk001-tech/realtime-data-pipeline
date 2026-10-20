# DataFlow Architecture Decision Records (ADRs)

This document formalizes the architectural decisions made in **DataFlow**, detailing the problem context, selected solution, alternatives considered, trade-offs, failure modes, testing strategy, and scaling vectors.

---

## ADR 001: Java 21 & Apache Spark Java API for Core Data Processing

- **Status**: Accepted
- **Context**: The platform requires a robust data processing engine capable of processing millions of restaurant and review records with schema validation, type safety, and analytical aggregations.
- **Decision**: Use **Java 21** as the primary programming language and utilize **Apache Spark's Java API (`org.apache.spark.sql.*`)** for all distributed batch and streaming computations.
- **Alternatives Considered**:
  1. *Pure Java Streams/Collections*: Memory bound, single-JVM limitation, lacks disk spillover, no optimizer for relational joins.
  2. *PySpark*: Python is common in data science, but introduces an unneeded JVM-Python IPC bridge (Py4J socket overhead), complicates unified Maven builds, and fragments the enterprise backend stack.
  3. *Scala*: Spark's native language, but creates multi-language overhead when the team's primary production stack is Java/Spring Boot.
- **Trade-offs**: Java's verbosity compared to Python or Scala requires explicit schema declarations (`StructType`, `Row`, `Encoders`) and lambda serialization awareness (`Serializable`).
- **Failure Modes**: Driver `OutOfMemoryError` on large data collection, Spark executor shuffle fetch failures, class serialization mismatch.
- **Testing Strategy**: In-memory unit tests using Spark local mode (`local[*]`) with JUnit 5, asserting on DataFrame counts, quarantined record partitions, and schema types.
- **Scaling Vector**: Transition from local execution mode (`local[*]`) to a distributed cluster (Spark on Kubernetes or EMR/Databricks) without altering core processing code.

---

## ADR 002: Dual-Store Persistence Strategy (PostgreSQL + Snowflake)

- **Status**: Accepted
- **Context**: The application handles two distinct data access patterns: (1) transactional CRUD for user profiles, pipeline execution tickets, and vector search; (2) large-scale OLAP analytical queries across millions of historical restaurant metrics.
- **Decision**: Implement a **Dual-Store Strategy**:
  - **PostgreSQL (+ pgvector)**: Acts as the operational metadata store (OLTP), managing job states, pipeline runs, audit trails, and vector embeddings for RAG.
  - **Snowflake**: Acts as the enterprise analytical data warehouse (OLAP), hosting raw staging, cleaned dimensions, and gold analytical marts.
- **Alternatives Considered**:
  1. *PostgreSQL for everything*: Simple single database, but column-wide aggregations over millions of rows saturate disk I/O and degrade transactional API performance.
  2. *Snowflake for everything*: Excellent for analytics, but unsuitable for low-latency row-level app state, user sessions, or sub-50ms REST API queries.
- **Trade-offs**: Requires synchronization/ETL coordination between the data lake and Snowflake, and managing two distinct connection pools.
- **Failure Modes**: Snowflake warehouse suspended or credential expiry; network partition during batch load. Handled by local fallback staging in PostgreSQL or parquet inspection.
- **Testing Strategy**: Testcontainers for PostgreSQL in integration tests; mock Snowflake JDBC connections or staging parquet verification for warehouse integration tests.
- **Scaling Vector**: Snowflake compute scales horizontally with multi-cluster warehouses; PostgreSQL scales with read-replicas and connection pooling (HikariCP).

---

## ADR 003: MinIO S3-Compatible Object Store with Medallion Zones

- **Status**: Accepted
- **Context**: Ingestion needs an immutable, distributed storage layer before triggering batch ETL. Directly writing raw data to relational tables couples ingestion to database schemas.
- **Decision**: Use **MinIO** as an S3-compatible local object store structured into three Medallion zones:
  - `raw/restaurants/ingestion_date=YYYY-MM-DD/run_id=<uuid>/`: Original uploaded CSV/JSON payloads.
  - `processed/restaurants/ingestion_date=YYYY-MM-DD/`: Validated, typed, Snappy-compressed Parquet files.
  - `quarantine/restaurants/ingestion_date=YYYY-MM-DD/`: Rejected records with failure codes and original payloads.
- **Alternatives Considered**:
  1. *Local File System (`/var/data`)*: Does not provide an S3 API, making migration to AWS S3, Google Cloud Storage, or Azure Blob Storage require code rewrites.
  2. *Direct database staging table*: Causes heavy database bloat and schema lock contention during large batch ingestion.
- **Trade-offs**: Requires running a MinIO service locally during development (handled seamlessly via Docker or local executable).
- **Failure Modes**: S3 endpoint connection timeout; insufficient bucket quota; duplicate file upload conflicts.
- **Testing Strategy**: Mock S3 client or MinIO Docker container in CI/CD pipeline, verifying file presence, checksum, and metadata headers.
- **Scaling Vector**: Switch endpoint configuration from `http://localhost:9000` to `s3.amazonaws.com` in `.env` without altering application code.

---

## ADR 004: Decoupled Asynchronous Pipeline Execution

- **Status**: Accepted
- **Context**: Triggering a batch ingestion or Spark ETL job over millions of rows takes minutes. Synchronous REST calls (`POST /api/v1/pipelines/run`) would cause HTTP gateway timeouts (504 Gateway Timeout).
- **Decision**: Implement **Decoupled Asynchronous Execution**:
  - `POST /api/v1/pipelines/run` validates the request, generates a unique `run_id`, inserts a `PENDING` job ticket in PostgreSQL, and enqueues the job asynchronously via a Spring `@Async` managed thread pool or worker queue.
  - Returns `202 Accepted` immediately with the `run_id` and tracking status URL.
  - Frontend polls `GET /api/v1/pipelines/{run_id}` or receives real-time WebSocket status updates.
- **Alternatives Considered**:
  1. *Synchronous blocking HTTP*: Fails on any dataset larger than a few thousand rows due to client-side or reverse-proxy timeouts.
  2. *Fire-and-forget without persistence*: Client cannot track whether the job succeeded or failed, making debugging impossible.
- **Trade-offs**: Client requires polling or WebSocket support to track completion.
- **Failure Modes**: Worker node crash midway through processing. Handled by a heartbeat daemon that flags orphaned `RUNNING` jobs as `FAILED` upon timeout.
- **Testing Strategy**: Unit tests verify immediate `202 Accepted` return; integration tests assert transition across `PENDING -> RUNNING -> SUCCEEDED`.

---

## ADR 005: Defense-in-Depth Text-to-SQL Security Engine

- **Status**: Accepted
- **Context**: Allowing natural-language querying of analytical data introduces severe SQL Injection (SQLi), denial-of-service (runaway queries), and data exfiltration risks if generated SQL is blindly executed.
- **Decision**: Implement a multi-layered **Defense-in-Depth Text-to-SQL Security Engine**:
  1. *Prompt Schema Sandboxing*: LLM is given only read-only view schemas (`ZOMATO.MARTS`). No system tables, credentials, or transactional tables are exposed.
  2. *Abstract Syntax Tree (AST) Validation*: Candidate SQL is parsed using a rigorous SQL parser (JSqlParser). Reject any statement containing `INSERT`, `UPDATE`, `DELETE`, `DROP`, `ALTER`, `TRUNCATE`, `EXEC`, `CREATE`, or comment delimiters (`--`, `/*`).
  3. *Statement Count Enforcement*: Strictly permit exactly one statement per request; reject any query containing semicolons `;`.
  4. *Row Limit & Timeout Clamping*: Automatically enforce `LIMIT 100` if not present, and execute with a 5-second database query timeout.
  5. *Least-Privilege Database Role*: The backend executes the query using a dedicated database role granted `SELECT` only on the analytical marts schema.
- **Alternatives Considered**:
  1. *Regex / Keyword Blacklist*: Trivial to bypass with SQL evasion techniques (e.g., encoded characters, nested subqueries).
  2. *Direct LLM execution*: Extremely dangerous; susceptible to prompt injection where malicious review text tells the LLM to `DROP TABLE`.
- **Trade-offs**: Complex queries with window functions or CTEs must be validated carefully by the parser.
- **Failure Modes**: Parser rejects novel complex valid SQL; model outputs unparseable text. Handled with clear feedback and safe rejection reasons.
- **Testing Strategy**: Comprehensive negative test suite covering classic and advanced SQL injection payloads, stacked queries, unauthorized table access, and sleep/benchmark DoS attacks.
