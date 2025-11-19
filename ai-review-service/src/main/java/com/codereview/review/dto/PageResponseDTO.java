package com.codereview.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应DTO
 * @author CodeReview
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应结果")
public class PageResponseDTO<T> {

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> records;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Long total;

    /**
     * 当前页
     */
    @Schema(description = "当前页码", example = "1")
    private Integer page;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    private Integer size;
}
