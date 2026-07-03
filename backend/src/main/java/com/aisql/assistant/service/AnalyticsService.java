package com.aisql.assistant.service;

import com.aisql.assistant.dto.AnalyticsResponse;
import com.aisql.assistant.dto.QueryHistoryEntry;
import com.aisql.assistant.model.QueryHistory;
import com.aisql.assistant.repository.QueryHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnalyticsService {

    private static final List<String> KNOWN_TABLES = List.of("students", "employees", "sales");
    private static final int MAX_RECENT_REJECTIONS = 10;

    private final QueryHistoryRepository queryHistoryRepository;

    public AnalyticsService(QueryHistoryRepository queryHistoryRepository) {
        this.queryHistoryRepository = queryHistoryRepository;
    }

    /** Scoped to one user's own history. Used by the default (non-admin) /analytics call. */
    public AnalyticsResponse forUser(String userEmail) {
        return build(queryHistoryRepository.findByUserEmailOrderByCreatedAtDesc(userEmail));
    }

    /** Unscoped, across all users. Only exposed to ADMIN callers. */
    public AnalyticsResponse forAllUsers() {
        return build(queryHistoryRepository.findAll());
    }

    private AnalyticsResponse build(List<QueryHistory> history) {
        long total = history.size();
        long valid = history.stream().filter(QueryHistory::isValid).count();
        long rejected = total - valid;

        Map<String, Long> tableCounts = new LinkedHashMap<>();
        for (String table : KNOWN_TABLES) {
            long count = history.stream()
                    .filter(h -> h.getSql() != null && mentionsTable(h.getSql(), table))
                    .count();
            tableCounts.put(table, count);
        }

        List<QueryHistoryEntry> recentRejections = history.stream()
                .filter(h -> !h.isValid())
                .limit(MAX_RECENT_REJECTIONS)
                .map(QueryHistoryEntry::from)
                .toList();

        return new AnalyticsResponse(total, valid, rejected, tableCounts, recentRejections);
    }

    private boolean mentionsTable(String sql, String table) {
        Pattern p = Pattern.compile("\\b" + Pattern.quote(table) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        return m.find();
    }
}
