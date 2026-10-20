{{ config(materialized='table') }}

WITH stg AS (
    SELECT * FROM {{ ref('stg_restaurants') }}
)

SELECT
    MD5(CONCAT(stg.restaurant_id::VARCHAR, '||', stg.pipeline_run_id)) AS metric_id,
    MD5(stg.restaurant_id::VARCHAR) AS restaurant_key,
    MD5(CONCAT(stg.city, '||', stg.locality)) AS location_key,
    stg.restaurant_id,
    stg.average_cost_for_two,
    stg.aggregate_rating,
    stg.votes,
    stg.price_tier,
    stg.has_online_delivery,
    stg.has_table_booking,
    stg.pipeline_run_id,
    stg.ingested_at
FROM stg
