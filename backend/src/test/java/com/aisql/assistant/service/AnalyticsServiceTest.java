package com.aisql.assistant.service;

import com.aisql.assistant.model.QueryHistory;
import com.aisql.assistant.repository.QueryHistoryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyticsServiceTest {

    private final QueryHistoryRepository repo = mock(QueryHistoryRepository.class);
    private final AnalyticsService service = new AnalyticsService(repo);

    @Test
    void countsValidAndRejectedSeparately() {
        List<QueryHistory> history = List.of(
                new QueryHistory("u@test.com", "q1", "SELECT * FROM students", true, null),
                new QueryHistory("u@test.com", "q2", "DROP TABLE students", false, "blocked keyword"),
                new QueryHistory("u@test.com", "q3", "SELECT * FROM employees", true, null)
        );
        when(repo.findByUserEmailOrderByCreatedAtDesc("u@test.com")).thenReturn(history);

        var result = service.forUser("u@test.com");

        assertEquals(3, result.getTotalQueries());
        assertEquals(2, result.getValidQueries());
        assertEquals(1, result.getRejectedQueries());
    }

    @Test
    void countsMostQueriedTablesCaseInsensitively() {
        List<QueryHistory> history = List.of(
                new QueryHistory("u@test.com", "q1", "select * from STUDENTS", true, null),
                new QueryHistory("u@test.com", "q2", "SELECT * FROM students WHERE cgpa > 8", true, null),
                new QueryHistory("u@test.com", "q3", "SELECT * FROM sales", true, null)
        );
        when(repo.findByUserEmailOrderByCreatedAtDesc("u@test.com")).thenReturn(history);

        var result = service.forUser("u@test.com");

        assertEquals(2L, result.getMostQueriedTables().get("students"));
        assertEquals(1L, result.getMostQueriedTables().get("sales"));
        assertEquals(0L, result.getMostQueriedTables().get("employees"));
    }
}
