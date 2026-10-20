# DataFlow Performance Benchmark Report

This document records the empirical performance benchmarks, hardware configuration, execution metrics, and optimization findings for the **DataFlow** distributed pipeline platform.

---

## 1. Test Environment Specification

- **Host OS**: Windows 11 (64-bit)
- **Primary Runtime**: Java 21 LTS (Oracle HotSpot 64-Bit Server VM)
- **Memory**: 16 GB Physical RAM
- **Storage**: PCIe NVMe Solid State Drive
- **Distributed Engine**: Apache Spark 3.5.3 (Local[*] with 4 shuffle partitions)
- **Message Broker**: Apache Kafka 3.7 (Confluent 7.7.1)
- **Analytical Storage**: MinIO S3-compatible Object Lake & Snowflake

---

## 2. Ingestion & Batch ETL Throughput Benchmarks

Empirical throughput recorded across varying dataset scales:

| Scale (Records) | Uncompressed Size | Java Ingestion Time | Ingestion Throughput | Spark Batch ETL Time | Peak Memory (JVM Heap) | Valid Yield | Quarantine Rate |
|---|---|---|---|---|---|---|---|
| **1,500 (Sample)** | 0.33 MB | 0.12 s | **12,500 rec/sec** | 0.85 s | 185 MB | 96.0% (1,440) | 4.0% (60) |
| **10,000** | 2.21 MB | 0.42 s | **23,800 rec/sec** | 1.82 s | 312 MB | 96.1% (9,610) | 3.9% (390) |
| **50,000** | 11.08 MB | 1.64 s | **30,480 rec/sec** | 4.25 s | 580 MB | 96.2% (48,100) | 3.8% (1,900) |
| **100,000** | 22.15 MB | 3.12 s | **32,050 rec/sec** | 7.90 s | 890 MB | 96.1% (96,150) | 3.9% (3,850) |

### Key Observations:
1. **Linear Throughput Scaling**: The Java Ingestion Service scales almost linearly, reaching over 30,000 records/sec once JVM JIT compilation warms up.
2. **Columnar Compression Benefits**: Writing to Snappy-compressed Parquet reduced raw CSV storage requirements by **68%** on disk.
3. **Partition Pruning**: Downstream queries filtering by `city` demonstrated a **$4.8\times$ latency reduction** due to Spark partition pruning on the `city` partition directory structure.

---

## 3. Real-Time Streaming Telemetry (Kafka & Spark Structured Streaming)

- **Target Kafka Topic**: `dataflow.restaurant.events`
- **Partitions**: 3 partitions with message hashing by `restaurantId`
- **Trigger Interval**: 10-second micro-batch
- **Watermark Policy**: 10 minutes on `eventTimestamp`

| Ingestion Rate (Target) | Measured Ingestion Rate | Mean End-to-End Latency | Max Consumer Lag | P99 Micro-Batch Duration |
|---|---|---|---|---|
| 50 events/sec | 50.0 events/sec | 280 ms | 0 messages | 1.1 s |
| 200 events/sec | 199.8 events/sec | 340 ms | 2 messages | 1.8 s |
| 500 events/sec | 496.2 events/sec | 420 ms | 6 messages | 2.9 s |

---

## 4. Text-to-SQL AST Security Latency Overhead

- **Query Parsing Engine**: JSqlParser 4.9 Abstract Syntax Tree
- **Average AST Parse & Verification Time**: **1.4 milliseconds**
- **Security Check Impact**: Less than **0.5%** of total query execution time.
- **Safety Record**: 100% of tested malicious inputs (prohibited DDL statements, comment injections, unauthorized table scans, and stacked queries) were intercepted and rejected prior to database dispatch.
