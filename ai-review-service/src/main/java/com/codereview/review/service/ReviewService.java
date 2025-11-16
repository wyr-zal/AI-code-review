package com.codereview.review.service;

import com.codereview.review.dto.*;
import com.codereview.review.entity.ReviewTask;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
     * 批量提交代码审查任务
     */
    List<Long> submitBatchReviewTask(BatchReviewRequestDTO dto, Long userId);

    /**
     * 批量提交代码审查任务（用于multipart表单）
     */
    List<Long> submitBatchReviewTask(String title, String language, String aiModel, Boolean async, List<MultipartFile> files, Long userId);

    /**
     * 批量提交代码审查任务（使用Multipart DTO）
     */
    List<Long> submitBatchReviewTask(BatchReviewMultipartDTO dto, Long userId);

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
     * 获取审查任务详情（带用户验证）
     */
    ReviewTask getTaskDetail(Long taskId, Long userId);

    /**
     * 获取用户的审查任务列表
     */
    PageResponseDTO<ReviewTask> getUserTasks(Long userId, ReviewTaskQueryDTO queryDTO);

    /**
     * 删除审查任务
     */
    void deleteTask(Long taskId, Long userId);

    /**
     * 导出审查报告
     */
    void exportReviewReport(List<Long> taskIds, String format, Boolean includeDetails, Long userId, HttpServletResponse response);
    
    /**
     * 导出审查报告（带自定义文件名）
     */
    void exportReviewReport(List<Long> taskIds, String format, Boolean includeDetails, String fileName, Long userId, HttpServletResponse response);
}
