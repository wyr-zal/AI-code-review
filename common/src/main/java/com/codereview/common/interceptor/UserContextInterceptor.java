package com.codereview.common.interceptor;

import com.codereview.common.utils.UserContext;
import com.codereview.common.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户信息拦截器
 * 从请求头解析用户信息并保存到ThreadLocal中
 * @author CodeReview
 */
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_NICKNAME = "X-Nickname";
    public static final String HEADER_EMAIL = "X-Email";
    public static final String HEADER_ORIGINAL_TOKEN = "X-Original-Token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取用户信息
        String userId = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);
        String nickname = request.getHeader(HEADER_NICKNAME);
        String email = request.getHeader(HEADER_EMAIL);
        String originalToken = request.getHeader(HEADER_ORIGINAL_TOKEN);

        // 如果存在用户信息，则设置到上下文中
        if (userId != null && !userId.isEmpty()) {
            UserContext userContext = UserContext.of(userId, username, nickname, email, originalToken);
            UserContextHolder.setCurrentUser(userContext);
            log.debug("设置用户上下文: userId={}, username={}", userId, username);
        } else {
            // 清除上下文，防止线程复用时出现脏数据
            UserContextHolder.clear();
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清理上下文，防止内存泄漏
        UserContextHolder.clear();
        log.debug("清理用户上下文");
    }
}