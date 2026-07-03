package com.aisql.assistant.dto;

public class GenerateQueryResponse {

    private String sql;
    private String explanation;
    private boolean valid;
    private String rejectionReason;

    public static GenerateQueryResponse rejected(String sql, String reason) {
        GenerateQueryResponse r = new GenerateQueryResponse();
        r.sql = sql;
        r.valid = false;
        r.rejectionReason = reason;
        return r;
    }

    public static GenerateQueryResponse accepted(String sql, String explanation) {
        GenerateQueryResponse r = new GenerateQueryResponse();
        r.sql = sql;
        r.explanation = explanation;
        r.valid = true;
        return r;
    }

    public String getSql() {
        return sql;
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean isValid() {
        return valid;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
