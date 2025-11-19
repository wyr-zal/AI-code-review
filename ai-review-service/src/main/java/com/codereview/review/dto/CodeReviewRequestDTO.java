package com.codereview.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 代码审查请求DTO
 * @author CodeReview
 */
@Data
@Schema(description = "代码审查请求")
public class CodeReviewRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    @Schema(description = "任务标题", example = "登录功能代码审查", required = true)
    private String title;

    /**
     * 代码内容
     */
    @NotBlank(message = "代码内容不能为空")
    @Schema(description = "待审查的代码内容", example = "public class UserService { ... }", required = true)
    private String codeContent;

    /**
     * 编程语言（如：Java, Python, JavaScript等）
     */
    @NotBlank(message = "编程语言不能为空")
    @Schema(description = "编程语言", example = "Java", required = true, allowableValues = {"Java", "Python", "JavaScript", "TypeScript", "Go", "C++", "C#", "Ruby", "PHP", "Rust"})
    private String language;

    /**
     * AI模型（gpt-4, gpt-3.5-turbo, claude-3-opus）
     */
    @Schema(description = "AI模型", example = "gpt-3.5-turbo", defaultValue = "gpt-3.5-turbo", allowableValues = {"gpt-4", "gpt-3.5-turbo", "claude-3-opus", "claude-3-sonnet"})
    private String aiModel = "gpt-3.5-turbo";

    /**
     * 是否异步审查（true-异步，false-同步）
     */
    @Schema(description = "是否异步审查", example = "true", defaultValue = "true")
    private Boolean async = true;
}
