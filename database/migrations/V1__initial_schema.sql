-- =============================================================================
-- Flyway Migration V1: Core Operational Schema & Pipeline Tracking
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Seed default administrative and viewer roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_ANALYST'), ('ROLE_USER')
ON CONFLICT (name) DO NOTHING;

-- Pipeline Job Management Tables
CREATE TABLE IF NOT EXISTS pipeline_jobs (
    id VARCHAR(64) PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    pipeline_type VARCHAR(50) NOT NULL, -- BATCH_ETL, STREAMING_INGESTION, AI_ENRICHMENT
    status VARCHAR(30) NOT NULL,        -- PENDING, RUNNING, SUCCEEDED, FAILED, CANCELLED
    source_path VARCHAR(500),
    target_path VARCHAR(500),
    input_records BIGINT DEFAULT 0,
    valid_records BIGINT DEFAULT 0,
    quarantined_records BIGINT DEFAULT 0,
    duplicate_records BIGINT DEFAULT 0,
    duration_ms BIGINT DEFAULT 0,
    triggered_by VARCHAR(100) DEFAULT 'system',
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_pipeline_jobs_status ON pipeline_jobs(status);
CREATE INDEX IF NOT EXISTS idx_pipeline_jobs_started_at ON pipeline_jobs(started_at DESC);

-- Job Execution Attempts (For Retries and Auditability)
CREATE TABLE IF NOT EXISTS pipeline_job_attempts (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(64) NOT NULL REFERENCES pipeline_jobs(id) ON DELETE CASCADE,
    attempt_number INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_job_attempts_job_id ON pipeline_job_attempts(job_id);

-- Operational Restaurants Cache (Used for Fast Sub-50ms REST API Lookups)
CREATE TABLE IF NOT EXISTS restaurants (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    locality VARCHAR(150),
    address VARCHAR(500),
    cuisines VARCHAR(255),
    average_cost_for_two NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    has_online_delivery BOOLEAN DEFAULT FALSE,
    has_table_booking BOOLEAN DEFAULT FALSE,
    price_range INT DEFAULT 1,
    aggregate_rating NUMERIC(3, 2) NOT NULL,
    rating_text VARCHAR(50),
    votes BIGINT DEFAULT 0,
    pipeline_run_id VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_restaurants_city ON restaurants(city);
CREATE INDEX IF NOT EXISTS idx_restaurants_rating ON restaurants(aggregate_rating DESC);
CREATE INDEX IF NOT EXISTS idx_restaurants_cost ON restaurants(average_cost_for_two);

-- Application Audit Logs
CREATE TABLE IF NOT EXISTS application_audit_events (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    ip_address VARCHAR(50),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
