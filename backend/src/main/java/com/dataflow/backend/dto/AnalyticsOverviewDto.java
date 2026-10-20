package com.dataflow.backend.dto;

public class AnalyticsOverviewDto {
    private long totalRestaurants;
    private double overallAverageRating;
    private double overallAverageCost;
    private long totalVotes;
    private long onlineDeliveryCount;
    private double onlineDeliveryPercentage;
    private long tableBookingCount;
    private double tableBookingPercentage;

    public AnalyticsOverviewDto() {}

    public long getTotalRestaurants() { return totalRestaurants; }
    public void setTotalRestaurants(long totalRestaurants) { this.totalRestaurants = totalRestaurants; }
    public double getOverallAverageRating() { return overallAverageRating; }
    public void setOverallAverageRating(double overallAverageRating) { this.overallAverageRating = overallAverageRating; }
    public double getOverallAverageCost() { return overallAverageCost; }
    public void setOverallAverageCost(double overallAverageCost) { this.overallAverageCost = overallAverageCost; }
    public long getTotalVotes() { return totalVotes; }
    public void setTotalVotes(long totalVotes) { this.totalVotes = totalVotes; }
    public long getOnlineDeliveryCount() { return onlineDeliveryCount; }
    public void setOnlineDeliveryCount(long onlineDeliveryCount) { this.onlineDeliveryCount = onlineDeliveryCount; }
    public double getOnlineDeliveryPercentage() { return onlineDeliveryPercentage; }
    public void setOnlineDeliveryPercentage(double onlineDeliveryPercentage) { this.onlineDeliveryPercentage = onlineDeliveryPercentage; }
    public long getTableBookingCount() { return tableBookingCount; }
    public void setTableBookingCount(long tableBookingCount) { this.tableBookingCount = tableBookingCount; }
    public double getTableBookingPercentage() { return tableBookingPercentage; }
    public void setTableBookingPercentage(double tableBookingPercentage) { this.tableBookingPercentage = tableBookingPercentage; }
}
