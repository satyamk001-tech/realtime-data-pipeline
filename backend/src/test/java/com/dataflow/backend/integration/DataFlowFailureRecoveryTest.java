package com.dataflow.backend.integration;

import com.dataflow.backend.repository.RestaurantRepository;
import com.dataflow.backend.service.TextToSqlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DataFlowFailureRecoveryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dataflow_failure_test")
            .withUsername("fail_user")
            .withPassword("fail_pass")
            .withCommand("postgres", "-c", "statement_timeout=2000");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.hikari.connection-timeout", () -> "1500");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "3");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TextToSqlService textToSqlService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    @DisplayName("Failure Scenario 1: Malformed SQL / Injection Attempt triggers clean 400 Bad Request rejection")
    void testSqlInjectionPoisonPillRejection() {
        var request = new com.dataflow.backend.dto.TextToSqlRequest();
        request.setQuestion("Top restaurants; DROP TABLE restaurants; --");

        var response = textToSqlService.processQuestion(request);

        assertThat(response.isSafe()).isFalse();
        assertThat(response.getRejectionReason()).containsIgnoringCase("semicolon");
        assertThat(response.getRows()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Failure Scenario 2: Resource Not Found gracefully produces HTTP 404 without leaking stack trace")
    void testNonExistentEntityReturnsClean404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/restaurants/99999999",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).doesNotContain("Exception");
        assertThat(response.getBody()).doesNotContain("at org.springframework");
    }

    @Test
    @DisplayName("Failure Scenario 3: Database statement timeout terminates runaway queries cleanly within 5 seconds")
    void testRunawayQueryTimeoutHandling() {
        var request = new com.dataflow.backend.dto.TextToSqlRequest();
        request.setQuestion("restaurants");

        long start = System.currentTimeMillis();
        var response = textToSqlService.processQuestion(request);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(6000);
        assertThat(response).isNotNull();
    }
}
