# DataFlow Data Dictionary & Schema Specifications

This document defines the complete data dictionary for raw ingestion, data quality rules, quarantine conditions, and the downstream analytical dimensional model (Star Schema).

---

## 1. Source Data Dictionary (Raw Ingestion Layer)

The raw layer ingests restaurant and review data from CSV / JSON sources into the **Raw Object Storage Zone** (`raw/restaurants/ingestion_date=YYYY-MM-DD/`).

| Column Name | Ingested Type | Cleaned Java Type | Nullable? | Description | Example Value | Validation & Quarantine Rules |
|-------------|---------------|-------------------|-----------|-------------|---------------|-------------------------------|
| `restaurant_id` | String / Long | `Long` | **No** | Unique business identifier for the restaurant | `1001` | **Reject / Quarantine** if null, empty, non-numeric, or `<= 0`. Primary deduplication key. |
| `name` | String | `String` | **No** | Registered commercial name of the restaurant | `"The Royal Kitchen"` | **Quarantine** if null or empty. Trim whitespace; preserve character casing. |
| `city` | String | `String` | **No** | Metropolitan city location | `"Bangalore"` | **Quarantine** if null or empty. Standardize to Title Case. |
| `address` | String | `String` | Yes | Street address / storefront address | `"Shop 42, 2nd Floor, Indiranagar"` | Null replaced with `"Unknown Address"`. Trim outer quotes. |
| `locality` | String | `String` | Yes | Specific neighborhood or suburb | `"Indiranagar"` | Null replaced with `city` or `"Unspecified"`. |
| `cuisines` | String | `List<String>` | Yes | Comma-delimited list of cuisines offered | `"North Indian, Chinese"` | Split by comma, trim whitespace. If null/empty, defaults to `["Uncategorized"]`. |
| `average_cost_for_two` | String / Double | `Double` | **No** | Estimated dining cost for two people | `850.0` | **Quarantine** if `<= 0`, null, or unparseable. Strip currency symbols and commas (`"1,200"` -> `1200.0`). |
| `currency` | String | `String` | Yes | Monetary currency identifier | `"INR"` | Defaults to `"INR"`. ISO currency validation. |
| `has_online_delivery` | String | `Boolean` | Yes | Flag for online food delivery | `true` | Cast `"Yes"`, `"1"`, `"true"`, `"y"` -> `true`; all other non-empty -> `false`. Default `false`. |
| `has_table_booking` | String | `Boolean` | Yes | Flag for dine-in reservation booking | `false` | Cast `"Yes"`, `"1"`, `"true"`, `"y"` -> `true`; else `false`. Default `false`. |
| `price_range` | String / Integer | `Integer` | Yes | Budget tier (1 = Budget, 4 = Fine Dining) | `2` | Clamp to integer range `[1, 4]`. If missing, derive from `average_cost_for_two`. |
| `aggregate_rating` | String / Double | `Double` | **No** | Weighted customer review rating (1.0 to 5.0) | `4.2` | **Quarantine** if `< 1.0` or `> 5.0`. Handle text `"NEW"`, `"-"` by marking as unrated (or quarantine based on pipeline policy). |
| `rating_text` | String | `String` | Yes | Categorical rating description | `"Very Good"` | Standardize: `"Excellent"`, `"Very Good"`, `"Good"`, `"Average"`, `"Poor"`, `"Not rated"`. |
| `votes` | String / Long | `Long` | Yes | Cumulative customer vote count | `775` | If null or negative, set to `0`. Must be `>= 0`. |
| `review_text` | String | `String` | Yes | Customer text review snippet | `"Great food and fast service!"` | Trimmed text; null allowed. Fed into AI sentiment and pgvector embeddings. |

---

## 2. Data Quality & Quarantine Policy

The ingestion and Spark ETL pipeline enforce strict **Data Quality Gates**. Records failing these rules are **not silently dropped**; they are tagged with an error code and written to the **Quarantine Zone** (`quarantine/restaurants/ingestion_date=YYYY-MM-DD/`) with metadata:

