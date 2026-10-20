# Data Directory & Dataset Specification

This directory manages dataset assets, sample inputs, synthetic data generators, and data dictionary mappings for the **DataFlow** distributed data platform.

---

## 1. Primary Dataset: Zomato Restaurant & Review Data

Inspired by real-world restaurant analytics platforms (such as the Zomato Bangalore & Global restaurant datasets), our dataset captures detailed metadata across restaurant profiles, operational features, pricing, ratings, and customer feedback.

### Source File Types:
- `data/samples/zomato_restaurants.csv`: Curated sample containing valid records, edge cases, and deliberate anomalies designed to test validation, deduplication, and quarantine pipelines.
- `data/generate_dataset.py`: Parametric synthetic dataset generator capable of producing 1,000 to 5,000,000+ realistic restaurant and review records with deterministic distributions and tunable anomaly injection rates.

---

## 2. Dataset Columns & Schema Overview

| # | Column Name | Raw Type | Clean Type | Description | Quality Rules / Anomaly Checks |
|---|-------------|----------|------------|-------------|--------------------------------|
| 1 | `restaurant_id` | String / Long | Long | Unique primary business identifier | Required, Positive Integer, Non-null |
| 2 | `name` | String | String | Name of the restaurant | Required, Trimmed text, Non-empty |
| 3 | `city` | String | String | Metropolitan city (e.g. Bangalore, Delhi) | Standardized Title Case, Non-empty |
| 4 | `address` | String | String | Physical address of the restaurant | Quoted string handling commas |
| 5 | `locality` | String | String | Neighborhood or district | Non-empty |
| 6 | `cuisines` | String | Array<String> | Comma-separated cuisine offerings | Standardized, split on comma, trimmed |
| 7 | `average_cost_for_two` | String / Double | Double | Average meal cost for two persons | Must be > 0. Handle commas (`"1,200"` -> `1200.0`) |
| 8 | `currency` | String | String | Currency code (INR, USD, etc.) | Standardized ISO currency |
| 9 | `has_online_delivery` | String | Boolean | Delivery availability flag | Cast `"Yes"`, `"1"`, `"true"` -> `true`, else `false` |
| 10 | `has_table_booking` | String | Boolean | Table reservation flag | Cast `"Yes"`, `"1"`, `"true"` -> `true`, else `false` |
| 11 | `price_range` | String / Integer | Integer | Tier scale (1 = Budget, 4 = Fine Dining) | Clamped range [1, 4] |
| 12 | `aggregate_rating` | String / Double | Double | Numerical rating on 1.0 to 5.0 scale | Must be between 1.0 and 5.0. Clean `"NEW"`, `"-"`, `"4.1/5"` |
| 13 | `rating_text` | String | String | Human rating label (Excellent, Good, etc.) | Standardized text |
| 14 | `votes` | String / Long | Long | Total number of customer votes | Non-negative integer (>= 0) |
| 15 | `review_text` | String | String | Sample customer review text | Used for AI Sentiment and RAG Vector Search |

---

## 3. Deliberate Data Quality Test Scenarios

To ensure the Java Ingestion and Spark ETL layers are production-tested, the sample dataset deliberately contains controlled anomalies:

1. **Malformed Ratings**: `"NEW"`, `"-"`, `"3.9/5"`, negative ratings (`-1.0`), and ratings above 5 (`6.5`).
2. **Pricing Anomalies**: Zero or negative cost for two (`-500`, `0`), and formatted numbers with commas (`"1,500"`).
3. **Missing Critical Fields**: Null or blank `restaurant_id`, `name`, or `city`.
4. **Boolean Inconsistencies**: Mixed case (`"YES"`, `"No"`, `"True"`, `"0"`, `"1"`).
5. **Duplicate Business Records**: Identical `restaurant_id` entries with differing ingestion timestamps or duplicate restaurant names and addresses.
6. **Rich Review Text**: Positive, negative, and mixed reviews containing service delays, food quality remarks, and specific dish mentions to evaluate the AI Sentiment and pgvector RAG pipeline.

---

## 4. Generating Benchmark Datasets

To generate 100,000 or 1,000,000 records for performance and stress testing:

```bash
# Generate 100,000 records
python data/generate_dataset.py --rows 100000 --output data/samples/zomato_100k.csv

# Generate 1,000,000 records
python data/generate_dataset.py --rows 1000000 --output data/samples/zomato_1m.csv
```
