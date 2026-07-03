package com.aisql.assistant.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Executes SQL that has already passed the ValidationAgent. This is the only
 * class in the codebase allowed to run user-generated SQL against the
 * database, and it is intentionally narrow: read-only, timeout-bounded,
 * row-capped.
 */
@Service
public class QueryExecutionService {

    private static final int QUERY_TIMEOUT_SECONDS = 5;
    private static final int MAX_ROWS = 100;
    private static final Pattern HAS_LIMIT = Pattern.compile("\\bLIMIT\\b", Pattern.CASE_INSENSITIVE);

    private final JdbcTemplate jdbcTemplate;

    public QueryExecutionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcTemplate.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
        this.jdbcTemplate.setMaxRows(MAX_ROWS);
    }

    public List<Map<String, Object>> execute(String validatedSql) {
        String sql = enforceLimit(validatedSql);
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (DataAccessException e) {
            // A query can pass the Validation Agent's checks (safe keywords,
            // known table) and still be malformed in a way only MySQL can
            // detect (unknown column, type mismatch, etc). Wrap it so the
            // client gets a clean message instead of a raw stack trace.
            throw new IllegalStateException("Query execution failed: " + rootMessage(e), e);
        }
    }

    private String rootMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : e.getMessage();
    }

    /**
     * Belt-and-suspenders: even though JdbcTemplate.setMaxRows caps result
     * fetching, we also append LIMIT to the statement itself so we never ask
     * MySQL to compute a huge result set in the first place.
     */
    private String enforceLimit(String sql) {
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (HAS_LIMIT.matcher(trimmed).find()) {
            return trimmed;
        }
        return trimmed + " LIMIT " + MAX_ROWS;
    }
}
