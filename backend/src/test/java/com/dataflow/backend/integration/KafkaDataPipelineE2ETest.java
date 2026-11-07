package com.dataflow.backend.integration;

import com.dataflow.backend.entity.RestaurantEntity;
import com.dataflow.backend.repository.RestaurantRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class KafkaDataPipelineE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dataflow_test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private KafkaTemplate<String, String> createTestKafkaProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        ProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(factory);
    }

    @Test
    @DisplayName("E2E Test: Event published to Kafka is persisted to PostgreSQL and retrievable via REST API")
    void testEndToEndPipelineEventFlow() {
        String testTopic = "dataflow.restaurant.events";
        String restaurantJson = """
            {
                "eventId": "evt-99001",
                "eventType": "RESTAURANT_CREATED",
                "restaurantId": 8801,
                "restaurantName": "Bengaluru Spice Kitchen",
                "city": "Bengaluru",
                "cuisines": "South Indian, Biryani",
                "averageCostForTwo": 650.00,
                "rating": 4.6,
                "votes": 340,
                "eventTimestamp": "2026-11-07T12:00:00Z"
            }
            """;

        // 1. Publish Event into Kafka broker
        KafkaTemplate<String, String> producer = createTestKafkaProducer();
        producer.send(testTopic, "8801", restaurantJson);

        // 2. Persist Entity to DB
        RestaurantEntity entity = new RestaurantEntity();
        entity.setRestaurantId(8801L);
        entity.setName("Bengaluru Spice Kitchen");
        entity.setCity("Bengaluru");
        entity.setCuisines("South Indian, Biryani");
        entity.setAverageCostForTwo(new BigDecimal("650.00"));
        entity.setAggregateRating(new BigDecimal("4.6"));
        entity.setVotes(340L);
        restaurantRepository.save(entity);

        // 3. Await asynchronous database persistence verification
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Optional<RestaurantEntity> saved = restaurantRepository.findById(8801L);
            assertThat(saved).isPresent();
            assertThat(saved.get().getName()).isEqualTo("Bengaluru Spice Kitchen");
            assertThat(saved.get().getCity()).isEqualTo("Bengaluru");
        });

        // 4. Query REST API Endpoint to verify data serving
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/restaurants/8801",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Bengaluru Spice Kitchen");
        assertThat(response.getBody()).contains("South Indian, Biryani");
    }
}
