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
1. [Project Vision & Overview](#1-project-vision--overview)
2. [End-to-End System Architecture](#2-end-to-end-system-architecture)
3. [Technology Stack & Architectural Rationales](#3-technology-stack--architectural-rationales)
4. [Medallion Data Lake & Quarantine Architecture](#4-medallion-data-lake--quarantine-architecture)
5. [Repository Structure](#5-repository-structure)
6. [Phase-by-Phase Roadmap & Milestones](#6-phase-by-phase-roadmap--milestones)
7. [Getting Started & Local Development](#7-getting-started--local-development)
8. [Dataset & Data Quality Inspection](#8-dataset--data-quality-inspection)
9. [FAANG Interview Prep: Core Engineering Concepts](#9-faang-interview-prep-core-engineering-concepts)
10. [Security & Reliability Guarantees](#10-security--reliability-guarantees)
11. [License & Author](#11-license--author)

---

## 1. Project Vision & Overview

Modern distributed data platforms cannot afford to be basic Python ETL scripts or hardcoded dashboards. In high-scale technology companies, data platforms must guarantee:
- **Zero Silent Data Loss**: Bad records are quarantined with metadata and error codes rather than discarded.
- **Idempotent Ingestion**: Pipeline runs are safe to re-execute without creating duplicate warehouse records.
- **Strict Decoupling**: High-volume asynchronous ETL execution is decoupled from synchronous REST API endpoints.
- **Streaming Correctness**: In-order event streaming with event-time watermarking, windowed aggregations, and dead-letter queues (DLQ).
- **Hardened AI Integration**: LLM features (RAG and Text-to-SQL) are protected by strict schema sandboxing, Abstract Syntax Tree (AST) validation, and grounding verification.

DataFlow uses a Zomato-style restaurant and customer review dataset to demonstrate these principles across batch, streaming, analytical, and retrieval workflows.

---

## 2. End-to-End System Architecture

```mermaid
flowchart TD
    subgraph Data_Sources["1. Data Ingestion & Streaming Sources"]
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

## 3. Technology Stack & Architectural Rationales

| Layer | Technology | Primary Responsibility | Architectural Rationale |
|---|---|---|---|
| **Core Language** | **Java 21 (LTS)** | Backend & Distributed Processing | Strong static typing, virtual threads, high-throughput JVM memory management, enterprise industry standard. |
| **Distributed Processing** | **Apache Spark 3.5 (Java API)** | Batch ETL & Structured Streaming | Catalyst query optimizer, Tungsten memory engine, horizontal partitioning, disk spilling, and unified batch/streaming semantics. |
| **Backend Framework** | **Spring Boot 3.3** | REST APIs, Job Management, Security | Battle-tested IoC container, reactive thread pools, declarative transaction management, and OpenAPI integration. |
| **Event Streaming** | **Apache Kafka** | Real-time event log | High-throughput distributed append-only log with consumer group offset tracking and partitioned horizontal scaling. |
| **Data Lake** | **MinIO** | S3-compatible raw & processed storage | Cloud-agnostic object storage with bucket isolation, checksum verification, and partition-pruned Parquet files. |
| **Data Warehouse** | **Snowflake** | Cloud OLAP Analytical Warehousing | Separation of storage and compute, micro-partitioning, and columnar vectorized analytics over millions of rows. |
| **Transformations** | **dbt Core** | In-warehouse modeling & testing | Version-controlled SQL modeling, automated DAG dependencies, snapshot tracking, and data quality testing. |
| **App DB & Vector** | **PostgreSQL 16 + pgvector** | Application metadata & Vector Search | ACID transactional persistence for jobs/users combined with sub-millisecond approximate nearest neighbor (ANN) vector search. |
| **Frontend** | **React 18 + TypeScript** | Operations & Analytics Dashboard | Type-safe, component-driven UI with real-time SSE/WebSocket feeds, responsive analytical charts, and operations monitoring. |
| **Observability** | **Prometheus + Grafana** | Metrics, SLIs/SLOs, Dashboards | Pull-based timeseries metric scraping with customizable Grafana dashboards for throughput, lag, and latency. |

---

## 4. Medallion Data Lake & Quarantine Architecture

DataFlow strictly enforces a three-zone **Medallion Data Lake Architecture**:

```
dataflow-lake/
|
|-- raw/restaurants/ingestion_date=YYYY-MM-DD/run_id=<uuid>/
|   +-- source_dataset.csv            (Immutable original upload)
|   +-- ingestion_manifest.json       (Run ID, file hash, row count, checksum)
|
|-- processed/restaurants/ingestion_date=YYYY-MM-DD/
|   +-- part-00000.snappy.parquet     (Normalized, typed, columnar)
|   +-- part-00001.snappy.parquet     (Partitioned by city / cuisine)
|
+-- quarantine/restaurants/ingestion_date=YYYY-MM-DD/
    +-- rejected_records.parquet      (Original record + failure_code + reason)
```

### Quarantine Conditions:
1. `ERR_MISSING_PK`: Missing or non-positive `restaurant_id`.
2. `ERR_MISSING_MANDATORY_FIELD`: Missing `name` or `city`.
3. `ERR_INVALID_RATING`: `aggregate_rating < 1.0` or `aggregate_rating > 5.0`.
4. `ERR_INVALID_COST`: `average_cost_for_two <= 0`.
5. `ERR_DUPLICATE_RECORD`: Duplicate business key within batch (latest record preserved, duplicate routed to quarantine).

---

## 5. Repository Structure

```
realtime-data-pipeline/
|-- backend/                 # Spring Boot REST API, Security, Text-to-SQL, RAG
|   |-- src/main/java/com/dataflow/
|   |-- pom.xml
|   +-- Dockerfile
|-- frontend/                # React + TypeScript Operations & Analytics Dashboard
|   |-- src/
|   |-- package.json
|   +-- Dockerfile
|-- ingestion/               # Standalone Java Ingestion Service (S3/MinIO upload)
|   |-- src/main/java/com/dataflow/ingestion/
|   +-- pom.xml
|-- spark-jobs/              # Apache Spark Batch ETL & Streaming Jobs (Java API)
|   |-- src/main/java/com/dataflow/spark/
|   +-- pom.xml
|-- kafka/                   # Event schemas, synthetic producers, topic configs
|-- dbt/                     # dbt Core models (staging, dimensions, facts, marts)
|-- airflow/                 # Airflow batch orchestration DAGs
|-- database/                # PostgreSQL schema migrations (Flyway)
|-- infrastructure/          # Docker compose, Prometheus, Grafana configs
|-- data/
|   |-- samples/             # Sample datasets with deliberate test anomalies
|   |-- generate_dataset.py  # Parametric 10k/100k/1M dataset generator
|   |-- inspect_dataset.py   # Dataset auditing & validation CLI
|   +-- README.md
|-- docs/
|   |-- architecture.md      # Detailed system architecture document
|   |-- data-dictionary.md   # Field-level dictionary and star schema
|   |-- design-decisions.md  # Architecture Decision Records (ADRs)
|-- pom.xml                  # Root parent Maven multi-module configuration
|-- docker-compose.yml       # Local multi-service infrastructure
|-- .env.example             # Environment template
|-- .gitignore
+-- README.md
```

---

## 6. Phase-by-Phase Roadmap & Milestones

- [x] **Phase 0 — Project Planning & Foundations** *(Current)*
  - Inspect dataset schemas, null distributions, and anomalies.
  - Architecture blueprint, ADRs, data dictionary, and parent Maven setup.
  - Remote repository setup: `https://github.com/satyamk001-tech/realtime-data-pipeline.git`.
- [ ] **Phase 1 — Java Ingestion Foundation**
  - Implement `Java Ingestion Service` with CSV parsing, schema checking, and run metadata generation.
- [ ] **Phase 2 — Object Storage (MinIO)**
  - Integrate MinIO S3 SDK. Organize `raw/`, `processed/`, and `quarantine/` zones with checksum validation.
- [ ] **Phase 3 & 4 — Apache Spark Batch ETL & Quarantine**
  - Implement Spark Java batch job with schema validation, normalization, deduplication, and Parquet output.
- [ ] **Phase 5 & 6 — Snowflake Warehousing & dbt Transformations**
  - Snowflake Bronze/Silver/Gold staging, dbt models, and analytical star schema tests.
- [ ] **Phase 7 & 8 — Spring Boot Backend & React Dashboard**
  - Asynchronous pipeline APIs, REST endpoints, and responsive interactive web UI.
- [ ] **Phase 9 & 10 — Kafka & Spark Structured Streaming**
  - Event producer, topic partitioning, streaming micro-batches, window aggregations, and DLQ.
- [ ] **Phase 11 & 12 — AI Review Enrichment & pgvector RAG**
  - Sentiment analysis, vector embeddings, and citation-grounded semantic retrieval.
- [ ] **Phase 13 — Secure Text-to-SQL Engine**
  - AST parsing via JSqlParser, read-only restriction, table whitelisting, and query execution.
- [ ] **Phase 14-16 — Airflow Orchestration & Observability**
  - Airflow DAG, Prometheus metric scraping, and Grafana operations dashboard.
- [ ] **Phase 17-20 — Testing, Benchmarks & Portfolio Preparation**
  - Automated integration tests, 1M record performance benchmarks, and interview presentation.

---

## 7. Getting Started & Local Development

### Prerequisites
- **Java 21+** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Python 3.10+** (for dataset generation and inspection)
- **Docker & Docker Compose** (for local infrastructure)

### Quick Start: Phase 0 Verification
```bash
# 1. Clone repository
git clone https://github.com/satyamk001-tech/realtime-data-pipeline.git
cd realtime-data-pipeline

# 2. Inspect the sample dataset and data quality rules
python data/inspect_dataset.py

# 3. Generate a scaled benchmark dataset (optional: 50,000 rows)
python data/generate_dataset.py --rows 50000 --output data/samples/zomato_50k.csv

# 4. Verify Maven parent build
mvn clean compile
```

---

## 8. Dataset & Data Quality Inspection

Our curated dataset (`data/samples/zomato_restaurants.csv`) contains real Zomato-style restaurant profiles across 7 metropolitan cities (Bangalore, New Delhi, Mumbai, Hyderabad, Pune, Chennai, Kolkata) with deliberately injected edge cases to test pipeline resilience:

```text
======================================================================
DATAFLOW DATASET INSPECTION REPORT
Source File: data/samples/zomato_restaurants.csv
File Size: 333.44 KB (0.33 MB)
======================================================================
Total Rows Ingested:      1,500
Unique Restaurant IDs:    1,499
Duplicate Restaurant IDs: 1

DETECTED DATA ANOMALIES (QUARANTINE CANDIDATES):
  - malformed_ratings           :   36 instances (text 'NEW', '-', or out-of-bounds)
  - negative_or_zero_cost       :    9 instances (costs <= 0)
  - missing_names               :    9 instances
  - missing_cities              :    8 instances
  - inconsistent_booleans       :    6 instances (mixed case / numeric booleans)
  - cost_with_commas            :    1 instances (unparsed formatted strings)
======================================================================
```

---

## 9. FAANG Interview Prep: Core Engineering Concepts

1. **Why use Spark instead of Java Collections?**
   Java collections are bound to a single JVM's heap memory. Processing 5M+ rows in memory causes `OutOfMemoryError`. Spark provides distributed memory management (Tungsten), disk spilling, cost-based query optimization (Catalyst), and horizontal partition parallelism.
2. **What causes a Spark shuffle?**
   Wide transformations (`groupByKey`, `reduceByKey`, `join`, `repartition`, `distinct`) require re-distributing data across partitions and executor network boundaries. Shuffles incur severe disk I/O and network latency. We optimize by broadcasting small dimension tables (`broadcast join`) and choosing optimal partition keys.
3. **How does Kafka guarantee message order?**
   Kafka guarantees strict order **only within a single partition**, not across the entire topic. By assigning `restaurant_id` as the message key, all events for a given restaurant hash to the same partition, guaranteeing chronological processing.
4. **Why use Snowflake alongside PostgreSQL?**
   PostgreSQL is an OLTP row store optimal for sub-10ms transactional writes, foreign keys, and application state. Snowflake is an OLAP column store optimal for scanning billions of rows across specific aggregate columns with micro-partition pruning.
5. **How does Text-to-SQL prevent SQL Injection?**
   Never rely on keyword string replacement. DataFlow parses candidate SQL into an Abstract Syntax Tree (AST) using JSqlParser, verifying that the root node is strictly a `Select` query, rejecting comments, semicolons, and DDL/DML statements, whitelisting tables to `ZOMATO.MARTS`, and executing under a read-only database role with strict statement timeouts.

---

## 10. Security & Reliability Guarantees

- **No Plaintext Secrets**: All sensitive tokens and credentials are encrypted or injected via `.env`.
- **Idempotent Ingestion**: Every batch ingestion run produces a deterministic SHA-256 checksum and unique UUID run ticket.
- **Fail-Safe Quarantining**: Corrupted records are diverted to the Quarantine object zone with diagnostic error reasons without halting downstream processing.
- **Least-Privilege Roles**: Database accounts for analytics and Text-to-SQL are restricted to `SELECT` only on analytical marts.

---

## 11. License & Author

- **Project**: DataFlow (Real-Time Distributed Data Pipeline and Analytics Platform)
- **Author**: Satyam Kumar ([@satyamk001-tech](https://github.com/satyamk001-tech))
- **Repository**: [realtime-data-pipeline](https://github.com/satyamk001-tech/realtime-data-pipeline)
- **License**: MIT License
