package com.codereview.common.utils;

import lombok.Data;

/**
 * 用户信息上下文
 * @author CodeReview
 */
@Data
public class UserContext {
    private String userId;
    private String username;
    private String nickname;
    private String email;
    private String originalToken;

    public UserContext(String userId, String username, String nickname, String email, String originalToken) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.originalToken = originalToken;
    }

    public static UserContext of(String userId, String username, String nickname, String email) {
        return new UserContext(userId, username, nickname, email, null);
    }

    public static UserContext of(String userId, String username, String nickname, String email, String originalToken) {
        return new UserContext(userId, username, nickname, email, originalToken);
    }
}