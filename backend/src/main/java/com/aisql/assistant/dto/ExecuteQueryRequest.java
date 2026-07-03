package com.aisql.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class ExecuteQueryRequest {

    @NotBlank(message = "sql must not be blank")
    private String sql;

    /** Optional - improves the Insight Agent's output if provided. */
    private String question;

    public ExecuteQueryRequest() {
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
