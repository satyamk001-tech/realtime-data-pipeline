package com.dataflow.ingestion.model;

import java.time.Instant;

public class IngestionJob {
    private String jobId;
    private String runId;
    private String sourceFilePath;
    private String fileName;
    private String checksumSha256;
    private long fileSizeBytes;
    private long recordCount;
    private IngestionStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private String s3RawPath;
    private String errorMessage;

    public IngestionJob() {}

    public IngestionJob(String jobId, String runId, String sourceFilePath, String fileName,
                        String checksumSha256, long fileSizeBytes, long recordCount,
                        IngestionStatus status, Instant startedAt, Instant completedAt,
                        String s3RawPath, String errorMessage) {
        this.jobId = jobId;
        this.runId = runId;
        this.sourceFilePath = sourceFilePath;
        this.fileName = fileName;
        this.checksumSha256 = checksumSha256;
        this.fileSizeBytes = fileSizeBytes;
        this.recordCount = recordCount;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.s3RawPath = s3RawPath;
        this.errorMessage = errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jobId;
        private String runId;
        private String sourceFilePath;
        private String fileName;
        private String checksumSha256;
        private long fileSizeBytes;
        private long recordCount;
        private IngestionStatus status;
        private Instant startedAt;
        private Instant completedAt;
        private String s3RawPath;
        private String errorMessage;

        public Builder jobId(String jobId) { this.jobId = jobId; return this; }
        public Builder runId(String runId) { this.runId = runId; return this; }
        public Builder sourceFilePath(String sourceFilePath) { this.sourceFilePath = sourceFilePath; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder checksumSha256(String checksumSha256) { this.checksumSha256 = checksumSha256; return this; }
        public Builder fileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; return this; }
        public Builder recordCount(long recordCount) { this.recordCount = recordCount; return this; }
        public Builder status(IngestionStatus status) { this.status = status; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }
        public Builder s3RawPath(String s3RawPath) { this.s3RawPath = s3RawPath; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }

        public IngestionJob build() {
            return new IngestionJob(jobId, runId, sourceFilePath, fileName, checksumSha256,
                    fileSizeBytes, recordCount, status, startedAt, completedAt, s3RawPath, errorMessage);
        }
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getSourceFilePath() { return sourceFilePath; }
    public void setSourceFilePath(String sourceFilePath) { this.sourceFilePath = sourceFilePath; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getChecksumSha256() { return checksumSha256; }
    public void setChecksumSha256(String checksumSha256) { this.checksumSha256 = checksumSha256; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public long getRecordCount() { return recordCount; }
    public void setRecordCount(long recordCount) { this.recordCount = recordCount; }
    public IngestionStatus getStatus() { return status; }
    public void setStatus(IngestionStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getS3RawPath() { return s3RawPath; }
    public void setS3RawPath(String s3RawPath) { this.s3RawPath = s3RawPath; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
