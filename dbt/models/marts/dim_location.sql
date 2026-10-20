{{ config(materialized='table') }}

WITH distinct_locations AS (
    SELECT DISTINCT
        city,
        locality
    FROM {{ ref('stg_restaurants') }}
)

SELECT
    MD5(CONCAT(city, '||', locality)) AS location_key,
    city,
    locality
FROM distinct_locations
