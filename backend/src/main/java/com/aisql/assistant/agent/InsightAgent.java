package com.aisql.assistant.agent;

import com.aisql.assistant.service.ClaudeApiService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deliberately does NOT receive the full raw result set - only a small
 * summary (row count + a few sample rows) - to keep the prompt small and
 * avoid ever needing to ship a large table into an LLM call.
 */
@Component
public class InsightAgent {

    private static final String SYSTEM_PROMPT = """
            You are a data analyst assistant. You will be given the user's
            original question and a short summary of the SQL query results.
            Provide one short, concrete insight and 2-3 natural follow-up
            questions the user might want to ask next. Do not repeat the
            question verbatim; add analytical value.
            """;

    private static final int SAMPLE_ROW_LIMIT = 5;

    private final ClaudeApiService claudeApiService;

    public InsightAgent(ClaudeApiService claudeApiService) {
        this.claudeApiService = claudeApiService;
    }

    public Insight summarize(String originalQuestion, List<Map<String, Object>> rows) {
        String userContent = buildSummaryPrompt(originalQuestion, rows);
        JsonNode toolInput = claudeApiService.generateInsight(SYSTEM_PROMPT, userContent);

        String insightText = toolInput.path("insight").asText("");
        List<String> suggestions = new ArrayList<>();
        toolInput.path("suggestions").forEach(node -> suggestions.add(node.asText()));

        return new Insight(insightText, suggestions);
    }

    private String buildSummaryPrompt(String originalQuestion, List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("Original question: ").append(originalQuestion).append("\n");
        sb.append("Row count: ").append(rows.size()).append("\n");
        sb.append("Sample rows (up to ").append(SAMPLE_ROW_LIMIT).append("):\n");
        rows.stream().limit(SAMPLE_ROW_LIMIT).forEach(row -> sb.append(row).append("\n"));
        return sb.toString();
    }

    public record Insight(String text, List<String> suggestions) {
    }
}
