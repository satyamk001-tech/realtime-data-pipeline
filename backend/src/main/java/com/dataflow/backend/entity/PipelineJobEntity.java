package com.dataflow.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pipeline_jobs")
public class PipelineJobEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "run_id", nullable = false, unique = true, length = 64)
    private String runId;

    @Column(name = "pipeline_type", nullable = false, length = 50)
    private String pipelineType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "source_path", length = 500)
    private String sourcePath;

    @Column(name = "target_path", length = 500)
    private String targetPath;

    @Column(name = "input_records")
    private Long inputRecords = 0L;

    @Column(name = "valid_records")
    private Long validRecords = 0L;

    @Column(name = "quarantined_records")
    private Long quarantinedRecords = 0L;

    @Column(name = "duplicate_records")
    private Long duplicateRecords = 0L;

    @Column(name = "duration_ms")
    private Long durationMs = 0L;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy = "system";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PipelineJobAttemptEntity> attempts = new ArrayList<>();

    public PipelineJobEntity() {}

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
    public List<PipelineJobAttemptEntity> getAttempts() { return attempts; }
    public void setAttempts(List<PipelineJobAttemptEntity> attempts) { this.attempts = attempts; }
}
