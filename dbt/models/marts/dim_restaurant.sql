{{ config(materialized='table') }}

WITH ranked_restaurants AS (
    SELECT
        restaurant_id,
        restaurant_name,
        address,
        price_tier,
        has_online_delivery,
        has_table_booking,
        ingested_at,
        ROW_NUMBER() OVER (
            PARTITION BY restaurant_id 
            ORDER BY votes DESC, ingested_at DESC
        ) AS rank_num
    FROM {{ ref('stg_restaurants') }}
)

SELECT
    MD5(restaurant_id::VARCHAR) AS restaurant_key,
    restaurant_id,
    restaurant_name,
    address,
    price_tier,
    has_online_delivery,
    has_table_booking,
    TRUE AS is_active,
    ingested_at AS valid_from,
    NULL::TIMESTAMP_NTZ AS valid_to
FROM ranked_restaurants
WHERE rank_num = 1
