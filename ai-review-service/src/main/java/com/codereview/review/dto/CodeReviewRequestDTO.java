package com.codereview.review.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 代码审查请求DTO
 * @author CodeReview
 */
@Data
public class CodeReviewRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    private String title;

    /**
     * 代码内容
     */
    @NotBlank(message = "代码内容不能为空")
    private String codeContent;

    /**
     * 编程语言（如：Java, Python, JavaScript等）
     */
    @NotBlank(message = "编程语言不能为空")
    private String language;

    /**
     * AI模型（gpt-4, gpt-3.5-turbo, claude-3-opus）
     */
    private String aiModel = "gpt-3.5-turbo";

    /**
     * 是否异步审查（true-异步，false-同步）
     */
    private Boolean async = true;
}
