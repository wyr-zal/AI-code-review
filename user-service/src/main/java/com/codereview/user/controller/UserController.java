package com.codereview.user.controller;

import com.codereview.common.result.Result;
import com.codereview.common.utils.UserContextHolder;
import com.codereview.user.dto.UserLoginDTO;
import com.codereview.user.dto.UserRegisterDTO;
import com.codereview.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * 用户控制器
 * @author CodeReview
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录、登出等相关接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "新用户注册接口，需提供用户名、密码等信息")
    @PostMapping("/register")
    public Result<String> register(
            @Parameter(description = "用户注册信息", required = true) @Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", "注册成功");
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户登录接口，返回token和用户基本信息")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "用户登录信息", required = true) @Valid @RequestBody UserLoginDTO dto) {
        Map<String, Object> result = userService.login(dto);
        return Result.success("登录成功", result);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户退出登录，清除token缓存")
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader(value = "X-Original-Token", required = false) String originalToken) {
        // 从UserContext获取用户信息
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        // 使用从UserContext获取的userId，而不是解析原始token
        userService.logout(userId);
        return Result.success("登出成功", "登出成功");
    }

    /**
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据token获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        Map<String, Object> userInfo = userService.getUserInfo(Long.parseLong(userId));
        return Result.success(userInfo);
    }
}
