package com.dataflow.backend.dto;

public class LocationAnalyticsDto {
    private String city;
    private long restaurantCount;
    private double averageRating;
    private double averageCost;

    public LocationAnalyticsDto() {}

    public LocationAnalyticsDto(String city, long restaurantCount, double averageRating, double averageCost) {
        this.city = city;
        this.restaurantCount = restaurantCount;
        this.averageRating = averageRating;
        this.averageCost = averageCost;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public long getRestaurantCount() { return restaurantCount; }
    public void setRestaurantCount(long restaurantCount) { this.restaurantCount = restaurantCount; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public double getAverageCost() { return averageCost; }
    public void setAverageCost(double averageCost) { this.averageCost = averageCost; }
}
