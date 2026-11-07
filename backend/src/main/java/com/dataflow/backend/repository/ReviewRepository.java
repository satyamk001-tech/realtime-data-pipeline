package com.dataflow.backend.repository;

import com.dataflow.backend.entity.ReviewEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, String> {

    List<ReviewEntity> findByRestaurantId(Long restaurantId);

    /**
     * Executes genuine Approximate Nearest Neighbor (ANN) cosine similarity search
     * utilizing PostgreSQL 16 + pgvector HNSW index.
     * Distance operator <=> computes cosine distance; (1 - distance) yields cosine similarity.
     */
    @Query(value = """
        SELECT id, restaurant_id, restaurant_name, city, rating, review_text,
               sentiment_label, sentiment_score, mentioned_topics, created_at,
               (1 - (embedding <=> cast(:queryVector as vector))) AS similarity_score
        FROM restaurant_reviews
        WHERE embedding IS NOT NULL
        ORDER BY embedding <=> cast(:queryVector as vector) ASC
        LIMIT :topK
        """, nativeQuery = true)
    List<Object[]> findNearestReviewsByVector(
            @Param("queryVector") String queryVector,
            @Param("topK") int topK
    );

    /**
     * Resilient keyword fallback for cold-start queries when embedding vector is unavailable.
     */
    @Query("SELECT r FROM ReviewEntity r WHERE " +
           "LOWER(r.reviewText) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(r.city) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<ReviewEntity> searchByKeyword(@Param("term") String term, Pageable pageable);

    @Query(value = "SELECT id, restaurant_id, restaurant_name, city, rating, review_text, " +
                   "sentiment_label, sentiment_score, mentioned_topics, created_at " +
                   "FROM restaurant_reviews " +
                   "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<ReviewEntity> findSampleReviews(@Param("limit") int limit);
}
