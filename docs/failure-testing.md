# DataFlow Distributed Reliability & Failure-Testing Specification

Reliability under partial failures and network partitions is a core engineering requirement of **DataFlow**. This document defines the failure modes, expected behavior, retry strategies, and recovery procedures for the 10 failure scenarios specified in the platform blueprint.

---

## Failure Matrix & Recovery Protocols

| # | Failure Scenario | Detection Mechanism | System Behavior | Retry / Recovery Policy | Consistency Guarantee |
|---|------------------|---------------------|-----------------|-------------------------|------------------------|
| **1** | **Ingestion crashes during upload to MinIO** | MinIO SDK timeout or broken TCP socket | Incomplete multipart upload; transaction discarded. Ingestion job remains in `PENDING` or marked `FAILED`. | Idempotent retry. MinIO garbage-collects uncommitted multipart uploads after 24h. Re-uploading recomputes SHA-256 and creates a clean partition. | **At-Most-Once / Clean Retry** (No corrupted partial files in raw zone). |
| **2** | **Spark batch job fails halfway through** | Spark driver task failure or executor OOM | Partial Parquet output written to temporary staging directory (`_temporary/`). | Spark transactional output committer aborts uncommitted tasks. Pipeline job marked `FAILED` with diagnostic stack trace. Job can be re-run with new `run_id`. | **Atomic Overwrite** (Output visible only if entire batch job succeeds). |
| **3** | **Analytical Warehouse (Snowflake) is unavailable** | JDBC connection timeout (5000ms threshold) | Data lake batch remains in `processed/` zone. Staging loader catches `SQLException` and halts warehouse stage. | Exponential backoff (initial delay: 5s, multiplier: 2.0, max attempts: 3). If warehouse remains suspended, pipeline job flags warning and preserves data lake Parquet for delayed bulk COPY. | **Zero Data Loss** (Data lake acts as immutable buffer). |
| **4** | **Kafka delivers an event more than once** | Ingestion pipeline receives duplicate `eventId` | Consumer deduplication layer checks in-memory LRU cache / database primary key constraint on `eventId`. | Duplicate event is acknowledged and logged as duplicate with zero state mutation. | **Effective Exactly-Once** via Idempotent Consumer. |
| **5** | **Consumer crashes before committing offset** | Kafka broker heartbeat timeout triggers group rebalance | Another consumer in `dataflow-analytics-group` takes ownership of the uncommitted partition and re-reads from last committed offset. | At-least-once delivery; replayed events pass through idempotent deduplication layer before updating state. | **At-Least-Once with Idempotency**. |
| **6** | **Consumer processes message but fails before sink write** | Sink connection exception or thread interrupt | Offset is NOT committed to Kafka broker. | The message remains uncommitted. Upon consumer restart or rebalance, the event is re-read and re-processed cleanly. | **At-Least-Once Delivery**. |
| **7** | **Pipeline triggered twice concurrently** | Database unique constraint on active running pipeline or distributed lock | Second request receives `409 Conflict` or is queued into `PENDING` state with unique `run_id`. | Distributed lock prevents concurrent Spark writes to the same target partition directory. | **Mutual Exclusion / Serializable Job State**. |
| **8** | **dbt test fails (e.g. unique constraint or not_null)** | dbt exit code `!= 0` caught by Airflow task runner | dbt halts downstream Mart materialization. Upstream Silver views remain untouched. Airflow task flags `FAILED`. | Alerts dispatched. Upstream Spark quarantine records inspected for schema drifts. No dirty data promoted to Gold Marts. | **Quality Gate Enforcement** (Marts remain uncorrupted). |
| **9** | **AI Review Enrichment or Ollama times out** | HTTP client timeout (5000ms threshold) | Non-blocking fallback; un-enriched review stored with sentiment `NEUTRAL` and flagged for offline enrichment worker. | Circuit breaker trips after 5 consecutive timeouts, bypassing LLM calls to keep main batch ingestion throughput unhindered. | **Graceful Degradation** (Core ETL never blocked by AI outage). |
| **10** | **Frontend loses connection to backend** | Fetch API network error (`Failed to fetch`) | React UI displays top floating warning banner: *"Connection interrupted. Attempting reconnect..."* | Exponential polling backoff (2s, 4s, 8s). Once connection resumes, UI seamlessly refreshes latest pipeline status. | **Eventual Consistency & Fault-Tolerant UX**. |

---

## 2. Controlled Failure Demonstrations

### Test Case A: Deduplication Resilience
1. Run Spark Batch ETL with deliberate duplicate record (`restaurant_id = 1001` present twice).
2. **Observed Result**: Spark Window partition correctly preserves the single latest record in `processed/` and routes the duplicate to `quarantine/` with `error_code = ERR_DUPLICATE_RECORD`.

### Test Case B: Malicious Text-to-SQL Interception
1. Submit `DROP TABLE restaurants; --` to `/api/v1/ai/text-to-sql`.
2. **Observed Result**: JSqlParser AST analysis intercepts the query in 1.4ms and returns `400 Bad Request` with `SecurityValidationException: Prohibited statement type: only SELECT queries are permitted`. No SQL touches the database.
