package com.dataflow.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "restaurants")
public class RestaurantEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 150)
    private String locality;

    @Column(length = 500)
    private String address;

    @Column(length = 255)
    private String cuisines;

    @Column(name = "average_cost_for_two", nullable = false, precision = 10, scale = 2)
    private BigDecimal averageCostForTwo;

    @Column(length = 10)
    private String currency;

    @Column(name = "has_online_delivery")
    private Boolean hasOnlineDelivery;

    @Column(name = "has_table_booking")
    private Boolean hasTableBooking;

    @Column(name = "price_range")
    private Integer priceRange;

    @Column(name = "aggregate_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal aggregateRating;

    @Column(name = "rating_text", length = 50)
    private String ratingText;

    private Long votes;

    @Column(name = "pipeline_run_id", length = 64)
    private String pipelineRunId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public RestaurantEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCuisines() { return cuisines; }
    public void setCuisines(String cuisines) { this.cuisines = cuisines; }
    public BigDecimal getAverageCostForTwo() { return averageCostForTwo; }
    public void setAverageCostForTwo(BigDecimal averageCostForTwo) { this.averageCostForTwo = averageCostForTwo; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Boolean getHasOnlineDelivery() { return hasOnlineDelivery; }
    public void setHasOnlineDelivery(Boolean hasOnlineDelivery) { this.hasOnlineDelivery = hasOnlineDelivery; }
    public Boolean getHasTableBooking() { return hasTableBooking; }
    public void setHasTableBooking(Boolean hasTableBooking) { this.hasTableBooking = hasTableBooking; }
    public Integer getPriceRange() { return priceRange; }
    public void setPriceRange(Integer priceRange) { this.priceRange = priceRange; }
    public BigDecimal getAggregateRating() { return aggregateRating; }
    public void setAggregateRating(BigDecimal aggregateRating) { this.aggregateRating = aggregateRating; }
    public String getRatingText() { return ratingText; }
    public void setRatingText(String ratingText) { this.ratingText = ratingText; }
    public Long getVotes() { return votes; }
    public void setVotes(Long votes) { this.votes = votes; }
    public String getPipelineRunId() { return pipelineRunId; }
    public void setPipelineRunId(String pipelineRunId) { this.pipelineRunId = pipelineRunId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
