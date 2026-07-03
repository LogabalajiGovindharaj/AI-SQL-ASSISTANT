package com.aisql.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the "claude.*" properties from application.yml.
 * The API key is read from the ANTHROPIC_API_KEY environment variable and is
 * never sent to, or accepted from, the frontend.
 */
@Configuration
@ConfigurationProperties(prefix = "claude")
public class ClaudeApiConfig {

    private String apiKey;
    private String model = "claude-sonnet-5";
    private String baseUrl = "https://api.anthropic.com/v1/messages";
    private int maxTokens = 512;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
}
