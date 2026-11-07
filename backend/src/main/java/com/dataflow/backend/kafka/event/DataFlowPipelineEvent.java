package com.dataflow.backend.kafka.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Canonical real-time event envelope dispatched across the DataFlow Kafka backbone.
 */
public class DataFlowPipelineEvent implements Serializable {

    private String eventId;
    private String eventType;
    private String jobId;
    private String runId;
    private String status;
    private long recordCount;
    private Instant timestamp;
    private String payload;

    public DataFlowPipelineEvent() {
    }

    public DataFlowPipelineEvent(String eventId, String eventType, String jobId, String runId,
                                 String status, long recordCount, Instant timestamp, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.jobId = jobId;
        this.runId = runId;
        this.status = status;
        this.recordCount = recordCount;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(long recordCount) {
        this.recordCount = recordCount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
