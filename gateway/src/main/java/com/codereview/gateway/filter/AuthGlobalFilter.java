package com.codereview.gateway.filter;

import com.codereview.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 全局认证过滤器
 * 验证JWT Token并将用户信息传递给下游服务
 * @author CodeReview
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    // 不需要认证的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/actuator/**",
            "/error"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 检查是否需要跳过认证
        if (isExcludePath(path)) {
            return chain.filter(exchange);
        }

        // 从请求头获取JWT Token
        String token = extractToken(exchange.getRequest());
        if (token == null || token.isEmpty()) {
            log.warn("请求路径: {} - 未提供JWT Token", path);
            return unauthorizedResponse(exchange);
        }

        try {
            // 验证Token并解析用户信息
            Claims claims = JwtUtils.parseToken(token);
            String userId = claims.getSubject();
            String username = (String) claims.get("username");
            String nickname = (String) claims.get("nickname");
            String email = (String) claims.get("email");

            // 将用户信息添加到请求头传递给下游服务
            ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
            builder.header("X-User-Id", userId);
            builder.header("X-Username", username);
            builder.header("X-Nickname", nickname);
            builder.header("X-Email", email);
            // 传递原始token，用于登出等操作
            builder.header("X-Original-Token", token);

            log.info("用户认证成功: userId={}, username={}, path={}", userId, username, path);

            return chain.filter(exchange.mutate().request(builder.build()).build());
        } catch (Exception e) {
            log.error("JWT Token验证失败: token={}, path={}", token, path, e);
            return unauthorizedResponse(exchange);
        }
    }

    /**
     * 检查路径是否需要排除认证
     */
    private boolean isExcludePath(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(p -> path.startsWith(p.replace("**", "")));
    }

    /**
     * 从请求头提取Token
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        return exchange.getResponse()
                .writeWith(Mono.fromCallable(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().bufferFactory().wrap("未授权访问，请先登录".getBytes());
                }));
    }

    @Override
    public int getOrder() {
        return -1000; // 设置高优先级，确保在其他过滤器之前执行
    }
}