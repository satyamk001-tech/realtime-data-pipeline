package com.dataflow.backend.service;

import com.dataflow.backend.dto.PipelineJobDto;
import com.dataflow.backend.dto.PipelineRunRequest;
import com.dataflow.backend.entity.PipelineJobEntity;
import com.dataflow.backend.repository.PipelineJobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineJobServiceTest {

    @Mock
    private PipelineJobRepository pipelineJobRepository;

    @InjectMocks
    private PipelineJobService pipelineJobService;

    @Test
    @DisplayName("Should successfully trigger and register a new batch pipeline execution")
    void shouldTriggerPipelineRun() {
        PipelineRunRequest request = new PipelineRunRequest();
        request.setPipelineType("BATCH_ETL");
        request.setTriggeredBy("UNIT_TEST");

        PipelineJobEntity savedEntity = new PipelineJobEntity();
        savedEntity.setId("job-12345");
        savedEntity.setPipelineType("BATCH_ETL");
        savedEntity.setStatus("SUBMITTED");

        when(pipelineJobRepository.save(any(PipelineJobEntity.class))).thenReturn(savedEntity);

        PipelineJobDto result = pipelineJobService.triggerPipeline(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("job-12345");
        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
        verify(pipelineJobRepository, times(1)).save(any(PipelineJobEntity.class));
    }
}
