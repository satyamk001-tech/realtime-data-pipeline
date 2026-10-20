package com.dataflow.backend.service;

import com.dataflow.backend.dto.RestaurantDto;
import com.dataflow.backend.entity.RestaurantEntity;
import com.dataflow.backend.exception.ResourceNotFoundException;
import com.dataflow.backend.mapper.RestaurantMapper;
import com.dataflow.backend.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public Page<RestaurantDto> getRestaurants(String keyword, String city, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return restaurantRepository.searchRestaurants(keyword.trim(), pageable)
                    .map(RestaurantMapper::toDto);
        }
        if (city != null && !city.trim().isEmpty()) {
            return restaurantRepository.findByCityIgnoreCase(city.trim(), pageable)
                    .map(RestaurantMapper::toDto);
        }
        return restaurantRepository.findAll(pageable)
                .map(RestaurantMapper::toDto);
    }

    @Transactional(readOnly = true)
    public RestaurantDto getRestaurantById(Long id) {
        RestaurantEntity entity = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return RestaurantMapper.toDto(entity);
    }
}
