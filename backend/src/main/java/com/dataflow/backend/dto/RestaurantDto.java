package com.dataflow.backend.dto;

import java.math.BigDecimal;

public class RestaurantDto {
    private Long id;
    private String name;
    private String city;
    private String locality;
    private String address;
    private String cuisines;
    private BigDecimal averageCostForTwo;
    private String currency;
    private Boolean hasOnlineDelivery;
    private Boolean hasTableBooking;
    private Integer priceRange;
    private BigDecimal aggregateRating;
    private String ratingText;
    private Long votes;
    private String pipelineRunId;

    public RestaurantDto() {}

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
}
