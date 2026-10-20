package com.dataflow.backend.mapper;

import com.dataflow.backend.dto.PipelineJobDto;
import com.dataflow.backend.entity.PipelineJobEntity;

public class PipelineJobMapper {

    public static PipelineJobDto toDto(PipelineJobEntity entity) {
        if (entity == null) return null;
        PipelineJobDto dto = new PipelineJobDto();
        dto.setId(entity.getId());
        dto.setRunId(entity.getRunId());
        dto.setPipelineType(entity.getPipelineType());
        dto.setStatus(entity.getStatus());
        dto.setSourcePath(entity.getSourcePath());
        dto.setTargetPath(entity.getTargetPath());
        dto.setInputRecords(entity.getInputRecords());
        dto.setValidRecords(entity.getValidRecords());
        dto.setQuarantinedRecords(entity.getQuarantinedRecords());
        dto.setDuplicateRecords(entity.getDuplicateRecords());
        dto.setDurationMs(entity.getDurationMs());
        dto.setTriggeredBy(entity.getTriggeredBy());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setStartedAt(entity.getStartedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        return dto;
    }
}
