package com.aisql.assistant.dto;

import java.util.List;
import java.util.Map;

public class ExecuteQueryResponse {

    private boolean valid;
    private String rejectionReason;
    private List<Map<String, Object>> rows;
    private String insight;
    private List<String> suggestions;
    private Long estimatedRowsScanned;
    private String accessType;
    private List<String> optimizationTips;

    public static ExecuteQueryResponse rejected(String reason) {
        ExecuteQueryResponse r = new ExecuteQueryResponse();
        r.valid = false;
        r.rejectionReason = reason;
        return r;
    }

    public static ExecuteQueryResponse accepted(List<Map<String, Object>> rows, String insight, List<String> suggestions,
                                                 Long estimatedRowsScanned, String accessType, List<String> optimizationTips) {
        ExecuteQueryResponse r = new ExecuteQueryResponse();
        r.valid = true;
        r.rows = rows;
        r.insight = insight;
        r.suggestions = suggestions;
        r.estimatedRowsScanned = estimatedRowsScanned;
        r.accessType = accessType;
        r.optimizationTips = optimizationTips;
        return r;
    }

    public boolean isValid() {
        return valid;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public String getInsight() {
        return insight;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public Long getEstimatedRowsScanned() {
        return estimatedRowsScanned;
    }

    public String getAccessType() {
        return accessType;
    }

    public List<String> getOptimizationTips() {
        return optimizationTips;
    }
}
