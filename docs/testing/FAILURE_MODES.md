# DataFlow — Failure Engineering & Resilience Matrix

This document details the observed fault behavior, recovery mechanisms, circuit fallback guarantees, and mitigation strategies implemented across the **DataFlow** distributed data pipeline.

---

## 1. Subsystem Failure Matrix

| Failure Mode | Detection Mechanism | Immediate Behavior | Recovery / Mitigation Strategy | Verification Evidence |
|---|---|---|---|---|
| **Kafka Broker Unavailable** | `KafkaProducerException` / connection timeout | Asynchronous CompletableFuture error handler logs failure; pipeline execution does not block or throw unhandled 500 error | Non-blocking execution with retry backoff; local operational persistence retains state | `PipelineEventProducerTest.java` |
| **Malformed / Corrupt Kafka Payload** | Consumer parsing validation (`JSON syntax / structural checks`) | Unparseable message intercepted in `@KafkaListener` | **Dead Letter Queue (DLQ)**: Message forwarded with diagnostic error headers to `dataflow.restaurant.dlq` for offline triage | `RestaurantStreamConsumerTest.java` |
| **PostgreSQL Connection Exhaustion / Timeout** | HikariCP pool timeout (`connection-timeout: 5000ms`) | Throws `SQLException`; active transactions rolled back | Read-only replica routing; structured JSON error envelope returned to API caller | `GlobalExceptionHandler.java` |
| **Corrupted / Truncated Ingestion File** | `CsvValidator` pre-flight schema analysis | File rejected before any byte is saved to data lake | Quarantined in rejection bucket with cryptographic SHA-256 validation log | `CsvValidatorTest.java` |
| **Malicious SQL Injection Attempt** | JSqlParser AST analysis (`validateSqlSafety`) | Query execution stopped before opening JDBC statement | **Strict Security Gate**: 100% rejection with `SecurityValidationException`; whitelisted SELECT statements only | `TextToSqlServiceTest.java` |
| **High Consumer Lag Burst** | Prometheus Micrometer gauge `kafka.consumer.lag` | Backpressure observed via partition offset lag | Spark Structured Streaming dynamic batch sizing with 10-minute watermarks | `RestaurantStreamingJob.java` |
| **Duplicate Event Ingestion** | Primary key constraint on `restaurant_id` / `event_id` | Database unique constraint violation intercepted | Idempotent upsert via JPA `saveAll` ensures deduplication without data corruption | `PipelineJobService.java` |

---

## 2. Realistic Failure Drill: Kafka Outage Simulation

```
[Simulated Condition]: Stop Kafka broker container during active ingestion job
$ docker compose stop kafka

[Observed Flow]:
1. User triggers pipeline via POST /api/v1/pipeline/run
2. IngestionService saves raw records to local lake (SHA-256 verified)
3. PipelineJobService persists valid entities to PostgreSQL operational store
4. PipelineEventProducer attempts Kafka event publication:
   -> Timeout intercepted: "Resilient fallback: Error dispatching pipeline event"
   -> Pipeline job status recorded as SUCCEEDED in database
   -> REST API client receives HTTP 200 OK with valid record statistics
5. No thread lockup, memory leak, or uncaught 500 exception
```

---

## 3. Realistic Failure Drill: Dead Letter Queue (DLQ) Recovery

```
[Simulated Condition]: Publish non-JSON byte sequence to 'dataflow.restaurant.reviews'
$ echo "CORRUPTED_STREAM_BYTE_DATA" | kafka-console-producer --topic dataflow.restaurant.reviews

[Observed Flow]:
1. RestaurantStreamConsumer receives corrupt payload
2. JSON validator triggers IllegalArgumentException
3. DLQ Counter incremented in Micrometer metrics
4. Message enriched with originalKey and error cause:
   {"originalKey":"null","error":"Payload does not conform to valid JSON format","raw":"CORRUPTED_STREAM_BYTE_DATA"}
5. Forwarded to 'dataflow.restaurant.dlq'
6. Consumer offset successfully advances, preventing partition blocking
```
