package com.aisql.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Talks to Gemini's generateContent endpoint (Google AI Studio / Gemini API).
 *
 * Uses forced function calling (tool_config.mode = ANY, restricted to one
 * allowed function) so Gemini *must* respond with a structured function call
 * instead of free-form prose - same idea as Claude's forced tool_choice.
 *
 * Reads config directly from environment variables so it works as a
 * drop-in replacement without needing changes to ClaudeApiConfig.java:
 *   GEMINI_API_KEY   - required, your Gemini API key
 *   GEMINI_MODEL     - optional, defaults to "gemini-2.0-flash"
 */
@Service
public class ClaudeApiService {

    private static final String TOOL_NAME = "generate_sql";
    private static final String INSIGHT_TOOL_NAME = "generate_insight";

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public ClaudeApiService() {
        this.apiKey = System.getenv("GEMINI_API_KEY");
        String envModel = System.getenv("GEMINI_MODEL");
        this.model = (envModel != null && !envModel.isBlank()) ? envModel : "gemini-2.0-flash";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not set. " +
                    "Get a key at https://aistudio.google.com/apikey and set it before starting the backend.");
        }
    }

    public JsonNode generateSql(String systemPrompt, String userQuestion) {
        ObjectNode requestBody = buildRequestBody(systemPrompt, userQuestion, buildGenerateSqlTool(), TOOL_NAME);
        return sendFunctionCall(requestBody, TOOL_NAME);
    }

    public JsonNode generateInsight(String systemPrompt, String userContent) {
        ObjectNode requestBody = buildRequestBody(systemPrompt, userContent, buildGenerateInsightTool(), INSIGHT_TOOL_NAME);
        return sendFunctionCall(requestBody, INSIGHT_TOOL_NAME);
    }

    private JsonNode sendFunctionCall(ObjectNode requestBody, String expectedToolName) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("content-type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Gemini API returned HTTP " + response.statusCode() + ": " + response.body());
            }
            return extractFunctionArgs(response.body(), expectedToolName);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private ObjectNode buildRequestBody(String systemPrompt, String userContent, ObjectNode functionDecl, String functionName) {
        ObjectNode root = mapper.createObjectNode();

        ObjectNode systemInstruction = mapper.createObjectNode();
        ArrayNode systemParts = systemInstruction.putArray("parts");
        ObjectNode systemPart = mapper.createObjectNode();
        systemPart.put("text", systemPrompt);
        systemParts.add(systemPart);
        root.set("system_instruction", systemInstruction);

        ArrayNode contents = root.putArray("contents");
        ObjectNode userContentNode = mapper.createObjectNode();
        userContentNode.put("role", "user");
        ArrayNode userParts = userContentNode.putArray("parts");
        ObjectNode userPart = mapper.createObjectNode();
        userPart.put("text", userContent);
        userParts.add(userPart);
        contents.add(userContentNode);

        ArrayNode tools = root.putArray("tools");
        ObjectNode toolWrapper = mapper.createObjectNode();
        ArrayNode functionDeclarations = toolWrapper.putArray("function_declarations");
        functionDeclarations.add(functionDecl);
        tools.add(toolWrapper);

        ObjectNode toolConfig = mapper.createObjectNode();
        ObjectNode functionCallingConfig = mapper.createObjectNode();
        functionCallingConfig.put("mode", "ANY");
        ArrayNode allowedNames = functionCallingConfig.putArray("allowed_function_names");
        allowedNames.add(functionName);
        toolConfig.set("function_calling_config", functionCallingConfig);
        root.set("tool_config", toolConfig);

        return root;
    }

    private ObjectNode buildGenerateInsightTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", INSIGHT_TOOL_NAME);
        tool.put("description",
                "Given a user's question and a summary of the query results, return a short " +
                "insight sentence and 2-3 relevant follow-up questions the user could ask next.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = mapper.createObjectNode();

        ObjectNode insightProp = mapper.createObjectNode();
        insightProp.put("type", "string");
        insightProp.put("description", "1-2 sentence plain-English insight about the results.");
        properties.set("insight", insightProp);

        ObjectNode suggestionsProp = mapper.createObjectNode();
        suggestionsProp.put("type", "array");
        ObjectNode suggestionItems = mapper.createObjectNode();
        suggestionItems.put("type", "string");
        suggestionsProp.set("items", suggestionItems);
        suggestionsProp.put("description", "2-3 natural-language follow-up questions.");
        properties.set("suggestions", suggestionsProp);

        schema.set("properties", properties);

        ArrayNode required = mapper.createArrayNode();
        required.add("insight");
        required.add("suggestions");
        schema.set("required", required);

        tool.set("parameters", schema);
        return tool;
    }

    private ObjectNode buildGenerateSqlTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", TOOL_NAME);
        tool.put("description",
                "Return the single read-only SQL SELECT statement that answers the user's " +
                "question, plus a one-sentence plain-English explanation of what it does.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = mapper.createObjectNode();

        ObjectNode sqlProp = mapper.createObjectNode();
        sqlProp.put("type", "string");
        sqlProp.put("description", "A single valid MySQL SELECT statement. No other statement types.");
        properties.set("sql", sqlProp);

        ObjectNode explanationProp = mapper.createObjectNode();
        explanationProp.put("type", "string");
        explanationProp.put("description", "One sentence explaining what the query returns.");
        properties.set("explanation", explanationProp);

        schema.set("properties", properties);

        ArrayNode required = mapper.createArrayNode();
        required.add("sql");
        required.add("explanation");
        schema.set("required", required);

        tool.set("parameters", schema);
        return tool;
    }

    private JsonNode extractFunctionArgs(String responseBody, String expectedToolName) throws Exception {
        JsonNode root = mapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        for (JsonNode candidate : candidates) {
            JsonNode parts = candidate.path("content").path("parts");
            for (JsonNode part : parts) {
                JsonNode functionCall = part.path("functionCall");
                if (!functionCall.isMissingNode()
                        && expectedToolName.equals(functionCall.path("name").asText())) {
                    return functionCall.path("args");
                }
            }
        }
        throw new IllegalStateException(
                "Gemini API response did not contain a " + expectedToolName + " function call: " + responseBody);
    }
}