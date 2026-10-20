{{ config(materialized='view') }}

WITH source_data AS (
    SELECT * FROM {{ source('raw_dataflow', 'RESTAURANTS_RAW') }}
)

SELECT
    restaurant_id::BIGINT AS restaurant_id,
    TRIM(name)::VARCHAR(255) AS restaurant_name,
    INITCAP(TRIM(city))::VARCHAR(100) AS city,
    COALESCE(TRIM(address), 'Unspecified')::VARCHAR(500) AS address,
    COALESCE(TRIM(locality), 'Unspecified')::VARCHAR(150) AS locality,
    COALESCE(TRIM(cuisines), 'Uncategorized')::VARCHAR(255) AS cuisines,
    average_cost_for_two::DECIMAL(10, 2) AS average_cost_for_two,
    COALESCE(currency, 'INR')::VARCHAR(10) AS currency,
    COALESCE(has_online_delivery, FALSE)::BOOLEAN AS has_online_delivery,
    COALESCE(has_table_booking, FALSE)::BOOLEAN AS has_table_booking,
    COALESCE(price_range, 1)::INT AS price_tier,
    aggregate_rating::DECIMAL(3, 2) AS aggregate_rating,
    COALESCE(TRIM(rating_text), 'Not rated')::VARCHAR(50) AS rating_text,
    COALESCE(votes, 0)::BIGINT AS votes,
    pipeline_run_id::VARCHAR(64) AS pipeline_run_id,
    loaded_at::TIMESTAMP_NTZ AS ingested_at
FROM source_data
WHERE restaurant_id IS NOT NULL
  AND restaurant_id > 0
  AND aggregate_rating BETWEEN 1.0 AND 5.0
  AND average_cost_for_two > 0.0
