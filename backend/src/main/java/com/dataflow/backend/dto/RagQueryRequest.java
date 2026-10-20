package com.dataflow.backend.dto;

public class RagQueryRequest {
    private String query;
    private int topK = 5;

    public RagQueryRequest() {}

    public RagQueryRequest(String query, int topK) {
        this.query = query;
        this.topK = topK;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
}
