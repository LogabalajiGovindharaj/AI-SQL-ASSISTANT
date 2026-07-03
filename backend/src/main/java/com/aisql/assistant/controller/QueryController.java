package com.aisql.assistant.controller;

import com.aisql.assistant.dto.ExecuteQueryRequest;
import com.aisql.assistant.dto.ExecuteQueryResponse;
import com.aisql.assistant.dto.GenerateQueryRequest;
import com.aisql.assistant.dto.GenerateQueryResponse;
import com.aisql.assistant.service.QueryOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryController {

    private final QueryOrchestratorService orchestratorService;

    public QueryController(QueryOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    /** Question -> intent -> SQL -> validation. Does not touch the database. */
    @PostMapping("/generate-query")
    public ResponseEntity<GenerateQueryResponse> generateQuery(@Valid @RequestBody GenerateQueryRequest request) {
        GenerateQueryResponse response = orchestratorService.generate(request.getQuestion());
        return ResponseEntity.ok(response);
    }

    /** Re-validates, executes against MySQL, and returns rows + an insight. */
    @PostMapping("/execute-query")
    public ResponseEntity<ExecuteQueryResponse> executeQuery(@Valid @RequestBody ExecuteQueryRequest request) {
        ExecuteQueryResponse response = orchestratorService.execute(request.getSql(), request.getQuestion());
        return ResponseEntity.ok(response);
    }
}