| Error Code | Error Description | Trigger Condition |
|------------|-------------------|-------------------|
| `ERR_MISSING_PK` | Missing or invalid restaurant identifier | `restaurant_id is null OR restaurant_id <= 0` |
| `ERR_MISSING_MANDATORY_FIELD` | Mandatory attribute missing | `name is null OR trim(name) == '' OR city is null OR trim(city) == ''` |
| `ERR_INVALID_RATING` | Rating out of physical bounds | `aggregate_rating < 1.0 OR aggregate_rating > 5.0 OR non-numeric string` |
| `ERR_INVALID_COST` | Cost for two is non-positive | `average_cost_for_two <= 0.0 OR unparseable numeric` |
| `ERR_DUPLICATE_RECORD` | Duplicate business identifier within batch | `count(restaurant_id) > 1` (latest or highest-voted record retained) |

---

## 3. Dimensional Analytical Model (Star Schema)

For Snowflake DWH and PostgreSQL analytical querying, data is structured into a clean Star Schema:

```
                    +---------------------------+
                    |       dim_location        |
                    +---------------------------+
                    | location_id (PK)          |
                    | city                      |
                    | locality                  |
                    +-------------+-------------+
                                  |
                                  | 1:N
                                  v
+------------------------+  +-------------------------------+  +------------------------+
|     dim_restaurant     |  |     fact_restaurant_metrics   |  |        dim_date        |
+------------------------+  +-------------------------------+  +------------------------+
| restaurant_key (PK)    |  | metric_id (PK)                |  | date_key (PK)          |
| restaurant_id (Natural)|<-| restaurant_key (FK)           |->| full_date              |
| name                   |  | location_key (FK)             |  | year                   |
| address                |  | date_key (FK)                 |  | month                  |
| price_tier             |  | average_cost_for_two          |  | day                    |
| has_online_delivery    |  | aggregate_rating              |  | quarter                |
| has_table_booking      |  | total_votes                   |  | is_weekend             |
| is_active              |  | review_count                  |  +------------------------+
+------------------------+  | pipeline_run_id               |
                            +---------------+---------------+
                                            ^
                                            | 1:N
                                            |
                            +---------------+---------------+
                            |       fact_review_events      |
                            +-------------------------------+
                            | review_id (PK)                |
                            | restaurant_key (FK)           |
                            | event_timestamp               |
                            | rating                        |
                            | sentiment_score (-1.0 to 1.0) |
                            | sentiment_label               |
                            | review_text                   |
                            | embedding_vector (pgvector)   |
                            +-------------------------------+
```

### Table Definitions:

### `dim_restaurant` (Slowly Changing Dimension Type 1 / 2)
- `restaurant_key` (BIGINT, Primary Key, Identity)
- `restaurant_id` (BIGINT, Natural Key)
- `name` (VARCHAR(255))
- `address` (VARCHAR(500))
- `price_tier` (INT)
- `has_online_delivery` (BOOLEAN)
- `has_table_booking` (BOOLEAN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### `dim_location`
- `location_key` (BIGINT, Primary Key)
- `city` (VARCHAR(100))
- `locality` (VARCHAR(150))

### `dim_cuisine`
- `cuisine_key` (BIGINT, Primary Key)
- `cuisine_name` (VARCHAR(100))

### `fact_restaurant_metrics`
- `metric_id` (BIGINT, Primary Key)
- `restaurant_key` (BIGINT, FK -> `dim_restaurant`)
- `location_key` (BIGINT, FK -> `dim_location`)
- `date_key` (INT, FK -> `dim_date`)
- `average_cost_for_two` (DECIMAL(10, 2))
- `aggregate_rating` (DECIMAL(3, 2))
- `total_votes` (BIGINT)
- `pipeline_run_id` (VARCHAR(64))
- `ingestion_timestamp` (TIMESTAMP)

### `fact_review_events` (Streaming & AI Vector Table)
- `review_id` (VARCHAR(64), Primary Key)
- `restaurant_id` (BIGINT)
- `event_timestamp` (TIMESTAMP)
- `rating` (DECIMAL(3,2))
- `sentiment_label` (VARCHAR(20)) -- POSITIVE, NEUTRAL, NEGATIVE
- `sentiment_score` (DECIMAL(4,3))
- `extracted_topics` (VARCHAR(255))
- `review_text` (TEXT)
- `embedding` (VECTOR(768) or VECTOR(1536))
