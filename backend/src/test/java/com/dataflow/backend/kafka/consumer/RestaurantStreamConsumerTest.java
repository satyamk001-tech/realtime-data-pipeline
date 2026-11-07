package com.dataflow.backend.kafka.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RestaurantStreamConsumerTest {

    private KafkaOperations<String, Object> kafkaOperations;
    private RestaurantStreamConsumer consumer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaOperations = (KafkaOperations<String, Object>) Mockito.mock(KafkaOperations.class);
        consumer = new RestaurantStreamConsumer(kafkaOperations);
    }

    @Test
    void shouldProcessValidJsonPayloadSuccessfully() {
        String validJson = "{\"restaurantId\": 1234, \"rating\": 4.5, \"review\": \"Superb dining experience!\"}";

        consumer.consumeReviewEvent(validJson, "key-1234", 0, 100L);

        assertThat(consumer.getProcessedCount()).isEqualTo(1L);
        assertThat(consumer.getDlqCount()).isEqualTo(0L);
        verify(kafkaOperations, never()).send(any(), any(), any());
    }

    @Test
    void shouldRouteMalformedPayloadToDeadLetterQueue() {
        String corruptedPayload = "CORRUPTED_NON_JSON_DATA_STREAM";

        consumer.consumeReviewEvent(corruptedPayload, "key-bad", 0, 101L);

        assertThat(consumer.getDlqCount()).isEqualTo(1L);
        verify(kafkaOperations, times(1)).send(any(), eq("key-bad"), any());
    }

    @Test
    void shouldThrowExceptionWhenPayloadIsEmpty() {
        assertThatThrownBy(() -> consumer.processPayload("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payload cannot be empty");
    }
}
