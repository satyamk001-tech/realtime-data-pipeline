{{ config(materialized='table') }}

WITH split_cuisines AS (
    SELECT
        restaurant_id,
        TRIM(value::VARCHAR) AS cuisine_name
    FROM {{ ref('stg_restaurants') }},
    LATERAL FLATTEN(input => SPLIT(cuisines, ','))
)

SELECT DISTINCT
    MD5(UPPER(cuisine_name)) AS cuisine_key,
    cuisine_name
FROM split_cuisines
WHERE cuisine_name IS NOT NULL AND cuisine_name != ''
