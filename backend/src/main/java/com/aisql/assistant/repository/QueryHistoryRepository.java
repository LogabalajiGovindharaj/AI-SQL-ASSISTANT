package com.aisql.assistant.repository;

import com.aisql.assistant.model.QueryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {
    List<QueryHistory> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
