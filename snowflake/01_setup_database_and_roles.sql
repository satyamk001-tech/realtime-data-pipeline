-- =============================================================================
-- Snowflake Data Warehouse Initialization & Cost-Guard Configuration
-- =============================================================================

-- 1. Compute Warehouse Configuration (Size: X-Small, Auto-Suspend: 60s)
CREATE WAREHOUSE IF NOT EXISTS DATAFLOW_WH
    WITH WAREHOUSE_SIZE = 'X-SMALL'
    AUTO_SUSPEND = 60
    AUTO_RESUME = TRUE
    MIN_CLUSTER_COUNT = 1
    MAX_CLUSTER_COUNT = 1
    INITIALLY_SUSPENDED = TRUE
    COMMENT = 'DataFlow lightweight analytics warehouse with automated cost suspension';

-- 2. Analytical Database & Schema Hierarchy
CREATE DATABASE IF NOT EXISTS ZOMATO_DWH;

USE DATABASE ZOMATO_DWH;

-- Medallion Schemas
CREATE SCHEMA IF NOT EXISTS RAW COMMENT = 'Bronze: Raw ingested restaurant and review staging tables';
CREATE SCHEMA IF NOT EXISTS STAGING COMMENT = 'Silver: Cleansed, typed, and standardized dbt staging views';
CREATE SCHEMA IF NOT EXISTS MARTS COMMENT = 'Gold: Dimensional Star Schema models and business marts';

-- 3. Security Roles & Least-Privilege Access Controls
-- Role for ETL/dbt Write Access
CREATE ROLE IF NOT EXISTS DATAFLOW_ETL_ROLE;
GRANT USAGE ON WAREHOUSE DATAFLOW_WH TO ROLE DATAFLOW_ETL_ROLE;
GRANT USAGE ON DATABASE ZOMATO_DWH TO ROLE DATAFLOW_ETL_ROLE;
GRANT ALL ON SCHEMA RAW TO ROLE DATAFLOW_ETL_ROLE;
GRANT ALL ON SCHEMA STAGING TO ROLE DATAFLOW_ETL_ROLE;
GRANT ALL ON SCHEMA MARTS TO ROLE DATAFLOW_ETL_ROLE;

-- Role for Secure Text-to-SQL Read-Only Queries (Strictly restricted to MARTS)
CREATE ROLE IF NOT EXISTS DATAFLOW_ANALYTICS_RO;
GRANT USAGE ON WAREHOUSE DATAFLOW_WH TO ROLE DATAFLOW_ANALYTICS_RO;
GRANT USAGE ON DATABASE ZOMATO_DWH TO ROLE DATAFLOW_ANALYTICS_RO;
GRANT USAGE ON SCHEMA MARTS TO ROLE DATAFLOW_ANALYTICS_RO;
GRANT SELECT ON ALL TABLES IN SCHEMA MARTS TO ROLE DATAFLOW_ANALYTICS_RO;
GRANT SELECT ON ALL VIEWS IN SCHEMA MARTS TO ROLE DATAFLOW_ANALYTICS_RO;
GRANT SELECT ON FUTURE TABLES IN SCHEMA MARTS TO ROLE DATAFLOW_ANALYTICS_RO;
GRANT SELECT ON FUTURE VIEWS IN SCHEMA MARTS TO ROLE DATAFLOW_ANALYTICS_RO;
-- Note: NO permissions on RAW, STAGING, internal system tables, or transactional DBs
