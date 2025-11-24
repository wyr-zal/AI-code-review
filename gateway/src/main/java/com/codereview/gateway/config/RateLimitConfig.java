package com.codereview.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 限流配置
 * @author CodeReview
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    /**
     * 是否启用限流
     */
    private boolean enabled = true;

    /**
     * 默认限流配置
     */
    private LimitRule defaultRule = new LimitRule();

    /**
     * 路径特定限流规则
     * key: 路径模式, value: 限流规则
     */
    private Map<String, LimitRule> rules = new HashMap<>();

    /**
     * 限流规则
     */
    @Data
    public static class LimitRule {
        /**
         * 时间窗口（秒）
         */
        private int windowSeconds = 60;

        /**
         * 窗口内最大请求数
         */
        private int maxRequests = 100;

        /**
         * 限流类型: IP, USER, GLOBAL
         */
        private String type = "IP";
    }
}
