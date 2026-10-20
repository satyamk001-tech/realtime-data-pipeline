package com.dataflow.spark.model;

import java.io.Serializable;
import java.time.Instant;

public class EtlMetrics implements Serializable {
    private static final long serialVersionUID = 1L;

    private String runId;
    private long inputRecords;
    private long validRecords;
    private long quarantinedRecords;
    private long duplicateRecords;
    private long durationMs;
    private String processedPath;
    private String quarantinePath;
    private Instant completedAt;

    public EtlMetrics() {}

    public EtlMetrics(String runId, long inputRecords, long validRecords, long quarantinedRecords,
                      long duplicateRecords, long durationMs, String processedPath,
                      String quarantinePath, Instant completedAt) {
        this.runId = runId;
        this.inputRecords = inputRecords;
        this.validRecords = validRecords;
        this.quarantinedRecords = quarantinedRecords;
        this.duplicateRecords = duplicateRecords;
        this.durationMs = durationMs;
        this.processedPath = processedPath;
        this.quarantinePath = quarantinePath;
        this.completedAt = completedAt;
    }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public long getInputRecords() { return inputRecords; }
    public void setInputRecords(long inputRecords) { this.inputRecords = inputRecords; }
    public long getValidRecords() { return validRecords; }
    public void setValidRecords(long validRecords) { this.validRecords = validRecords; }
    public long getQuarantinedRecords() { return quarantinedRecords; }
    public void setQuarantinedRecords(long quarantinedRecords) { this.quarantinedRecords = quarantinedRecords; }
    public long getDuplicateRecords() { return duplicateRecords; }
    public void setDuplicateRecords(long duplicateRecords) { this.duplicateRecords = duplicateRecords; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public String getProcessedPath() { return processedPath; }
    public void setProcessedPath(String processedPath) { this.processedPath = processedPath; }
    public String getQuarantinePath() { return quarantinePath; }
    public void setQuarantinePath(String quarantinePath) { this.quarantinePath = quarantinePath; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    @Override
    public String toString() {
        return "EtlMetrics{" +
                "runId='" + runId + '\'' +
                ", inputRecords=" + inputRecords +
                ", validRecords=" + validRecords +
                ", quarantinedRecords=" + quarantinedRecords +
                ", duplicateRecords=" + duplicateRecords +
                ", durationMs=" + durationMs +
                ", processedPath='" + processedPath + '\'' +
                ", quarantinePath='" + quarantinePath + '\'' +
                ", completedAt=" + completedAt +
                '}';
    }
}
