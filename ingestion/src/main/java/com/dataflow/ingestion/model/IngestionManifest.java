package com.dataflow.ingestion.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

public class IngestionManifest {
    private String runId;
    private String sourceFileName;
    private String checksumSha256;
    private long fileSizeBytes;
    private long totalRecords;
    private List<String> detectedHeaders;
    private String rawStoragePath;
    private String bucketName;
    private IngestionStatus status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private Instant ingestionTimestamp;
    
    private String ingestedBy;
    private String notes;

    public IngestionManifest() {}

    public IngestionManifest(String runId, String sourceFileName, String checksumSha256, long fileSizeBytes,
                             long totalRecords, List<String> detectedHeaders, String rawStoragePath,
                             String bucketName, IngestionStatus status, Instant ingestionTimestamp,
                             String ingestedBy, String notes) {
        this.runId = runId;
        this.sourceFileName = sourceFileName;
        this.checksumSha256 = checksumSha256;
        this.fileSizeBytes = fileSizeBytes;
        this.totalRecords = totalRecords;
        this.detectedHeaders = detectedHeaders;
        this.rawStoragePath = rawStoragePath;
        this.bucketName = bucketName;
        this.status = status;
        this.ingestionTimestamp = ingestionTimestamp;
        this.ingestedBy = ingestedBy;
        this.notes = notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String runId;
        private String sourceFileName;
        private String checksumSha256;
        private long fileSizeBytes;
        private long totalRecords;
        private List<String> detectedHeaders;
        private String rawStoragePath;
        private String bucketName;
        private IngestionStatus status;
        private Instant ingestionTimestamp;
        private String ingestedBy;
        private String notes;

        public Builder runId(String runId) { this.runId = runId; return this; }
        public Builder sourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; return this; }
        public Builder checksumSha256(String checksumSha256) { this.checksumSha256 = checksumSha256; return this; }
        public Builder fileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; return this; }
        public Builder totalRecords(long totalRecords) { this.totalRecords = totalRecords; return this; }
        public Builder detectedHeaders(List<String> detectedHeaders) { this.detectedHeaders = detectedHeaders; return this; }
        public Builder rawStoragePath(String rawStoragePath) { this.rawStoragePath = rawStoragePath; return this; }
        public Builder bucketName(String bucketName) { this.bucketName = bucketName; return this; }
        public Builder status(IngestionStatus status) { this.status = status; return this; }
        public Builder ingestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; return this; }
        public Builder ingestedBy(String ingestedBy) { this.ingestedBy = ingestedBy; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public IngestionManifest build() {
            return new IngestionManifest(runId, sourceFileName, checksumSha256, fileSizeBytes, totalRecords,
                    detectedHeaders, rawStoragePath, bucketName, status, ingestionTimestamp, ingestedBy, notes);
        }
    }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }
    public String getChecksumSha256() { return checksumSha256; }
    public void setChecksumSha256(String checksumSha256) { this.checksumSha256 = checksumSha256; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
    public List<String> getDetectedHeaders() { return detectedHeaders; }
    public void setDetectedHeaders(List<String> detectedHeaders) { this.detectedHeaders = detectedHeaders; }
    public String getRawStoragePath() { return rawStoragePath; }
    public void setRawStoragePath(String rawStoragePath) { this.rawStoragePath = rawStoragePath; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public IngestionStatus getStatus() { return status; }
    public void setStatus(IngestionStatus status) { this.status = status; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
    public String getIngestedBy() { return ingestedBy; }
    public void setIngestedBy(String ingestedBy) { this.ingestedBy = ingestedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
