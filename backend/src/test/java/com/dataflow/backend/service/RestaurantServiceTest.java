package com.dataflow.backend.service;

import com.dataflow.backend.dto.RestaurantDto;
import com.dataflow.backend.entity.RestaurantEntity;
import com.dataflow.backend.exception.ResourceNotFoundException;
import com.dataflow.backend.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RestaurantServiceTest {

    private RestaurantRepository restaurantRepository;
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantRepository = Mockito.mock(RestaurantRepository.class);
        restaurantService = new RestaurantService(restaurantRepository);
    }

    @Test
    void shouldReturnRestaurantByIdWhenExists() {
        RestaurantEntity entity = new RestaurantEntity();
        entity.setId(101L);
        entity.setName("The Royal Curry");
        entity.setCity("New Delhi");
        entity.setAggregateRating(BigDecimal.valueOf(4.6));

        when(restaurantRepository.findById(101L)).thenReturn(Optional.of(entity));

        RestaurantDto dto = restaurantService.getRestaurantById(101L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(101L);
        assertThat(dto.getName()).isEqualTo("The Royal Curry");
        assertThat(dto.getCity()).isEqualTo("New Delhi");
    }

    @Test
    void shouldThrowExceptionWhenRestaurantNotFound() {
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.getRestaurantById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant not found with id: 999");
    }

    @Test
    void shouldSearchRestaurantsByKeyword() {
        RestaurantEntity entity = new RestaurantEntity();
        entity.setId(202L);
        entity.setName("Pizza Palace");
        entity.setCity("Mumbai");
        entity.setAggregateRating(BigDecimal.valueOf(4.2));

        Page<RestaurantEntity> page = new PageImpl<>(List.of(entity));
        Pageable pageable = PageRequest.of(0, 10);

        when(restaurantRepository.searchRestaurants(eq("pizza"), any(Pageable.class))).thenReturn(page);

        Page<RestaurantDto> result = restaurantService.getRestaurants("pizza", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Pizza Palace");
    }
}
