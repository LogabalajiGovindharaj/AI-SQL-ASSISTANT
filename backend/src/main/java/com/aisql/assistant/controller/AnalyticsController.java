package com.aisql.assistant.controller;

import com.aisql.assistant.dto.AnalyticsResponse;
import com.aisql.assistant.service.AnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /** Every authenticated user can see their own usage stats. */
    @GetMapping("/analytics")
    public AnalyticsResponse analytics(Authentication authentication) {
        String email = String.valueOf(authentication.getPrincipal());
        return analyticsService.forUser(email);
    }

    /** ADMIN only - see usage across every user. Enforced in SecurityConfig, not just hidden in the UI. */
    @GetMapping("/admin/analytics")
    public AnalyticsResponse adminAnalytics() {
        return analyticsService.forAllUsers();
    }
}
