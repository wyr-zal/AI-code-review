package com.codereview.review.controller;

import com.codereview.common.result.Result;
import com.codereview.common.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息测试控制器
 * 演示如何在服务中使用用户上下文
 * @author CodeReview
 */
@Slf4j
@RestController
@RequestMapping("/user-context")
@Tag(name = "用户上下文测试", description = "用户上下文功能测试接口")
public class UserContextTestController {

    @Operation(summary = "获取当前用户信息", description = "从ThreadLocal中获取当前用户信息")
    @GetMapping("/current")
    public Result<?> getCurrentUser() {
        String userId = UserContextHolder.getCurrentUserId();
        String username = UserContextHolder.getCurrentUsername();
        String nickname = UserContextHolder.getCurrentNickname();
        String email = UserContextHolder.getCurrentEmail();

        if (userId == null) {
            return Result.error("当前用户未登录或未通过网关认证");
        }

        return Result.success("获取成功", UserContextHolder.getCurrentUser());
    }
}