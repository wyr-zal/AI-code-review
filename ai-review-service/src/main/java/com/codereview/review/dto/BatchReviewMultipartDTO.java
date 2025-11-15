package com.codereview.review.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 批量文件审查请求DTO（用于multipart表单提交）
 * @author CodeReview
 */
@Data
public class BatchReviewMultipartDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标题前缀
     */
    @NotBlank(message = "任务标题不能为空")
    private String title;

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

    /**
     * 批量上传的文件列表
     */
    @NotEmpty(message = "至少需要上传一个文件")
    private List<MultipartFile> files;
}