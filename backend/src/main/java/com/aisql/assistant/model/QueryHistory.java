package com.aisql.assistant.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "query_history")
public class QueryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false, length = 2000)
    private String question;

    @Column(name = "`sql`", length = 2000)
    private String sql;

    @Column(nullable = false)
    private boolean valid;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public QueryHistory() {
    }

    public QueryHistory(String userEmail, String question, String sql, boolean valid, String rejectionReason) {
        this.userEmail = userEmail;
        this.question = question;
        this.sql = sql;
        this.valid = valid;
        this.rejectionReason = rejectionReason;
    }

    public Long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
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
