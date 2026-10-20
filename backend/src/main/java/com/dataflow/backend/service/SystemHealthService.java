package com.dataflow.backend.service;

import com.dataflow.backend.dto.SystemHealthDto;
import com.dataflow.backend.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;

@Service
public class SystemHealthService {

    private final DataSource dataSource;
    private final RestaurantRepository restaurantRepository;

    public SystemHealthService(DataSource dataSource, RestaurantRepository restaurantRepository) {
        this.dataSource = dataSource;
        this.restaurantRepository = restaurantRepository;
    }

    public SystemHealthDto getHealth() {
        SystemHealthDto dto = new SystemHealthDto();
        dto.setStatus("UP");

        // Database Check
        try (Connection conn = dataSource.getConnection()) {
            dto.setDatabaseConnected(conn.isValid(2));
        } catch (Exception e) {
            dto.setDatabaseConnected(false);
            dto.setStatus("DEGRADED");
        }

        // MinIO & Kafka Check (Simulated local status)
        dto.setMinioConnected(true);
        dto.setKafkaConnected(true);

        try {
            dto.setTotalProcessedRestaurants(restaurantRepository.count());
        } catch (Exception e) {
            dto.setTotalProcessedRestaurants(0L);
        }

        // JVM Metrics
        Runtime runtime = Runtime.getRuntime();
        long usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        dto.setMemoryUsedMb(usedMem);
        dto.setMemoryMaxMb(maxMem);

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        dto.setUptimeSeconds(uptimeMs / 1000);

        return dto;
    }
}
