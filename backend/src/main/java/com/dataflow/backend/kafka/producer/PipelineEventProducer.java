package com.dataflow.backend.kafka.producer;

import com.dataflow.backend.kafka.event.DataFlowPipelineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Resilient Kafka Event Producer for DataFlow pipeline lifecycle transitions.
 * Supports asynchronous dispatch with error callback logging and circuit fallback.
 */
@Service
public class PipelineEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PipelineEventProducer.class);

    private final KafkaOperations<String, Object> kafkaOperations;

    @Value("${dataflow.kafka.topics.pipeline-events:dataflow.restaurant.events}")
    private String pipelineEventsTopic;

    @Autowired(required = false)
    public PipelineEventProducer(KafkaOperations<String, Object> kafkaOperations) {
        this.kafkaOperations = kafkaOperations;
    }

    public void publishJobEvent(String jobId, String runId, String eventType, String status, long recordCount) {
        DataFlowPipelineEvent event = new DataFlowPipelineEvent(
                UUID.randomUUID().toString(),
                eventType,
                jobId,
                runId,
                status,
                recordCount,
                Instant.now(),
                "Pipeline transitioned to status: " + status
        );

        if (kafkaOperations == null) {
            log.warn("KafkaOperations unavailable. Logged pipeline event locally: jobId={}, status={}", jobId, status);
            return;
        }

        try {
            kafkaOperations.send(pipelineEventsTopic, jobId, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Dispatched Kafka event [type={}, jobId={}] to partition {} with offset {}",
                                    eventType, jobId,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to publish Kafka event [type={}, jobId={}]: {}",
                                    eventType, jobId, ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("Resilient fallback: Error dispatching pipeline event [jobId={}]: {}", jobId, e.getMessage());
        }
    }
}
