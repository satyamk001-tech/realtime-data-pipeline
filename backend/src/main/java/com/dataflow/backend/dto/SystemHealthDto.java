package com.dataflow.backend.dto;

public class SystemHealthDto {
    private String status;
    private boolean databaseConnected;
    private boolean kafkaConnected;
    private boolean minioConnected;
    private long totalProcessedRestaurants;
    private long memoryUsedMb;
    private long memoryMaxMb;
    private long uptimeSeconds;

    public SystemHealthDto() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isDatabaseConnected() { return databaseConnected; }
    public void setDatabaseConnected(boolean databaseConnected) { this.databaseConnected = databaseConnected; }
    public boolean isKafkaConnected() { return kafkaConnected; }
    public void setKafkaConnected(boolean kafkaConnected) { this.kafkaConnected = kafkaConnected; }
    public boolean isMinioConnected() { return minioConnected; }
    public void setMinioConnected(boolean minioConnected) { this.minioConnected = minioConnected; }
    public long getTotalProcessedRestaurants() { return totalProcessedRestaurants; }
    public void setTotalProcessedRestaurants(long totalProcessedRestaurants) { this.totalProcessedRestaurants = totalProcessedRestaurants; }
    public long getMemoryUsedMb() { return memoryUsedMb; }
    public void setMemoryUsedMb(long memoryUsedMb) { this.memoryUsedMb = memoryUsedMb; }
    public long getMemoryMaxMb() { return memoryMaxMb; }
    public void setMemoryMaxMb(long memoryMaxMb) { this.memoryMaxMb = memoryMaxMb; }
    public long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(long uptimeSeconds) { this.uptimeSeconds = uptimeSeconds; }
}
