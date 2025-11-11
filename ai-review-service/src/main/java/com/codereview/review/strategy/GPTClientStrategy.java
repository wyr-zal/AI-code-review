package com.codereview.review.strategy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * GPT AI客户端策略实现
 * @author CodeReview
 */
@Slf4j
@Component
public class GPTClientStrategy implements AIClientStrategy {

    @Value("${ai.openai.api-key:your-openai-api-key}")
    private String apiKey;

    @Value("${ai.openai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.openai.model:gpt-3.5-turbo}")
    private String model;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public String reviewCode(String code, String language) {
        try {
            String prompt = buildReviewPrompt(code, language);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一位资深的代码审查专家，擅长发现代码中的问题、性能优化点和安全漏洞。");
            messages.add(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            RequestBody body = RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "无响应体";
                    log.error("iFlow API调用失败 - 状态码: {}, 响应: {}", response.code(), errorBody);
                    return "AI审查失败：API调用错误（状态码：" + response.code() + "）";
                }

                String responseBody = null;
                if (response.body() != null) {
                    responseBody = response.body().string();
                }
                log.debug("iFlow API响应: {}", responseBody);
                JSONObject jsonResponse = JSON.parseObject(responseBody);

                if (jsonResponse.containsKey("choices")) {
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.size() > 0) {
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject message = firstChoice.getJSONObject("message");
                        return message.getString("content");
                    }
                }

                log.error("iFlow API响应格式错误: {}", responseBody);
                return "AI审查失败：响应格式错误";
            }
        } catch (Exception e) {
            log.error("调用iFlow API异常", e);
            return "AI审查失败：" + e.getMessage();
        }
    }

    @Override
    public String getModelName() {
        return "gpt";
    }

    /**
     * 构建代码审查提示词
     */
    private String buildReviewPrompt(String code, String language) {
        return String.format(
            "请对以下%s代码进行全面审查，并提供详细的分析报告：\n\n" +
            "```%s\n%s\n```\n\n" +
            "请从以下几个方面进行审查：\n" +
            "1. **代码质量**：检查代码的可读性、可维护性、命名规范等\n" +
            "2. **潜在Bug**：识别可能导致错误的代码模式\n" +
            "3. **性能优化**：指出可以优化的性能瓶颈\n" +
            "4. **安全漏洞**：检查SQL注入、XSS、敏感信息泄露等安全问题\n" +
            "5. **最佳实践**：给出符合业界最佳实践的改进建议\n\n" +
            "请以结构化的JSON格式返回结果，包含以下字段：\n" +
            "{\n" +
            "  \"summary\": \"总体评价\",\n" +
            "  \"qualityScore\": 质量评分(0-100),\n" +
            "  \"securityScore\": 安全评分(0-100),\n" +
            "  \"performanceScore\": 性能评分(0-100),\n" +
            "  \"issues\": [\n" +
            "    {\n" +
            "      \"type\": \"问题类型\",\n" +
            "      \"severity\": \"严重程度(high/medium/low)\",\n" +
            "      \"description\": \"问题描述\",\n" +
            "      \"suggestion\": \"修复建议\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"suggestions\": [\"优化建议1\", \"优化建议2\"]\n" +
            "}",
            language, language, code
        );
    }
}
