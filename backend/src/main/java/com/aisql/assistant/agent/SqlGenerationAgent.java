package com.aisql.assistant.agent;

import com.aisql.assistant.service.ClaudeApiService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * Phase 2 collapses "Intent Understanding" and "SQL Generation" into a single
 * Claude call for the minimal slice. They will be split into two agents in
 * Phase 3 once we need the intermediate intent object for other consumers
 * (e.g. query history search, analytics).
 */
@Component
public class SqlGenerationAgent {

    private static final String SCHEMA_DESCRIPTION = """
            Table: students
            Columns:
              student_id INT PRIMARY KEY
              name       VARCHAR(100)
              department VARCHAR(50)
              cgpa       DECIMAL(3,2)
              marks      INT

            Table: employees
            Columns:
              id         INT PRIMARY KEY
              name       VARCHAR(100)
              department VARCHAR(50)
              salary     INT
              experience INT

            Table: sales
            Columns:
              id      INT PRIMARY KEY
              product VARCHAR(100)
              amount  INT
              region  VARCHAR(50)
            """;

    private static final String SYSTEM_PROMPT = """
            You are a SQL Expert AI Agent for a MySQL database.

            You may ONLY generate a single read-only SELECT statement against the
            schema below. You must never generate DROP, DELETE, UPDATE, INSERT,
            ALTER, TRUNCATE, GRANT, REVOKE, or any statement that modifies data or
            schema. You must never generate more than one SQL statement.

            Schema:
            %s

            Always call the generate_sql tool with the SQL and a one-sentence
            explanation. If the user's question cannot be answered from this
            schema, still call the tool, returning a SELECT that returns zero
            rows (e.g. "SELECT * FROM students WHERE 1=0"), and explain why in
            the explanation field.
            """.formatted(SCHEMA_DESCRIPTION);

    private final ClaudeApiService claudeApiService;

    public SqlGenerationAgent(ClaudeApiService claudeApiService) {
        this.claudeApiService = claudeApiService;
    }

    public GeneratedSql generate(String userQuestion) {
        JsonNode toolInput = claudeApiService.generateSql(SYSTEM_PROMPT, userQuestion);
        String sql = toolInput.path("sql").asText("");
        String explanation = toolInput.path("explanation").asText("");
        return new GeneratedSql(sql, explanation);
    }

    public record GeneratedSql(String sql, String explanation) {
    }
}
