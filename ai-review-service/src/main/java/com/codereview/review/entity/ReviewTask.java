package com.codereview.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 代码审查任务实体
 * @author CodeReview
 */
@Data
@TableName("review_task")
public class ReviewTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 代码内容
     */
    private String codeContent;

    /**
     * 编程语言
     */
    private String language;

    /**
     * AI模型
     */
    private String aiModel;

    /**
     * 审查状态（0-待审查，1-审查中，2-已完成，3-审查失败）
     */
    private Integer status;

    /**
     * 审查结果
     */
    private String reviewResult;

    /**
     * 质量评分（0-100）
     */
    private Integer qualityScore;

    /**
     * 安全评分（0-100）
     */
    private Integer securityScore;

    /**
     * 性能评分（0-100）
     */
    private Integer performanceScore;

    /**
     * 问题数量
     */
    private Integer issueCount;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
