package com.dataflow.backend.service;

import com.dataflow.backend.dto.RagQueryRequest;
import com.dataflow.backend.dto.RagQueryResponse;
import com.dataflow.backend.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class RagServiceTest {

    private ReviewRepository reviewRepository;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        reviewRepository = Mockito.mock(ReviewRepository.class);
        ragService = new RagService(reviewRepository);
    }

    @Test
    void shouldReturnNoticeWhenQueryIsEmpty() {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("   ");
        request.setTopK(5);

        RagQueryResponse response = ragService.queryReviews(request);

        assertThat(response.getAnswer()).contains("Query cannot be empty");
        assertThat(response.getSourceReviews()).isEmpty();
    }

    @Test
    void shouldExecuteVectorSearchAndSynthesizeEvidence() {
        Object[] row1 = new Object[]{
                "rev-1", 101L, "Empire Grill", "Bangalore",
                BigDecimal.valueOf(4.5), "Incredible food and fast delivery!", "POSITIVE",
                BigDecimal.valueOf(0.92), "food, delivery", null, 0.942
        };

        List<Object[]> results = new ArrayList<>();
        results.add(row1);

        when(reviewRepository.findNearestReviewsByVector(anyString(), anyInt()))
                .thenReturn(results);

        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("spicy chicken curry and quick delivery");
        request.setTopK(3);

        RagQueryResponse response = ragService.queryReviews(request);

        assertThat(response.getSourceReviews()).hasSize(1);
        assertThat(response.getSourceReviews().get(0).getRestaurantName()).isEqualTo("Empire Grill");
        assertThat(response.getSourceReviews().get(0).getSentiment()).isEqualTo("POSITIVE");
        assertThat(response.getAnswer()).contains("Grounded Retrieval");
        assertThat(response.getAnswer()).contains("positive endorsements");
    }

    @Test
    void shouldHandleEmptyVectorStoreResultsGracefully() {
        List<Object[]> emptyResults = Collections.emptyList();
        when(reviewRepository.findNearestReviewsByVector(anyString(), anyInt()))
                .thenReturn(emptyResults);

        RagQueryRequest request = new RagQueryRequest();
        request.setQuery("unheard exotic dish");
        request.setTopK(5);

        RagQueryResponse response = ragService.queryReviews(request);

        assertThat(response.getSourceReviews()).isEmpty();
        assertThat(response.getAnswer()).contains("No verified customer reviews match");
    }
}
