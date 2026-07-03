package com.aisql.assistant.dto;

import com.aisql.assistant.model.QueryHistory;

import java.time.Instant;

public class QueryHistoryEntry {

    private Long id;
    private String question;
    private String sql;
    private boolean valid;
    private String rejectionReason;
    private Instant createdAt;

    public static QueryHistoryEntry from(QueryHistory h) {
        QueryHistoryEntry e = new QueryHistoryEntry();
        e.id = h.getId();
        e.question = h.getQuestion();
        e.sql = h.getSql();
        e.valid = h.isValid();
        e.rejectionReason = h.getRejectionReason();
        e.createdAt = h.getCreatedAt();
        return e;
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getSql() {
        return sql;
    }

    public boolean isValid() {
        return valid;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
