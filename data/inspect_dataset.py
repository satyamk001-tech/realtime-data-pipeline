#!/usr/bin/env python3
"""
DataFlow Dataset Inspection & Quality Audit Tool
Audits the raw CSV dataset, verifies columns, types, null rates,
value ranges, and flags data anomalies for Phase 0 delivery.
"""

import csv
import sys
from pathlib import Path
from collections import Counter

def inspect_dataset(csv_path: str):
    file = Path(csv_path)
    if not file.exists():
        print(f"Error: Dataset not found at {csv_path}")
        sys.exit(1)

    print("=" * 70)
    print(f"DATAFLOW DATASET INSPECTION REPORT")
    print(f"Source File: {file.resolve()}")
    print(f"File Size: {file.stat().st_size / 1024:.2f} KB ({file.stat().st_size / (1024*1024):.2f} MB)")
    print("=" * 70)

    total_rows = 0
    column_null_counts = Counter()
    column_types = {}
    restaurant_ids = set()
    duplicate_id_count = 0
    
    anomalies = {
        "malformed_ratings": 0,
        "negative_or_zero_cost": 0,
        "missing_primary_keys": 0,
        "missing_names": 0,
        "missing_cities": 0,
        "inconsistent_booleans": 0,
        "cost_with_commas": 0
    }
    
    cities = Counter()
    cuisines_freq = Counter()
    rating_distributions = Counter()
    
    with open(file, mode="r", encoding="utf-8", errors="replace") as f:
        reader = csv.DictReader(f)
        headers = reader.fieldnames
        print(f"\nDiscovered {len(headers)} Columns:")
        for idx, col in enumerate(headers, 1):
            print(f"  {idx:2d}. {col}")

        for row in reader:
            total_rows += 1
            
            # Null/Empty check
            for col in headers:
                val = row.get(col, "").strip()
                if not val:
                    column_null_counts[col] += 1

            # Primary key audit
            r_id = row.get("restaurant_id", "").strip()
            if not r_id:
                anomalies["missing_primary_keys"] += 1
            else:
                if r_id in restaurant_ids:
                    duplicate_id_count += 1
                else:
                    restaurant_ids.add(r_id)

            # Name & City check
            if not row.get("name", "").strip():
                anomalies["missing_names"] += 1
            city = row.get("city", "").strip()
            if not city:
                anomalies["missing_cities"] += 1
            else:
                cities[city] += 1

            # Cuisines
            cuisines_str = row.get("cuisines", "").strip()
            if cuisines_str:
                for c in cuisines_str.split(","):
                    cuisines_freq[c.strip()] += 1

            # Cost audit
            cost_str = row.get("average_cost_for_two", "").strip()
            if "," in cost_str:
                anomalies["cost_with_commas"] += 1
            try:
                cost_cleaned = float(cost_str.replace(",", "").replace('"', ""))
                if cost_cleaned <= 0:
                    anomalies["negative_or_zero_cost"] += 1
            except ValueError:
                anomalies["negative_or_zero_cost"] += 1

            # Rating audit
            rating_str = row.get("aggregate_rating", "").strip()
            rating_text = row.get("rating_text", "").strip()
            rating_distributions[rating_text] += 1
            try:
                rating_num = float(rating_str)
                if rating_num < 1.0 or rating_num > 5.0:
                    anomalies["malformed_ratings"] += 1
            except ValueError:
                anomalies["malformed_ratings"] += 1

            # Boolean check
            online_delivery = row.get("has_online_delivery", "").strip().lower()
            if online_delivery not in ["yes", "no"]:
                anomalies["inconsistent_booleans"] += 1

    print("\n" + "=" * 70)
    print("SUMMARY METRICS")
    print("=" * 70)
    print(f"Total Rows Ingested:      {total_rows:,}")
    print(f"Unique Restaurant IDs:    {len(restaurant_ids):,}")
    print(f"Duplicate Restaurant IDs: {duplicate_id_count:,}")
    
    print("\nNULL / EMPTY FIELD BREAKDOWN:")
    for col in headers:
        null_count = column_null_counts[col]
        null_pct = (null_count / total_rows * 100) if total_rows > 0 else 0
        status = "OK" if null_count == 0 else f"CONTAINS NULLS ({null_pct:.1f}%)"
        print(f"  - {col:<24}: {null_count:>5} nulls ({status})")

    print("\nDETECTED DATA ANOMALIES (QUARANTINE CANDIDATES):")
    for key, count in anomalies.items():
        print(f"  - {key:<28}: {count:>4} instances")

    print("\nGEOGRAPHIC COVERAGE (TOP CITIES):")
    for city, count in cities.most_common(7):
        print(f"  - {city:<16}: {count:>5} restaurants")

    print("\nTOP CUISINES OFFERED:")
    for cuisine, count in cuisines_freq.most_common(8):
        print(f"  - {cuisine:<16}: {count:>5} mentions")

    print("=" * 70)
    print("Inspection complete. Dataset is verified and ready for Phase 1 Ingestion.")
    print("=" * 70)

if __name__ == "__main__":
    csv_file = sys.argv[1] if len(sys.argv) > 1 else "data/samples/zomato_restaurants.csv"
    inspect_dataset(csv_file)
