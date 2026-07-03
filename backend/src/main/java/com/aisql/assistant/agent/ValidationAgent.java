package com.aisql.assistant.agent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Deliberately NOT an LLM call. The whole point of this agent is to be a
 * guardrail that works even if the SQL Generation Agent's prompt is ever
 * bypassed, jailbroken, or simply wrong. Phase 2 scope: single-statement
 * SELECT-only queries against the "students" table.
 */
@Component
public class ValidationAgent {

    private static final List<String> BLOCKED_KEYWORDS = List.of(
            "DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "TRUNCATE",
            "GRANT", "REVOKE", "CREATE", "EXEC", "EXECUTE", "CALL"
    );

    private static final Pattern MULTI_STATEMENT = Pattern.compile(";.*\\S");
    private static final Pattern COMMENT_INJECTION = Pattern.compile("(--)|(/\\*)|(\\*/)");
    private static final List<String> ALLOWED_TABLES = List.of("students", "employees", "sales");

    public ValidationResult validate(String sql) {
        if (sql == null || sql.isBlank()) {
            return ValidationResult.reject("Generated SQL was empty.");
        }

        String trimmed = sql.trim();
        String upper = trimmed.toUpperCase();

        if (!upper.startsWith("SELECT")) {
            return ValidationResult.reject("Only SELECT statements are allowed.");
        }

        for (String keyword : BLOCKED_KEYWORDS) {
            if (containsWholeWord(upper, keyword)) {
                return ValidationResult.reject("Query contains a blocked keyword: " + keyword);
            }
        }

        if (COMMENT_INJECTION.matcher(trimmed).find()) {
            return ValidationResult.reject("Query contains SQL comment syntax, which is not allowed.");
        }

        // strip a single trailing semicolon before checking for stacked statements
        String withoutTrailingSemicolon = trimmed.endsWith(";")
                ? trimmed.substring(0, trimmed.length() - 1)
                : trimmed;
        if (MULTI_STATEMENT.matcher(withoutTrailingSemicolon).find()) {
            return ValidationResult.reject("Multiple SQL statements are not allowed.");
        }

        boolean referencesAllowedTable = ALLOWED_TABLES.stream()
                .anyMatch(table -> upper.contains(table.toUpperCase()));
        if (!referencesAllowedTable) {
            return ValidationResult.reject(
                    "Query does not reference an allowed table. Allowed tables: " + ALLOWED_TABLES);
        }

        return ValidationResult.accept();
    }

    private boolean containsWholeWord(String text, String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }

    public record ValidationResult(boolean valid, String rejectionReason) {
        static ValidationResult accept() {
            return new ValidationResult(true, null);
        }

        static ValidationResult reject(String reason) {
            return new ValidationResult(false, reason);
        }
    }
}
