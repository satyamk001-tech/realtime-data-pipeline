package com.dataflow.backend.dto;

public class TextToSqlRequest {
    private String question;

    public TextToSqlRequest() {}

    public TextToSqlRequest(String question) {
        this.question = question;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
