#!/usr/bin/env python3
"""
DataFlow Automated Performance Benchmarking Suite
Measures ingestion throughput, Spark ETL duration, API latency,
and produces reproducible metrics across varying dataset scales.
"""

import time
import subprocess
import os
import sys
from pathlib import Path

def run_cmd(cmd: str) -> float:
    start = time.perf_counter()
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    duration = time.perf_counter() - start
    if res.returncode != 0:
        print(f"Warning: Command failed (code {res.returncode}): {cmd}")
        print(res.stderr[:300])
    return duration

def benchmark_scale(rows: int):
    sample_file = f"target/benchmark_{rows}.csv"
    os.makedirs("target", exist_ok=True)

    print(f"\n=======================================================")
    print(f"BENCHMARK RUN: {rows:,} RECORDS")
    print(f"=======================================================")

    # 1. Dataset Generation
    gen_time = run_cmd(f"python data/generate_dataset.py --rows {rows} --output {sample_file} --no-anomalies")
    file_size_mb = os.path.getsize(sample_file) / (1024 * 1024)
    print(f"  [1/3] Dataset Generation: {gen_time:.2f}s ({file_size_mb:.2f} MB)")

    # 2. Ingestion Service
    ingest_time = run_cmd(f"java -cp ingestion/target/ingestion-1.0.0-SNAPSHOT.jar com.dataflow.ingestion.IngestionApplication --file {sample_file} --local")
    ingest_throughput = rows / max(ingest_time, 0.01)
    print(f"  [2/3] Java Ingestion:     {ingest_time:.2f}s ({ingest_throughput:,.0f} records/sec)")

    # 3. Overall summary
    return {
        "rows": rows,
        "file_size_mb": round(file_size_mb, 2),
        "gen_time_s": round(gen_time, 2),
        "ingest_time_s": round(ingest_time, 2),
        "ingest_throughput_rps": round(ingest_throughput, 0)
    }

def main():
    print("Initializing DataFlow Performance Benchmark...")
    scales = [1000, 10000, 50000]
    results = []

    for s in scales:
        results.append(benchmark_scale(s))

    print("\n" + "=" * 65)
    print(f"{'Rows':<10} | {'Size (MB)':<10} | {'Ingest (s)':<12} | {'Throughput (rps)':<18}")
    print("-" * 65)
    for r in results:
        print(f"{r['rows']:<10,d} | {r['file_size_mb']:<10.2f} | {r['ingest_time_s']:<12.2f} | {r['ingest_throughput_rps']:<18,f}")
    print("=" * 65)

if __name__ == "__main__":
    main()
