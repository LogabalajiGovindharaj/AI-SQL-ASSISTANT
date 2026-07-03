package com.aisql.assistant.agent;

import com.aisql.assistant.service.ClaudeApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InsightAgentTest {

    private final ClaudeApiService claudeApiService = mock(ClaudeApiService.class);
    private final InsightAgent agent = new InsightAgent(claudeApiService);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsesInsightAndSuggestions() throws Exception {
        JsonNode toolInput = mapper.readTree(
                "{\"insight\": \"South leads in sales.\", \"suggestions\": [\"Which product?\", \"vs last quarter?\"]}");
        when(claudeApiService.generateInsight(anyString(), anyString())).thenReturn(toolInput);

        var result = agent.summarize("Show total sales region wise",
                List.of(Map.of("region", "South", "amount", 50000)));

        assertEquals("South leads in sales.", result.text());
        assertEquals(2, result.suggestions().size());
        assertEquals("Which product?", result.suggestions().get(0));
    }

    @Test
    void handlesEmptyResultSet() throws Exception {
        JsonNode toolInput = mapper.readTree(
                "{\"insight\": \"No matching rows.\", \"suggestions\": []}");
        when(claudeApiService.generateInsight(anyString(), anyString())).thenReturn(toolInput);

        var result = agent.summarize("Show students with CGPA above 10", List.of());

        assertEquals("No matching rows.", result.text());
        assertTrue(result.suggestions().isEmpty());
    }

    @Test
    void doesNotShipFullRowSetIntoThePrompt() throws Exception {
        JsonNode toolInput = mapper.readTree("{\"insight\": \"ok\", \"suggestions\": []}");
        when(claudeApiService.generateInsight(anyString(), anyString())).thenReturn(toolInput);

        // 20 rows - only a small sample should end up in the prompt sent to Claude
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            rows.add(Map.of("id", i, "name", "row-" + i));
        }

        agent.summarize("Show everything", rows);

        verify(claudeApiService).generateInsight(anyString(), org.mockito.ArgumentMatchers.argThat(
                content -> content.contains("Row count: 20") && !content.contains("row-19")));
    }
}
