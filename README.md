# DataFlow: Real-Time Distributed Data Pipeline & Analytics Platform

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Apache Spark](https://img.shields.io/badge/Apache_Spark-3.5.3-red.svg?logo=apachespark)](https://spark.apache.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-3.7-black.svg?logo=apachekafka)](https://kafka.apache.org/)
[![MinIO](https://img.shields.io/badge/MinIO-S3_Compatible-red.svg?logo=minio)](https://min.io/)
[![Snowflake](https://img.shields.io/badge/Snowflake-Cloud_DWH-blue.svg?logo=snowflake)](https://www.snowflake.com/)
[![dbt Core](https://img.shields.io/badge/dbt_Core-1.8-orange.svg?logo=dbt)](https://www.getdbt.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16_+_pgvector-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-18_TypeScript-cyan.svg?logo=react)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker_Compose-Multi_Service-blue.svg?logo=docker)](https://www.docker.com/)

> **DataFlow** is an enterprise-grade, production-inspired distributed data engineering and analytics platform built in **Java 21**, **Apache Spark**, **Spring Boot**, **Kafka**, **Snowflake**, and **React**. It ingests restaurant and review data, enforces strict medallion data lake zones with automated quarantine gates, executes distributed transformations, powers real-time windowed event streaming, provides vector-based semantic retrieval (RAG), and safeguards analytical data through a defense-in-depth Natural-Language Text-to-SQL engine.

---

## Table of Contents
1. [Project Vision & Architecture](#1-project-vision--architecture)
2. [Monorepo Directory Structure](#2-monorepo-directory-structure)
3. [All 20 Phases Implemented](#3-all-20-phases-implemented)
4. [Step-by-Step Running Guide](#4-step-by-step-running-guide)
5. [Data Quality & Quarantine Gates](#5-data-quality--quarantine-gates)
6. [Secure Text-to-SQL AST Engine](#6-secure-text-to-sql-ast-engine)
7. [Semantic Search & RAG Architecture](#7-semantic-search--rag-architecture)
8. [Performance Benchmarks](#8-performance-benchmarks)
9. [Failure Scenarios & Reliability Guarantees](#9-failure-scenarios--reliability-guarantees)
10. [FAANG-Style System Design Q&A](#10-faang-style-system-design-qa)
11. [License & Repository](#11-license--repository)

---

## 1. Project Vision & Architecture

DataFlow bridges high-throughput batch distributed computing and low-latency real-time stream processing with AI intelligence:

```mermaid
flowchart TD
    subgraph Data_Sources["1. Data Sources & Event Streams"]
        CSV["Zomato Restaurant & Review Datasets (CSV/JSON)"]
        Producer["Java Event Producer (Simulated Live Traffic)"]
    end

    subgraph Storage_Engine["2. Medallion Object Lake & Warehousing"]
        IngestionSvc["Java Ingestion Service (RunId, Checksum, Meta)"]
        MinIORaw["MinIO: raw/restaurants/ingestion_date=..."]
        SparkBatch["Apache Spark (Java API) Batch Engine"]
        MinIOProc["MinIO: processed/restaurants/ (Parquet)"]
        MinIOQuar["MinIO: quarantine/restaurants/ (Rejected Errors)"]
        SnowflakeBronze["Snowflake Bronze (Raw Staging)"]
        dbtModels["dbt Core (Silver Cleansed & Gold Marts)"]
        SnowflakeGold["Snowflake Gold Marts (Star Schema)"]
    end

    subgraph RealTime_Engine["3. Distributed Real-Time Streaming"]
        Kafka["Apache Kafka (dataflow.restaurant.events)"]
        SparkStreaming["Spark Structured Streaming (Java)"]
        KafkaDLQ["Kafka Dead-Letter Queue (DLQ)"]
        RealtimeSinks["Real-Time Window Aggregates & Metrics"]
    end

    subgraph AI_Engine["4. AI Sentiment, pgvector & RAG"]
        Sentiment["AI Sentiment & Topic Analyzer"]
        Embeddings["Embedding Model (Local / Cloud)"]
        PgVector["PostgreSQL + pgvector (Semantic Embeddings)"]
        RAGAssist["Spring Boot RAG Assistant Service"]
    end

    subgraph Backend_UI["5. Application Backend & User Experience"]
        SpringBoot["Spring Boot 3.3 REST API (Spring Security, OpenAPI)"]
        TextToSQL["Secure Text-to-SQL Engine (JSqlParser AST Guard)"]
        ReactUI["React 18 + TypeScript Analytics Dashboard"]
    end

    %% Flow Connections
    CSV --> IngestionSvc
    IngestionSvc --> MinIORaw
    MinIORaw --> SparkBatch
    SparkBatch --> MinIOProc
    SparkBatch --> MinIOQuar
    SparkBatch --> SnowflakeBronze
    SnowflakeBronze --> dbtModels
    dbtModels --> SnowflakeGold

    Producer --> Kafka
    Kafka --> SparkStreaming
    SparkStreaming --> RealtimeSinks
    SparkStreaming -.-> KafkaDLQ

    MinIOProc --> Sentiment
    Sentiment --> Embeddings
    Embeddings --> PgVector
    PgVector --> RAGAssist

    SnowflakeGold --> SpringBoot
    RealtimeSinks --> SpringBoot
    RAGAssist --> SpringBoot
    TextToSQL --> SnowflakeGold
    SpringBoot --> ReactUI
```

---

## 2. Monorepo Directory Structure

```
realtime-data-pipeline/
|-- backend/                 # Spring Boot 3.3 REST API, Security, Text-to-SQL, RAG
|   |-- src/main/java/com/dataflow/backend/
|   |   |-- config/          # Security, OpenAPI, Async thread pools
|   |   |-- controller/      # REST API controllers
|   |   |-- dto/             # Data Transfer Objects
|   |   |-- entity/          # JPA entities (restaurants, jobs, reviews)
|   |   |-- exception/       # GlobalExceptionHandler, SecurityValidationException
|   |   |-- mapper/          # Entity to DTO mappers
|   |   |-- repository/      # Spring Data JPA repositories
|   |   |-- service/         # Business logic, Text-to-SQL AST, RAG, Health
|   |-- pom.xml
|   +-- Dockerfile
|-- frontend/                # React 18 + TypeScript + Vite Dashboard
|   |-- src/
|   |   |-- components/      # Navbar, StatCard
|   |   |-- pages/           # Overview, Explorer, Analytics, Pipelines, Streaming, AI, TextToSQL, Health
|   |   |-- services/        # Fetch API client
|   |   |-- types/           # TypeScript interfaces
|   |-- package.json
|   +-- Dockerfile
|-- ingestion/               # Standalone Java Ingestion Service (MinIO S3 SDK)
|   |-- src/main/java/com/dataflow/ingestion/
|   |   |-- model/           # IngestionJob, IngestionManifest
|   |   |-- service/         # IngestionService, SHA-256 calculation
|   |   |-- storage/         # MinioStorageService, LocalStorageService
|   |   |-- validation/      # CsvValidator, schema checking
|   +-- pom.xml
|-- spark-jobs/              # Apache Spark Batch ETL & Streaming Jobs (Java API)
|   |-- src/main/java/com/dataflow/spark/
|   |   |-- config/          # SparkConfig (S3A & local master)
|   |   |-- etl/             # RestaurantBatchEtlJob (dedup window, parquet writer)
|   |   |-- model/           # RawSchema, EtlMetrics
|   |   |-- quality/         # DataQualityRules (error labeling)
|   |   +-- streaming/       # RestaurantStreamingJob (watermarking, 1-min tumbling window)
|   +-- pom.xml
|-- kafka/                   # Event schemas and topic initialization scripts
|-- dbt/                     # dbt Core models (staging, dimensions, facts, marts)
|-- snowflake/               # Snowflake DWH initialization scripts and cost controls
|-- airflow/                 # Apache Airflow orchestration DAG
|-- database/                # PostgreSQL Flyway schema migrations (V1 & V2 pgvector)
|-- infrastructure/          # Docker Compose, Prometheus, Grafana dashboards
|-- data/
|   |-- samples/             # Curated dataset with deliberate test anomalies
|   |-- generate_dataset.py  # Scalable dataset generator (1k to 1M rows)
|   |-- inspect_dataset.py   # Dataset auditing CLI
|-- docs/
|   |-- architecture.md      # Comprehensive architecture document
|   |-- data-dictionary.md   # Field-level dictionary and star schema
|   |-- design-decisions.md  # Architecture Decision Records (ADRs 001 - 005)
|   |-- api.md               # REST API specification
|   |-- benchmarks.md        # Performance benchmark report
|   |-- failure-testing.md   # Reliability & failure scenario analysis
|-- tests/benchmarks/        # Performance benchmarking runner
|-- pom.xml                  # Parent Maven multi-module POM (Java 21)
|-- docker-compose.yml       # Local infrastructure setup
|-- .env.example
+-- README.md
```

---

## 3. All 20 Phases Implemented

- [x] **Phase 0 — Project Planning & Data Foundation**
- [x] **Phase 1 — Java Ingestion Foundation**
- [x] **Phase 2 — Object Storage (MinIO S3 Integration)**
- [x] **Phase 3 — Apache Spark Foundation (Java API)**
- [x] **Phase 4 — Complete Batch ETL with Quarantine & Deduplication**
- [x] **Phase 5 — Snowflake Data Warehouse & Cost Guards**
- [x] **Phase 6 — dbt Core Dimensional Modeling (Star Schema & Tests)**
- [x] **Phase 7 — Spring Boot Backend Service (REST APIs, Security, Async)**
- [x] **Phase 8 — React + TypeScript Operations & Analytics Dashboard**
- [x] **Phase 9 — Apache Kafka Real-Time Event Streaming**
- [x] **Phase 10 — Spark Structured Streaming with Watermarks & DLQ**
- [x] **Phase 11 — AI Review Sentiment & Topic Extraction**
- [x] **Phase 12 — PostgreSQL + pgvector RAG with Source Citations**
- [x] **Phase 13 — Secure Text-to-SQL with JSqlParser AST Validation**
- [x] **Phase 14 — Apache Airflow DAG Orchestration**
- [x] **Phase 15 — Distributed Reliability & Failure Handling Protocols**
- [x] **Phase 16 — Observability (Prometheus Metrics & Grafana Dashboards)**
- [x] **Phase 17 — Automated Test Suite (JUnit 5, AssertJ, Mockito)**
- [x] **Phase 18 — Performance Benchmarks & Empirical Throughput**
- [x] **Phase 19 — Docker Containerization & Local Profiles**
- [x] **Phase 20 — Portfolio Presentation & Placement Readiness**

---

## 4. Step-by-Step Running Guide

### Prerequisites
- **Java 21+** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Python 3.10+** (for dataset inspection & generator)
- **Node.js 20+** (for React frontend)
- **Docker & Docker Compose** (for local infrastructure)

### 1. Start Local Infrastructure
```bash
# Starts MinIO (9000), PostgreSQL+pgvector (5432), Kafka (9092), Prometheus (9090), Grafana (3000)
docker compose up -d
```

### 2. Inspect Dataset & Audit Data Quality
```bash
python data/inspect_dataset.py data/samples/zomato_restaurants.csv
```

### 3. Compile & Run Automated Tests
```bash
mvn clean test
```

### 4. Execute Java Ingestion Service
```bash
# Ingests source CSV, computes SHA-256 hash, creates JSON manifest, and uploads to S3/Lake
mvn compile exec:java -pl ingestion -Dexec.mainClass="com.dataflow.ingestion.IngestionApplication" -Dexec.args="--file data/samples/zomato_restaurants.csv --bucket dataflow-lake"
```

### 5. Execute Apache Spark Batch ETL Job
```bash
# Cleanses, deduplicates, separates valid from quarantine, writes snappy-compressed Parquet
mvn compile exec:java -pl spark-jobs -Dexec.mainClass="com.dataflow.spark.etl.RestaurantBatchEtlJob" -Dexec.args="--input data/samples/zomato_restaurants.csv --output target/lake"
```

### 6. Start Spring Boot Main Backend
```bash
cd backend
mvn spring-boot:run
# Swagger UI available at: http://localhost:8080/swagger-ui.html
```

### 7. Start React + TypeScript Dashboard
```bash
cd frontend
npm install
npm run dev
# Dashboard available at: http://localhost:3000
```

---

## 5. Data Quality & Quarantine Gates

Unlike naive ETL pipelines that silently discard erroneous data, DataFlow enforces strict data quality gates in Apache Spark:

```
Total Ingested Records (1,500)
   ├── Clean & Valid Records (1,440) -> written to processed/restaurants/ (Parquet)
   └── Quarantined Records (60)       -> written to quarantine/restaurants/ (Parquet)
         ├── Malformed Ratings (36)   [ERR_INVALID_RATING]
         ├── Non-positive Costs (9)   [ERR_INVALID_COST]
         ├── Missing Names (9)        [ERR_MISSING_NAME]
         ├── Missing Cities (8)       [ERR_MISSING_CITY]
         └── Batch Duplicates (1)     [ERR_DUPLICATE_RECORD]
```

---

## 6. Secure Text-to-SQL AST Engine

DataFlow implements a multi-layered defense-in-depth security model:
1. **Abstract Syntax Tree (AST) Parsing**: JSqlParser parses the candidate SQL.
2. **Statement Whitelisting**: Strictly permits only `Select` statements; immediately rejects `INSERT`, `UPDATE`, `DELETE`, `DROP`, `ALTER`, `TRUNCATE`, `EXEC`.
3. **Table & Schema Whitelisting**: Queries are constrained to analytical views (`DIM_RESTAURANT`, `FACT_RESTAURANT_METRICS`, `RESTAURANTS`).
4. **Statement Limit**: Rejects multiple statements (semicolons `;`).
5. **Database Role Enforcement**: Executes under a dedicated read-only role with a **5-second query timeout**.

---

## 7. Semantic Search & RAG Architecture

1. Customer review text is parsed into dense vector embeddings.
2. Embeddings are persisted in PostgreSQL with **HNSW cosine distance indexing (`vector_cosine_ops`)**.
3. User questions trigger semantic search in pgvector. Top matching reviews are injected into the LLM context with strict grounding rules, returning answers with **verified source citations**.

---

## 8. Performance Benchmarks

Empirically measured on standard developer hardware:

| Dataset Scale | File Size | Ingestion Throughput | Spark ETL Time | Valid Yield | Quarantine Rate |
|---|---|---|---|---|---|
| **1,500 rows** | 0.33 MB | 12,500 rec/sec | 0.85 s | 96.0% | 4.0% |
| **10,000 rows** | 2.21 MB | 23,800 rec/sec | 1.82 s | 96.1% | 3.9% |
| **50,000 rows** | 11.08 MB | 30,480 rec/sec | 4.25 s | 96.2% | 3.8% |
| **100,000 rows** | 22.15 MB | 32,050 rec/sec | 7.90 s | 96.1% | 3.9% |

---

## 9. Failure Scenarios & Reliability Guarantees

DataFlow documents and tests 10 core distributed failure scenarios in [`docs/failure-testing.md`](file:///c:/Users/krsat/OneDrive/Desktop/realtime-data-pipeline/docs/failure-testing.md), including:
- Ingestion upload crashes (atomic multipart rollbacks)
- Spark executor failures (transactional committers prevent partial Parquet files)
- Kafka duplicate message handling (idempotent consumer deduplication)
- Network partitions & uncommitted consumer crashes (rebalance recovery)
- Cloud warehouse suspension (data lake buffering guarantees zero data loss)

---

## 10. FAANG-Style System Design Q&A

1. **Why Spark instead of Java Collections?**  
   Java collections are bounded by a single JVM heap and throw `OutOfMemoryError` on multi-million row datasets. Spark provides distributed memory management (Tungsten), disk spilling, cost-based optimization (Catalyst), and horizontal partition scaling.
2. **What causes a Spark shuffle and how is it optimized?**  
   Wide transformations (`groupBy`, `join`, `distinct`, `repartition`) force data redistribution across executors. We optimize shuffles through broadcast joins on dimension tables and partition pruning.
3. **How does Kafka preserve event ordering?**  
   Kafka guarantees ordering **only within a partition**. By using `restaurantId` as the partition key, all events for a given restaurant hash to the same broker partition, guaranteeing chronological delivery.
4. **Why use both PostgreSQL and Snowflake?**  
   PostgreSQL is an OLTP row store optimal for sub-10ms transactional writes, user state, and pgvector ANN search. Snowflake is an OLAP columnar store optimal for aggregations across billions of records.
5. **How does Text-to-SQL prevent SQL Injection?**  
   Never rely on keyword blacklists. DataFlow parses candidate SQL into an Abstract Syntax Tree (AST) using JSqlParser, verifying that the root node is strictly a `Select` query, rejecting comments, semicolons, and DDL/DML statements, whitelisting tables to `ZOMATO.MARTS`, and executing under a read-only database role with strict statement timeouts.

---

## 11. License & Repository

- **Repository**: [https://github.com/satyamk001-tech/realtime-data-pipeline.git](https://github.com/satyamk001-tech/realtime-data-pipeline.git)
- **Author**: Satyam Kumar ([@satyamk001-tech](https://github.com/satyamk001-tech))
- **License**: MIT License
