package com.dataflow.backend.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * High-throughput Kafka Stream Consumer with Dead Letter Queue (DLQ) error recovery.
 * Processes real-time restaurant reviews and incoming data events.
 */
@Service
public class RestaurantStreamConsumer {

    private static final Logger log = LoggerFactory.getLogger(RestaurantStreamConsumer.class);

    private final KafkaOperations<String, Object> kafkaOperations;
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong dlqCount = new AtomicLong(0);

    @Value("${dataflow.kafka.topics.dlq:dataflow.restaurant.dlq}")
    private String dlqTopic;

    @Autowired(required = false)
    public RestaurantStreamConsumer(KafkaOperations<String, Object> kafkaOperations) {
        this.kafkaOperations = kafkaOperations;
    }

    @KafkaListener(
            topics = "${dataflow.kafka.topics.reviews:dataflow.restaurant.reviews}",
            groupId = "${spring.kafka.consumer.group-id:dataflow-analytics-group}",
            autoStartup = "${dataflow.kafka.consumer.enabled:false}"
    )
    public void consumeReviewEvent(@Payload String payload,
                                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                   @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
                                   @Header(value = KafkaHeaders.OFFSET, required = false) Long offset) {
        log.debug("Received event from partition {} at offset {}: key={}", partition, offset, key);

        try {
            processPayload(payload);
            processedCount.incrementAndGet();
        } catch (Exception ex) {
            log.warn("Corrupted or malformed event detected. Routing to Dead Letter Queue [{}]: {}", dlqTopic, ex.getMessage());
            dlqCount.incrementAndGet();
            routeToDlq(key, payload, ex.getMessage());
        }
    }

    public void processPayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be empty");
        }
        if (!payload.contains("{") || !payload.contains("}")) {
            throw new IllegalArgumentException("Payload does not conform to valid JSON format");
        }
    }

    public void routeToDlq(String key, String payload, String reason) {
        if (kafkaOperations != null) {
            try {
                String dlqPayload = String.format("{\"originalKey\":\"%s\",\"error\":\"%s\",\"raw\":%s}",
                        key != null ? key : "null", reason, payload != null ? payload : "\"null\"");
                kafkaOperations.send(dlqTopic, key != null ? key : "error", dlqPayload);
            } catch (Exception e) {
                log.error("Failed to forward malformed event to DLQ: {}", e.getMessage());
            }
        }
    }

    public long getProcessedCount() {
        return processedCount.get();
    }

    public long getDlqCount() {
        return dlqCount.get();
    }
}
