package com.aisql.assistant.controller;

import com.aisql.assistant.dto.QueryHistoryEntry;
import com.aisql.assistant.repository.QueryHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HistoryController {

    private final QueryHistoryRepository queryHistoryRepository;

    public HistoryController(QueryHistoryRepository queryHistoryRepository) {
        this.queryHistoryRepository = queryHistoryRepository;
    }

    @GetMapping("/history")
    public List<QueryHistoryEntry> history(Authentication authentication) {
        String email = String.valueOf(authentication.getPrincipal());
        return queryHistoryRepository.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(QueryHistoryEntry::from)
                .toList();
    }
}
