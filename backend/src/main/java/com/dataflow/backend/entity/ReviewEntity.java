package com.dataflow.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "restaurant_reviews")
public class ReviewEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "restaurant_name", length = 255)
    private String restaurantName;

    @Column(length = 100)
    private String city;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "review_text", nullable = false, columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "sentiment_label", length = 20)
    private String sentimentLabel;

    @Column(name = "sentiment_score", precision = 4, scale = 3)
    private BigDecimal sentimentScore;

    @Column(name = "mentioned_topics", length = 255)
    private String mentionedTopics;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ReviewEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public String getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(String sentimentLabel) { this.sentimentLabel = sentimentLabel; }
    public BigDecimal getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(BigDecimal sentimentScore) { this.sentimentScore = sentimentScore; }
    public String getMentionedTopics() { return mentionedTopics; }
    public void setMentionedTopics(String mentionedTopics) { this.mentionedTopics = mentionedTopics; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
