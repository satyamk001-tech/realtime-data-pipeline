package com.dataflow.backend.service;

import com.dataflow.backend.dto.AnalyticsOverviewDto;
import com.dataflow.backend.dto.CuisineAnalyticsDto;
import com.dataflow.backend.dto.LocationAnalyticsDto;
import com.dataflow.backend.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final RestaurantRepository restaurantRepository;

    public AnalyticsService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsOverviewDto getOverview() {
        long total = restaurantRepository.count();
        Double avgRating = restaurantRepository.getOverallAverageRating();
        Double avgCost = restaurantRepository.getOverallAverageCost();
        Long totalVotes = restaurantRepository.getTotalVotes();
        Long onlineCount = restaurantRepository.countOnlineDelivery();
        Long bookingCount = restaurantRepository.countTableBooking();

        AnalyticsOverviewDto dto = new AnalyticsOverviewDto();
        dto.setTotalRestaurants(total);
        dto.setOverallAverageRating(avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0);
        dto.setOverallAverageCost(avgCost != null ? Math.round(avgCost * 100.0) / 100.0 : 0.0);
        dto.setTotalVotes(totalVotes != null ? totalVotes : 0L);
        dto.setOnlineDeliveryCount(onlineCount != null ? onlineCount : 0L);
        dto.setOnlineDeliveryPercentage(total > 0 && onlineCount != null ? Math.round((onlineCount * 100.0 / total) * 10.0) / 10.0 : 0.0);
        dto.setTableBookingCount(bookingCount != null ? bookingCount : 0L);
        dto.setTableBookingPercentage(total > 0 && bookingCount != null ? Math.round((bookingCount * 100.0 / total) * 10.0) / 10.0 : 0.0);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<LocationAnalyticsDto> getLocationAnalytics() {
        List<Object[]> rows = restaurantRepository.getLocationAnalytics();
        List<LocationAnalyticsDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            String city = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            Double avgRating = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            Double avgCost = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            result.add(new LocationAnalyticsDto(city, count, Math.round(avgRating * 10.0) / 10.0, Math.round(avgCost)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<CuisineAnalyticsDto> getCuisineAnalytics() {
        List<Object[]> rows = restaurantRepository.getCuisineAnalytics();
        List<CuisineAnalyticsDto> result = new ArrayList<>();
        int limit = 10;
        for (Object[] row : rows) {
            if (limit-- <= 0) break;
            String cuisine = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            Double avgRating = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            result.add(new CuisineAnalyticsDto(cuisine, count, Math.round(avgRating * 10.0) / 10.0));
        }
        return result;
    }
}
