{{ config(materialized='view') }}

WITH source_reviews AS (
    SELECT * FROM {{ source('raw_dataflow', 'REVIEWS_RAW') }}
)

SELECT
    review_id::VARCHAR(64) AS review_id,
    restaurant_id::BIGINT AS restaurant_id,
    aggregate_rating::DECIMAL(3, 2) AS rating,
    TRIM(review_text)::VARCHAR AS review_text,
    COALESCE(event_timestamp, CURRENT_TIMESTAMP())::TIMESTAMP_NTZ AS review_timestamp,
    COALESCE(source, 'batch')::VARCHAR(50) AS event_source,
    pipeline_run_id::VARCHAR(64) AS pipeline_run_id,
    loaded_at::TIMESTAMP_NTZ AS ingested_at
FROM source_reviews
WHERE review_id IS NOT NULL
  AND restaurant_id IS NOT NULL
