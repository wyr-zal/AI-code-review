package com.codereview.gateway.filter;

import com.codereview.common.constant.RedisConstants;
import com.codereview.gateway.config.RateLimitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * API 限流过滤器
 * 基于 Redis 实现滑动窗口限流算法
 * @author CodeReview
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 如果限流未启用，直接放行
        if (!rateLimitConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 获取适用的限流规则
        RateLimitConfig.LimitRule rule = getApplicableRule(path);
        if (rule == null) {
            return chain.filter(exchange);
        }

        // 生成限流键
        String limitKey = generateLimitKey(exchange, rule);

        // 执行限流检查
        return checkRateLimit(limitKey, rule)
                .flatMap(allowed -> {
                    if (allowed) {
                        log.debug("请求通过限流检查: path={}, key={}", path, limitKey);
                        return chain.filter(exchange);
                    } else {
                        log.warn("请求被限流拦截: path={}, key={}", path, limitKey);
                        return rateLimitExceededResponse(exchange);
                    }
                });
    }

    /**
     * 获取适用的限流规则
     */
    private RateLimitConfig.LimitRule getApplicableRule(String path) {
        // 检查是否有路径特定规则
        for (Map.Entry<String, RateLimitConfig.LimitRule> entry : rateLimitConfig.getRules().entrySet()) {
            String pattern = entry.getKey();
            if (pathMatches(path, pattern)) {
                log.debug("找到路径特定规则: path={}, pattern={}", path, pattern);
                return entry.getValue();
            }
        }
        // 使用默认规则
        return rateLimitConfig.getDefaultRule();
    }

    /**
     * 路径匹配
     */
    private boolean pathMatches(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        } else if (pattern.contains("*")) {
            return path.matches(pattern.replace("*", ".*"));
        } else {
            return path.equals(pattern);
        }
    }

    /**
     * 生成限流键
     */
    private String generateLimitKey(ServerWebExchange exchange, RateLimitConfig.LimitRule rule) {
        String path = exchange.getRequest().getURI().getPath();
        String identifier;

        switch (rule.getType().toUpperCase()) {
            case "USER":
                // 基于用户限流
                identifier = exchange.getRequest().getHeaders().getFirst("X-User-Id");
                if (identifier == null || identifier.isEmpty()) {
                    // 如果没有用户ID，降级为IP限流
                    identifier = getClientIp(exchange);
                }
                break;
            case "GLOBAL":
                // 全局限流
                identifier = "global";
                break;
            case "IP":
            default:
                // 基于IP限流
                identifier = getClientIp(exchange);
                break;
        }

        return RedisConstants.RATE_LIMIT_KEY + rule.getType().toLowerCase() + ":" + path + ":" + identifier;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个IP才是真实IP
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
    }

    /**
     * 检查限流
     * 使用滑动窗口计数器算法
     */
    private Mono<Boolean> checkRateLimit(String key, RateLimitConfig.LimitRule rule) {
        long now = System.currentTimeMillis();
        long windowStart = now - rule.getWindowSeconds() * 1000L;

        return reactiveRedisTemplate.opsForZSet()
                // 移除窗口外的旧记录
                .removeRangeByScore(key, 0, windowStart)
                .then(reactiveRedisTemplate.opsForZSet().count(key, windowStart, now))
                .flatMap(count -> {
                    if (count < rule.getMaxRequests()) {
                        // 未超限，记录本次请求
                        return reactiveRedisTemplate.opsForZSet()
                                .add(key, String.valueOf(now), now)
                                .then(reactiveRedisTemplate.expire(key, Duration.ofSeconds(rule.getWindowSeconds())))
                                .thenReturn(true);
                    } else {
                        // 超限
                        return Mono.just(false);
                    }
                })
                .onErrorResume(e -> {
                    log.error("限流检查失败，默认放行: key={}", key, e);
                    // Redis故障时放行，避免影响业务
                    return Mono.just(true);
                });
    }

    /**
     * 返回限流响应
     */
    private Mono<Void> rateLimitExceededResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("X-RateLimit-Exceeded", "true");
        return exchange.getResponse()
                .writeWith(Mono.fromCallable(() ->
                    exchange.getResponse().bufferFactory().wrap("请求过于频繁，请稍后再试".getBytes())
                ));
    }

    @Override
    public int getOrder() {
        // 在认证过滤器之后执行
        return -900;
    }
}
