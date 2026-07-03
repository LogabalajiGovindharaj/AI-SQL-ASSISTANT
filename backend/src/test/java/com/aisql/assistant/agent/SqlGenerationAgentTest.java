package com.aisql.assistant.agent;

import com.aisql.assistant.service.ClaudeApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SqlGenerationAgentTest {

    private final ClaudeApiService claudeApiService = mock(ClaudeApiService.class);
    private final SqlGenerationAgent agent = new SqlGenerationAgent(claudeApiService);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void returnsGeneratedSqlAndExplanationOnSuccess() throws Exception {
        JsonNode toolInput = mapper.readTree(
                "{\"sql\": \"SELECT * FROM employees ORDER BY salary DESC\", \"explanation\": \"Highest paid employees.\"}");
        when(claudeApiService.generateSql(anyString(), anyString())).thenReturn(toolInput);

        var result = agent.generate("Show highest paid employees");

        assertEquals("SELECT * FROM employees ORDER BY salary DESC", result.sql());
        assertEquals("Highest paid employees.", result.explanation());
    }

    @Test
    void defaultsToEmptyStringsWhenFieldsAreMissing() throws Exception {
        // Simulates a malformed/partial tool_use input from Claude
        JsonNode toolInput = mapper.readTree("{}");
        when(claudeApiService.generateSql(anyString(), anyString())).thenReturn(toolInput);

        var result = agent.generate("Show something");

        assertEquals("", result.sql());
        assertEquals("", result.explanation());
    }

    @Test
    void propagatesExceptionsFromClaudeApiService() {
        when(claudeApiService.generateSql(anyString(), anyString()))
                .thenThrow(new IllegalStateException("Claude API returned HTTP 500: internal error"));

        try {
            agent.generate("Show students");
            throw new AssertionError("expected an exception");
        } catch (IllegalStateException e) {
            assertEquals("Claude API returned HTTP 500: internal error", e.getMessage());
        }
    }

    @Test
    void injectsSchemaIntoSystemPrompt() {
        when(claudeApiService.generateSql(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String systemPrompt = invocation.getArgument(0);
                    // The schema for all three demo tables must be present so
                    // Claude never has to guess column names.
                    assertContains(systemPrompt, "students");
                    assertContains(systemPrompt, "employees");
                    assertContains(systemPrompt, "sales");
                    return mapper.readTree("{\"sql\": \"SELECT 1\", \"explanation\": \"test\"}");
                });

        agent.generate("Show students");
    }

    private void assertContains(String haystack, String needle) {
        if (!haystack.contains(needle)) {
            throw new AssertionError("Expected system prompt to contain: " + needle);
        }
    }
}
