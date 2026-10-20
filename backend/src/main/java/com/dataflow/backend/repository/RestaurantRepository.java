package com.dataflow.backend.repository;

import com.dataflow.backend.entity.RestaurantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantEntity, Long> {

    Page<RestaurantEntity> findByCityIgnoreCase(String city, Pageable pageable);

    @Query("SELECT r FROM RestaurantEntity r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.locality) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.cuisines) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<RestaurantEntity> searchRestaurants(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r.city AS city, COUNT(r) AS count, AVG(r.aggregateRating) AS avgRating, AVG(r.averageCostForTwo) AS avgCost " +
           "FROM RestaurantEntity r GROUP BY r.city ORDER BY COUNT(r) DESC")
    List<Object[]> getLocationAnalytics();

    @Query("SELECT r.cuisines AS cuisines, COUNT(r) AS count, AVG(r.aggregateRating) AS avgRating " +
           "FROM RestaurantEntity r GROUP BY r.cuisines ORDER BY COUNT(r) DESC")
    List<Object[]> getCuisineAnalytics();

    @Query("SELECT AVG(r.aggregateRating) FROM RestaurantEntity r")
    Double getOverallAverageRating();

    @Query("SELECT AVG(r.averageCostForTwo) FROM RestaurantEntity r")
    Double getOverallAverageCost();

    @Query("SELECT SUM(r.votes) FROM RestaurantEntity r")
    Long getTotalVotes();

    @Query("SELECT COUNT(r) FROM RestaurantEntity r WHERE r.hasOnlineDelivery = true")
    Long countOnlineDelivery();

    @Query("SELECT COUNT(r) FROM RestaurantEntity r WHERE r.hasTableBooking = true")
    Long countTableBooking();
}
