package com.codereview.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 用户注册DTO
 * @author CodeReview
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,16}$", message = "用户名长度4-16位，只能包含字母、数字、下划线")
    @Schema(description = "用户名（4-16位字母、数字、下划线）", example = "testuser", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度6-20位")
    @Schema(description = "密码（6-20位）", example = "123456", required = true)
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "test@example.com", required = true)
    private String email;

    @Schema(description = "用户昵称", example = "测试用户")
    private String nickname;
}
