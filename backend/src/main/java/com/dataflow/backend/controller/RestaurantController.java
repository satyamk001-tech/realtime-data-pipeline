package com.dataflow.backend.controller;

import com.dataflow.backend.dto.RestaurantDto;
import com.dataflow.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restaurants")
@Tag(name = "Restaurants", description = "Restaurant catalog discovery and query APIs")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    @Operation(summary = "Search and filter restaurants with pagination")
    public ResponseEntity<Page<RestaurantDto>> getRestaurants(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "aggregateRating") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {

        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<RestaurantDto> result = restaurantService.getRestaurants(keyword, city, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant profile by ID")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }
}
