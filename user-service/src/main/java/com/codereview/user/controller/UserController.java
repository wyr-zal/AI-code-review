package com.codereview.user.controller;

import com.codereview.common.result.Result;
import com.codereview.common.utils.UserContextHolder;
import com.codereview.user.dto.UserLoginDTO;
import com.codereview.user.dto.UserRegisterDTO;
import com.codereview.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "用户注册",
            description = "新用户注册接口，需提供用户名、密码、邮箱等信息。用户名长度4-16位，密码长度6-20位。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "注册成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": 200, \"message\": \"注册成功\", \"data\": \"注册成功\"}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "500", description = "系统内部错误")
    })
    @PostMapping("/register")
    public Result<String> register(
            @Parameter(description = "用户注册信息", required = true) @Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", "注册成功");
    }

    /**
     * 用户登录
     */
    @Operation(
            summary = "用户登录",
            description = "用户登录接口，验证通过后返回JWT token和用户基本信息。Token有效期24小时。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登录成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": 200, \"message\": \"登录成功\", \"data\": {\"token\": \"eyJhbGciOiJIUzI1NiJ9...\", \"userId\": 1, \"username\": \"admin\", \"nickname\": \"管理员\", \"email\": \"admin@example.com\"}}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "500", description = "系统内部错误")
    })
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "用户登录信息", required = true) @Valid @RequestBody UserLoginDTO dto) {
        Map<String, Object> result = userService.login(dto);
        return Result.success("登录成功", result);
    }

    /**
     * 用户登出
     */
    @Operation(
            summary = "用户登出",
            description = "用户退出登录，清除Redis中的token缓存。需要在请求头中携带token。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登出成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": 200, \"message\": \"登出成功\", \"data\": \"登出成功\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "用户未登录"),
            @ApiResponse(responseCode = "500", description = "系统内部错误")
    })
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
    @Operation(
            summary = "获取用户信息",
            description = "根据token获取当前登录用户的详细信息。需要在请求头中携带token。"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": 200, \"message\": \"success\", \"data\": {\"userId\": 1, \"username\": \"admin\", \"nickname\": \"管理员\", \"email\": \"admin@example.com\", \"role\": 1, \"createTime\": \"2025-11-01 10:00:00\"}}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "用户未登录"),
            @ApiResponse(responseCode = "500", description = "系统内部错误")
    })
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
