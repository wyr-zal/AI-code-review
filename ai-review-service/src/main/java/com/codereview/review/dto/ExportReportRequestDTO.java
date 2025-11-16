package com.codereview.review.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 导出审查报告请求DTO
 * @author CodeReview
 */
@Data
public class ExportReportRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID列表
     */
    @NotEmpty(message = "至少需要选择一个任务进行导出")
    private List<Long> taskIds;

    /**
     * 导出格式（pdf, excel）
     */
    private String format = "pdf";

    /**
     * 是否包含详细信息
     */
    private Boolean includeDetails = true;
    
    /**
     * 文件名（可选）
     */
    private String fileName;
}