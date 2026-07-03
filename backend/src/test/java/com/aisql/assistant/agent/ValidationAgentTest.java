package com.aisql.assistant.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationAgentTest {

    private final ValidationAgent agent = new ValidationAgent();

    @Test
    void acceptsSimpleSelect() {
        var result = agent.validate("SELECT * FROM students WHERE cgpa > 8");
        assertTrue(result.valid());
    }

    @Test
    void acceptsAggregateSelectWithGroupBy() {
        var result = agent.validate("SELECT region, SUM(amount) FROM sales GROUP BY region");
        assertTrue(result.valid());
    }

    @Test
    void rejectsEmptyOrNullSql() {
        assertFalse(agent.validate("").valid());
        assertFalse(agent.validate("   ").valid());
        assertFalse(agent.validate(null).valid());
    }

    @Test
    void rejectsNonSelectStatements() {
        assertFalse(agent.validate("UPDATE students SET cgpa = 10").valid());
        assertFalse(agent.validate("INSERT INTO students VALUES (1,2,3,4,5)").valid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "DROP TABLE students",
            "DELETE FROM students WHERE 1=1",
            "TRUNCATE TABLE students",
            "ALTER TABLE students DROP COLUMN cgpa",
            "GRANT ALL ON students TO 'x'@'%'",
            "SELECT * FROM students; DROP TABLE students;"
    })
    void rejectsDestructiveOrBlockedKeywords(String sql) {
        assertFalse(agent.validate(sql).valid());
    }

    @Test
    void rejectsCommentInjection() {
        assertFalse(agent.validate("SELECT * FROM students -- WHERE cgpa > 8").valid());
        assertFalse(agent.validate("SELECT * FROM students /* comment */ WHERE cgpa > 8").valid());
    }

    @Test
    void rejectsStackedStatements() {
        assertFalse(agent.validate("SELECT * FROM students; SELECT * FROM employees;").valid());
    }

    @Test
    void allowsSingleTrailingSemicolon() {
        assertTrue(agent.validate("SELECT * FROM students;").valid());
    }

    @Test
    void rejectsQueriesAgainstUnknownTables() {
        var result = agent.validate("SELECT * FROM users");
        assertFalse(result.valid());
    }

    @Test
    void rejectionIncludesAReason() {
        var result = agent.validate("DROP TABLE students");
        assertFalse(result.valid());
        assertTrue(result.rejectionReason() != null && !result.rejectionReason().isBlank());
    }
}
