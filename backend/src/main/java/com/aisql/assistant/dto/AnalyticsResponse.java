package com.aisql.assistant.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {

    private long totalQueries;
    private long validQueries;
    private long rejectedQueries;
    private Map<String, Long> mostQueriedTables;
    private List<QueryHistoryEntry> recentRejections;

    public AnalyticsResponse(long totalQueries, long validQueries, long rejectedQueries,
                              Map<String, Long> mostQueriedTables, List<QueryHistoryEntry> recentRejections) {
        this.totalQueries = totalQueries;
        this.validQueries = validQueries;
        this.rejectedQueries = rejectedQueries;
        this.mostQueriedTables = mostQueriedTables;
        this.recentRejections = recentRejections;
    }

    public long getTotalQueries() {
        return totalQueries;
    }

    public long getValidQueries() {
        return validQueries;
    }

    public long getRejectedQueries() {
        return rejectedQueries;
    }

    public Map<String, Long> getMostQueriedTables() {
        return mostQueriedTables;
    }

    public List<QueryHistoryEntry> getRecentRejections() {
        return recentRejections;
    }
}
