package com.aisql.assistant.service;

import com.aisql.assistant.agent.InsightAgent;
import com.aisql.assistant.agent.OptimizationAgent;
import com.aisql.assistant.agent.SqlGenerationAgent;
import com.aisql.assistant.agent.ValidationAgent;
import com.aisql.assistant.dto.ExecuteQueryResponse;
import com.aisql.assistant.dto.GenerateQueryResponse;
import com.aisql.assistant.repository.QueryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class QueryOrchestratorServiceTest {

    private final SqlGenerationAgent sqlGenerationAgent = mock(SqlGenerationAgent.class);
    private final ValidationAgent validationAgent = mock(ValidationAgent.class);
    private final QueryExecutionService queryExecutionService = mock(QueryExecutionService.class);
    private final InsightAgent insightAgent = mock(InsightAgent.class);
    private final OptimizationAgent optimizationAgent = mock(OptimizationAgent.class);
    private final QueryHistoryRepository queryHistoryRepository = mock(QueryHistoryRepository.class);

    private final QueryOrchestratorService orchestrator = new QueryOrchestratorService(
            sqlGenerationAgent, validationAgent, queryExecutionService, insightAgent,
            optimizationAgent, queryHistoryRepository);

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken("user@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void generate_acceptedPath_returnsSqlAndExplanation() {
        when(sqlGenerationAgent.generate("Show students with CGPA above 8"))
                .thenReturn(new SqlGenerationAgent.GeneratedSql("SELECT * FROM students WHERE cgpa > 8", "explains it"));
        when(validationAgent.validate(anyString())).thenReturn(new ValidationAgent.ValidationResult(true, null));

        GenerateQueryResponse response = orchestrator.generate("Show students with CGPA above 8");

        assertTrue(response.isValid());
        assertEquals("SELECT * FROM students WHERE cgpa > 8", response.getSql());
        assertEquals("explains it", response.getExplanation());
        assertNull(response.getRejectionReason());
        verify(queryHistoryRepository).save(any());
    }

    @Test
    void generate_rejectedPath_recordsHistoryAndReturnsReason() {
        when(sqlGenerationAgent.generate(anyString()))
                .thenReturn(new SqlGenerationAgent.GeneratedSql("DROP TABLE students", "n/a"));
        when(validationAgent.validate("DROP TABLE students"))
                .thenReturn(new ValidationAgent.ValidationResult(false, "blocked keyword: DROP"));

        GenerateQueryResponse response = orchestrator.generate("delete everything");

        assertFalse(response.isValid());
        assertEquals("blocked keyword: DROP", response.getRejectionReason());
        verify(queryHistoryRepository).save(any());
        // execution/insight must never be reached for a rejected query
        verifyNoInteractions(queryExecutionService, insightAgent);
    }

    @Test
    void execute_acceptedPath_returnsRowsInsightAndCostInfo() {
        when(validationAgent.validate(anyString())).thenReturn(new ValidationAgent.ValidationResult(true, null));
        List<Map<String, Object>> rows = List.of(Map.of("student_id", 101, "name", "Arun"));
        when(queryExecutionService.execute(anyString())).thenReturn(rows);
        when(insightAgent.summarize(anyString(), eq(rows)))
                .thenReturn(new InsightAgent.Insight("1 student found.", List.of("follow up?")));
        when(optimizationAgent.analyze(anyString()))
                .thenReturn(new OptimizationAgent.CostAnalysis(4L, "ALL", List.of("add an index")));

        ExecuteQueryResponse response = orchestrator.execute("SELECT * FROM students WHERE cgpa > 8", "Show students");

        assertTrue(response.isValid());
        assertEquals(rows, response.getRows());
        assertEquals("1 student found.", response.getInsight());
        assertEquals(4L, response.getEstimatedRowsScanned());
        assertEquals("ALL", response.getAccessType());
        assertEquals(List.of("add an index"), response.getOptimizationTips());
    }

    @Test
    void execute_rejectsUnsafeSqlEvenIfClientSuppliesItDirectly() {
        // Defense in depth: /execute-query re-validates, it never trusts the client's SQL string.
        when(validationAgent.validate("DROP TABLE students"))
                .thenReturn(new ValidationAgent.ValidationResult(false, "blocked keyword: DROP"));

        ExecuteQueryResponse response = orchestrator.execute("DROP TABLE students", null);

        assertFalse(response.isValid());
        assertEquals("blocked keyword: DROP", response.getRejectionReason());
        verifyNoInteractions(queryExecutionService, insightAgent, optimizationAgent);
    }

    @Test
    void execute_missingQuestion_fallsBackToDefaultForInsightAgent() {
        when(validationAgent.validate(anyString())).thenReturn(new ValidationAgent.ValidationResult(true, null));
        List<Map<String, Object>> rows = List.of();
        when(queryExecutionService.execute(anyString())).thenReturn(rows);
        when(insightAgent.summarize(eq("Explain these results"), eq(rows)))
                .thenReturn(new InsightAgent.Insight("no rows", List.of()));
        when(optimizationAgent.analyze(anyString())).thenReturn(new OptimizationAgent.CostAnalysis(null, null, List.of()));

        ExecuteQueryResponse response = orchestrator.execute("SELECT * FROM students WHERE 1=0", null);

        assertTrue(response.isValid());
        verify(insightAgent).summarize("Explain these results", rows);
    }

    @Test
    void execute_propagatesExecutionFailureAsIllegalStateException() {
        when(validationAgent.validate(anyString())).thenReturn(new ValidationAgent.ValidationResult(true, null));
        when(queryExecutionService.execute(anyString()))
                .thenThrow(new IllegalStateException("Query execution failed: Unknown column 'foo'"));

        assertThrows(IllegalStateException.class, () -> orchestrator.execute("SELECT foo FROM students", "q"));
    }
}
