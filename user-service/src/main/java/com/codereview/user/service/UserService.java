package com.codereview.user.service;

import com.codereview.user.dto.UserLoginDTO;
import com.codereview.user.dto.UserRegisterDTO;

import java.util.Map;

/**
 * 用户服务接口
 * @author CodeReview
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    Map<String, Object> login(UserLoginDTO dto);

    /**
     * 用户登出
     */
    void logout(String userId);

    /**
     * 获取用户信息
     */
    Map<String, Object> getUserInfo(Long userId);
}
