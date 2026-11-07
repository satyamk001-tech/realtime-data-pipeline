package com.dataflow.backend.kafka.producer;

import com.dataflow.backend.kafka.event.DataFlowPipelineEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PipelineEventProducerTest {

    private KafkaOperations<String, Object> kafkaOperations;
    private PipelineEventProducer producer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaOperations = (KafkaOperations<String, Object>) Mockito.mock(KafkaOperations.class);
        producer = new PipelineEventProducer(kafkaOperations);
    }

    @Test
    void shouldPublishJobEventSuccessfully() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaOperations.send(any(), any(), any())).thenReturn(future);

        producer.publishJobEvent("job-101", "run-555", "PIPELINE_INITIATED", "PENDING", 0);

        ArgumentCaptor<DataFlowPipelineEvent> eventCaptor = ArgumentCaptor.forClass(DataFlowPipelineEvent.class);
        verify(kafkaOperations, times(1)).send(any(), eq("job-101"), eventCaptor.capture());

        DataFlowPipelineEvent captured = eventCaptor.getValue();
        assertThat(captured.getJobId()).isEqualTo("job-101");
        assertThat(captured.getRunId()).isEqualTo("run-555");
        assertThat(captured.getEventType()).isEqualTo("PIPELINE_INITIATED");
        assertThat(captured.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldHandleNullKafkaTemplateGracefully() {
        PipelineEventProducer nullProducer = new PipelineEventProducer(null);
        assertThatCode(() -> nullProducer.publishJobEvent("job-101", "run-555", "PIPELINE_INITIATED", "PENDING", 0))
                .doesNotThrowAnyException();
    }
}
