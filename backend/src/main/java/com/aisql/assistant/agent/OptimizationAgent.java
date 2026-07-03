package com.aisql.assistant.agent;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Not an LLM call - cost estimation comes straight from MySQL's own EXPLAIN
 * plan, and optimization tips are deterministic heuristics. This keeps cost
 * numbers trustworthy (they're MySQL's own estimate, not a guess) and keeps
 * the tips reproducible.
 */
@Component
public class OptimizationAgent {

    private static final Pattern SELECT_STAR = Pattern.compile("SELECT\\s+\\*", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_WHERE = Pattern.compile("\\bWHERE\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_LIMIT = Pattern.compile("\\bLIMIT\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_ORDER_BY = Pattern.compile("\\bORDER\\s+BY\\b", Pattern.CASE_INSENSITIVE);

    private final JdbcTemplate jdbcTemplate;

    public OptimizationAgent(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CostAnalysis analyze(String sql) {
        Long estimatedRowsScanned = null;
        String accessType = null;

        try {
            List<Map<String, Object>> plan = jdbcTemplate.queryForList("EXPLAIN " + sql);
            if (!plan.isEmpty()) {
                Map<String, Object> firstRow = plan.get(0);
                Object rows = firstRow.get("rows");
                if (rows != null) {
                    estimatedRowsScanned = Long.parseLong(rows.toString());
                }
                Object type = firstRow.get("type");
                accessType = type != null ? type.toString() : null;
            }
        } catch (Exception e) {
            // EXPLAIN failing shouldn't block the actual query - cost info is best-effort.
        }

        List<String> tips = buildTips(sql, accessType);
        return new CostAnalysis(estimatedRowsScanned, accessType, tips);
    }

    private List<String> buildTips(String sql, String accessType) {
        List<String> tips = new ArrayList<>();

        if (SELECT_STAR.matcher(sql).find()) {
            tips.add("Selecting specific columns instead of '*' reduces the amount of data transferred.");
        }
        if (!HAS_WHERE.matcher(sql).find()) {
            tips.add("No WHERE clause - this scans the entire table. Add a filter if you only need a subset.");
        }
        if (!HAS_LIMIT.matcher(sql).find()) {
            tips.add("No LIMIT clause - consider capping result size for large tables.");
        }
        if (HAS_ORDER_BY.matcher(sql).find() && !HAS_LIMIT.matcher(sql).find()) {
            tips.add("ORDER BY without LIMIT sorts the full result set - add LIMIT if you only need the top rows.");
        }
        if ("ALL".equalsIgnoreCase(accessType)) {
            tips.add("MySQL's plan shows a full table scan (type=ALL). An index on the filtered column could speed this up.");
        }

        return tips;
    }

    public record CostAnalysis(Long estimatedRowsScanned, String accessType, List<String> tips) {
    }
}
