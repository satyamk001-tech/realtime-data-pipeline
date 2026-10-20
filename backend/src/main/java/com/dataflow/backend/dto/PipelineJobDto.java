package com.dataflow.backend.dto;

import java.time.Instant;

public class PipelineJobDto {
    private String id;
    private String runId;
    private String pipelineType;
    private String status;
    private String sourcePath;
    private String targetPath;
    private Long inputRecords;
    private Long validRecords;
    private Long quarantinedRecords;
    private Long duplicateRecords;
    private Long durationMs;
    private String triggeredBy;
    private String errorMessage;
    private Instant startedAt;
    private Instant completedAt;

    public PipelineJobDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getPipelineType() { return pipelineType; }
    public void setPipelineType(String pipelineType) { this.pipelineType = pipelineType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
    public Long getInputRecords() { return inputRecords; }
    public void setInputRecords(Long inputRecords) { this.inputRecords = inputRecords; }
    public Long getValidRecords() { return validRecords; }
    public void setValidRecords(Long validRecords) { this.validRecords = validRecords; }
    public Long getQuarantinedRecords() { return quarantinedRecords; }
    public void setQuarantinedRecords(Long quarantinedRecords) { this.quarantinedRecords = quarantinedRecords; }
    public Long getDuplicateRecords() { return duplicateRecords; }
    public void setDuplicateRecords(Long duplicateRecords) { this.duplicateRecords = duplicateRecords; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
