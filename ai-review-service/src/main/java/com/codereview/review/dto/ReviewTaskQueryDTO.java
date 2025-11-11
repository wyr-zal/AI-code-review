package com.codereview.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 审查任务查询DTO
 * @author CodeReview
 */
@Data
@Schema(description = "审查任务查询参数")
public class ReviewTaskQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    /**
     * 审查状态（0-待审查，1-审查中，2-已完成，3-失败）
     */
    @Schema(description = "审查状态", example = "0")
    private Integer status;

    /**
     * 编程语言
     */
    @Schema(description = "编程语言", example = "Java")
    private String language;
}
