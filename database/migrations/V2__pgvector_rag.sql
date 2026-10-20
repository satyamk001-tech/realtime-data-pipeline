-- =============================================================================
-- Flyway Migration V2: pgvector Extension & Semantic Review Embeddings
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS restaurant_reviews (
    id VARCHAR(64) PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    restaurant_name VARCHAR(255),
    city VARCHAR(100),
    rating NUMERIC(3, 2),
    review_text TEXT NOT NULL,
    sentiment_label VARCHAR(20),     -- POSITIVE, NEUTRAL, NEGATIVE
    sentiment_score NUMERIC(4, 3),    -- -1.000 to +1.000
    mentioned_topics VARCHAR(255),   -- e.g. "service, pasta, delivery"
    embedding vector(384),           -- 384-dim for MiniLM / all-MiniLM-L6-v2 embeddings
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reviews_restaurant_id ON restaurant_reviews(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_reviews_sentiment ON restaurant_reviews(sentiment_label);

-- Create HNSW Index for sub-millisecond approximate nearest neighbor (ANN) cosine similarity
CREATE INDEX IF NOT EXISTS idx_reviews_embedding_hnsw 
ON restaurant_reviews 
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);
