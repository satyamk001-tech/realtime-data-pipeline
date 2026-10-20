package com.dataflow.backend.mapper;

import com.dataflow.backend.dto.RestaurantDto;
import com.dataflow.backend.entity.RestaurantEntity;

public class RestaurantMapper {

    public static RestaurantDto toDto(RestaurantEntity entity) {
        if (entity == null) return null;
        RestaurantDto dto = new RestaurantDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCity(entity.getCity());
        dto.setLocality(entity.getLocality());
        dto.setAddress(entity.getAddress());
        dto.setCuisines(entity.getCuisines());
        dto.setAverageCostForTwo(entity.getAverageCostForTwo());
        dto.setCurrency(entity.getCurrency());
        dto.setHasOnlineDelivery(entity.getHasOnlineDelivery());
        dto.setHasTableBooking(entity.getHasTableBooking());
        dto.setPriceRange(entity.getPriceRange());
        dto.setAggregateRating(entity.getAggregateRating());
        dto.setRatingText(entity.getRatingText());
        dto.setVotes(entity.getVotes());
        dto.setPipelineRunId(entity.getPipelineRunId());
        return dto;
    }
}
