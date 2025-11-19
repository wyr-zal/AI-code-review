package com.codereview.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 导出审查报告请求DTO
 * @author CodeReview
 */
@Data
@Schema(description = "导出审查报告请求")
public class ExportReportRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID列表
     */
    @NotEmpty(message = "至少需要选择一个任务进行导出")
    @Schema(description = "任务ID列表", example = "[1, 2, 3]", required = true)
    private List<Long> taskIds;

    /**
     * 导出格式（pdf, excel）
     */
    @Schema(description = "导出格式", example = "pdf", defaultValue = "pdf", allowableValues = {"pdf", "excel"})
    private String format = "pdf";

    /**
     * 是否包含详细信息
     */
    @Schema(description = "是否包含详细信息", example = "true", defaultValue = "true")
    private Boolean includeDetails = true;

    /**
     * 文件名（可选）
     */
    @Schema(description = "导出文件名（不含扩展名）", example = "code_review_report")
    private String fileName;
}