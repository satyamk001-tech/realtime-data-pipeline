package com.dataflow.backend.dto;

public class PipelineRunRequest {
    private String pipelineType;
    private String sourcePath;
    private String triggeredBy;

    public PipelineRunRequest() {}

    public PipelineRunRequest(String pipelineType, String sourcePath, String triggeredBy) {
        this.pipelineType = pipelineType;
        this.sourcePath = sourcePath;
        this.triggeredBy = triggeredBy;
    }

    public String getPipelineType() { return pipelineType; }
    public void setPipelineType(String pipelineType) { this.pipelineType = pipelineType; }
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
}
