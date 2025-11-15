package com.codereview.common.utils;

/**
 * 用户上下文工具类，用于在请求线程中存储和获取用户信息
 * @author CodeReview
 */
public class UserContextHolder {
    private static final ThreadLocal<UserContext> userContextThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户上下文
     */
    public static void setCurrentUser(UserContext userContext) {
        userContextThreadLocal.set(userContext);
    }

    /**
     * 获取当前用户上下文
     */
    public static UserContext getCurrentUser() {
        return userContextThreadLocal.get();
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        UserContext userContext = userContextThreadLocal.get();
        return userContext != null ? userContext.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        UserContext userContext = userContextThreadLocal.get();
        return userContext != null ? userContext.getUsername() : null;
    }

    /**
     * 获取当前用户昵称
     */
    public static String getCurrentNickname() {
        UserContext userContext = userContextThreadLocal.get();
        return userContext != null ? userContext.getNickname() : null;
    }

    /**
     * 获取当前用户邮箱
     */
    public static String getCurrentEmail() {
        UserContext userContext = userContextThreadLocal.get();
        return userContext != null ? userContext.getEmail() : null;
    }

    /**
     * 获取原始Token
     */
    public static String getOriginalToken() {
        UserContext userContext = userContextThreadLocal.get();
        return userContext != null ? userContext.getOriginalToken() : null;
    }

    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        userContextThreadLocal.remove();
    }
}