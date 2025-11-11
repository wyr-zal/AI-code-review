package com.codereview.review.service;

import com.codereview.review.dto.CodeReviewRequestDTO;
import com.codereview.review.dto.PageResponseDTO;
import com.codereview.review.dto.ReviewTaskQueryDTO;
import com.codereview.review.entity.ReviewTask;

/**
 * 代码审查服务接口
 * @author CodeReview
 */
public interface ReviewService {

    /**
     * 提交代码审查任务
     */
    Long submitReviewTask(CodeReviewRequestDTO dto, Long userId);

    /**
     * 执行同步代码审查
     */
    ReviewTask executeSyncReview(CodeReviewRequestDTO dto, Long userId);

    /**
     * 执行异步审查（处理MQ队列中的任务）
     */
    void executeAsyncReview(Long taskId);

    /**
     * 获取审查任务详情
     */
    ReviewTask getTaskDetail(Long taskId);

    /**
     * 获取用户的审查任务列表
     */
    PageResponseDTO<ReviewTask> getUserTasks(Long userId, ReviewTaskQueryDTO queryDTO);

    /**
     * 删除审查任务
     */
    void deleteTask(Long taskId, Long userId);
}
