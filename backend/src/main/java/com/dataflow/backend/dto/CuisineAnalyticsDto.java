package com.dataflow.backend.dto;

public class CuisineAnalyticsDto {
    private String cuisine;
    private long restaurantCount;
    private double averageRating;

    public CuisineAnalyticsDto() {}

    public CuisineAnalyticsDto(String cuisine, long restaurantCount, double averageRating) {
        this.cuisine = cuisine;
        this.restaurantCount = restaurantCount;
        this.averageRating = averageRating;
    }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public long getRestaurantCount() { return restaurantCount; }
    public void setRestaurantCount(long restaurantCount) { this.restaurantCount = restaurantCount; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}
