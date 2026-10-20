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
