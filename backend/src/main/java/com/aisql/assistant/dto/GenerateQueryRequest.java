package com.aisql.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateQueryRequest {

    @NotBlank(message = "question must not be blank")
    private String question;

    public GenerateQueryRequest() {
    }

    public GenerateQueryRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
