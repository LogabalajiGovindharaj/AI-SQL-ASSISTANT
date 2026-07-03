package com.aisql.assistant.service;

import com.aisql.assistant.agent.InsightAgent;
import com.aisql.assistant.agent.OptimizationAgent;
import com.aisql.assistant.agent.SqlGenerationAgent;
import com.aisql.assistant.agent.ValidationAgent;
import com.aisql.assistant.dto.ExecuteQueryResponse;
import com.aisql.assistant.dto.GenerateQueryResponse;
import com.aisql.assistant.model.QueryHistory;
import com.aisql.assistant.repository.QueryHistoryRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Phase 3: /generate-query and /execute-query are now separate steps,
 * matching the original API contract. The client is expected to show the
 * generated SQL to the user (or just chain the calls automatically) before
 * executing it.
 *
 * Phase 7: every /generate-query call (accepted or rejected) is recorded to
 * QueryHistory for the authenticated user.
 */
@Service
public class QueryOrchestratorService {

    private final SqlGenerationAgent sqlGenerationAgent;
    private final ValidationAgent validationAgent;
    private final QueryExecutionService queryExecutionService;
    private final InsightAgent insightAgent;
    private final OptimizationAgent optimizationAgent;
    private final QueryHistoryRepository queryHistoryRepository;

    public QueryOrchestratorService(SqlGenerationAgent sqlGenerationAgent,
                                     ValidationAgent validationAgent,
                                     QueryExecutionService queryExecutionService,
                                     InsightAgent insightAgent,
                                     OptimizationAgent optimizationAgent,
                                     QueryHistoryRepository queryHistoryRepository) {
        this.sqlGenerationAgent = sqlGenerationAgent;
        this.validationAgent = validationAgent;
        this.queryExecutionService = queryExecutionService;
        this.insightAgent = insightAgent;
        this.optimizationAgent = optimizationAgent;
        this.queryHistoryRepository = queryHistoryRepository;
    }

    /** Agent 1+2 (Intent + SQL Generation) followed by Agent 3 (Validation). No execution. */
    public GenerateQueryResponse generate(String userQuestion) {
        SqlGenerationAgent.GeneratedSql generated = sqlGenerationAgent.generate(userQuestion);
        ValidationAgent.ValidationResult validation = validationAgent.validate(generated.sql());

        String currentUser = currentUserEmail();
        queryHistoryRepository.save(new QueryHistory(
                currentUser, userQuestion, generated.sql(), validation.valid(), validation.rejectionReason()));

        if (!validation.valid()) {
            return GenerateQueryResponse.rejected(generated.sql(), validation.rejectionReason());
        }
        return GenerateQueryResponse.accepted(generated.sql(), generated.explanation());
    }

    private String currentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? String.valueOf(auth.getPrincipal()) : "unknown";
    }

    /**
     * Agent 3 (Validation, re-run defensively - never trust a SQL string
     * handed back by a client) + Agent 4 (Execution) + Agent 5 (Insight).
     */
    public ExecuteQueryResponse execute(String sql, String originalQuestion) {
        ValidationAgent.ValidationResult validation = validationAgent.validate(sql);
        if (!validation.valid()) {
            return ExecuteQueryResponse.rejected(validation.rejectionReason());
        }

        List<Map<String, Object>> rows = queryExecutionService.execute(sql);
        OptimizationAgent.CostAnalysis cost = optimizationAgent.analyze(sql);

        String question = (originalQuestion == null || originalQuestion.isBlank())
                ? "Explain these results"
                : originalQuestion;
        InsightAgent.Insight insight = insightAgent.summarize(question, rows);

        return ExecuteQueryResponse.accepted(rows, insight.text(), insight.suggestions(),
                cost.estimatedRowsScanned(), cost.accessType(), cost.tips());
    }
}
