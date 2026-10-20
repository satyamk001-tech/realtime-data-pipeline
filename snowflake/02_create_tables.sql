-- =============================================================================
-- Snowflake Table Definitions & File Formats (RAW Bronze Layer)
-- =============================================================================

USE DATABASE ZOMATO_DWH;
USE SCHEMA RAW;

-- Parquet File Format
CREATE OR REPLACE FILE FORMAT PARQUET_FORMAT
    TYPE = 'PARQUET'
    COMPRESSION = 'SNAPPY';

-- CSV File Format
CREATE OR REPLACE FILE FORMAT CSV_FORMAT
    TYPE = 'CSV'
    FIELD_DELIMITER = ','
    SKIP_HEADER = 1
    FIELD_OPTIONALLY_ENCLOSED_BY = '"'
    NULL_IF = ('', 'NULL', 'null')
    EMPTY_FIELD_AS_NULL = TRUE;

-- Raw Staging Table for Restaurants
CREATE OR REPLACE TABLE RESTAURANTS_RAW (
    restaurant_id NUMBER(18, 0),
    name VARCHAR(255),
    city VARCHAR(100),
    address VARCHAR(500),
    locality VARCHAR(150),
    cuisines VARCHAR(255),
    average_cost_for_two NUMBER(10, 2),
    currency VARCHAR(10),
    has_online_delivery BOOLEAN,
    has_table_booking BOOLEAN,
    price_range NUMBER(2, 0),
    aggregate_rating NUMBER(3, 2),
    rating_text VARCHAR(50),
    votes NUMBER(10, 0),
    review_text VARCHAR,
    pipeline_run_id VARCHAR(64),
    loaded_at TIMESTAMP_NTZ DEFAULT CURRENT_TIMESTAMP()
);

-- Raw Staging Table for Reviews
CREATE OR REPLACE TABLE REVIEWS_RAW (
    review_id VARCHAR(64),
    restaurant_id NUMBER(18, 0),
    rating NUMBER(3, 2),
    review_text VARCHAR,
    event_timestamp TIMESTAMP_NTZ,
    source VARCHAR(50),
    pipeline_run_id VARCHAR(64),
    loaded_at TIMESTAMP_NTZ DEFAULT CURRENT_TIMESTAMP()
);
