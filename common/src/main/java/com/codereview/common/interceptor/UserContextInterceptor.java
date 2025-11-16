package com.codereview.common.interceptor;

import com.codereview.common.utils.UserContext;
import com.codereview.common.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * 用户信息拦截器
 * 从请求头解析用户信息并保存到ThreadLocal中
 * @author CodeReview
 */
@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_NICKNAME = "X-Nickname";
    public static final String HEADER_EMAIL = "X-Email";
    public static final String HEADER_ORIGINAL_TOKEN = "X-Original-Token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("=== UserContextInterceptor开始处理请求 ===");
        log.debug("请求URL: {}", request.getRequestURL());
        log.debug("请求URI: {}", request.getRequestURI());
        log.debug("请求方法: {}", request.getMethod());
        log.debug("请求参数: {}", request.getQueryString());
        
        // 打印所有请求头，便于调试
        if (log.isDebugEnabled()) {
            log.debug("所有请求头信息:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                log.debug("  {}: {}", headerName, headerValue);
            }
        }

        // 从请求头获取用户信息
        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);
        String nickname = request.getHeader(HEADER_NICKNAME);
        String email = request.getHeader(HEADER_EMAIL);
        String originalToken = request.getHeader(HEADER_ORIGINAL_TOKEN);

        log.debug("解析到的用户信息:");
        log.debug("  X-User-Id: {}", userId);
        log.debug("  X-Username: {}", username);
        log.debug("  X-Nickname: {}", nickname);
        log.debug("  X-Email: {}", email);
        log.debug("  X-Original-Token: {}", originalToken);

        // 检查用户信息是否完整
        boolean hasUserInfo = userId != null && !userId.isEmpty();
        log.debug("是否存在用户信息: {}", hasUserInfo);

        // 如果存在用户信息，则设置到上下文中
        if (hasUserInfo) {
            UserContext userContext = UserContext.of(userId, username, nickname, email, originalToken);
            UserContextHolder.setCurrentUser(userContext);
            log.debug("成功设置用户上下文: userId={}, username={}", userId, username);
        } else {
            log.warn("未找到用户信息或用户ID为空，清除上下文 - 请求路径: {}", request.getRequestURI());
            // 清除上下文，防止线程复用时出现脏数据
            UserContextHolder.clear();
        }

        log.debug("=== UserContextInterceptor处理完成 ===");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清理上下文，防止内存泄漏
        UserContextHolder.clear();
        log.debug("清理用户上下文 - 请求完成");
    }
}