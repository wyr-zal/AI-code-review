package com.codereview.review.controller;

import com.codereview.common.result.Result;
import com.codereview.common.utils.UserContextHolder;
import com.codereview.review.dto.*;
import com.codereview.review.entity.ReviewTask;
import com.codereview.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 代码审查控制器
 * @author CodeReview
 */
@Slf4j
@RestController
@RequestMapping("/review")
@Tag(name = "代码审查", description = "代码审查相关接口")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    /**
     * 提交代码审查任务
     */
    @Operation(summary = "提交代码审查任务（异步）", description = "提交代码到队列进行异步审查，返回任务ID")
    @PostMapping("/submit")
    public Result<Long> submitReview(
            @Parameter(description = "代码审查请求", required = true) @Valid @RequestBody CodeReviewRequestDTO dto) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        Long taskId = reviewService.submitReviewTask(dto, Long.parseLong(userId));
        return Result.success("任务提交成功", taskId);
    }

    /**
     * 批量提交代码审查任务
     */
    @Operation(summary = "批量提交代码审查任务", description = "批量提交多个文件进行代码审查")
    @PostMapping(value = "/batch", consumes = "multipart/form-data")
    public Result<List<Long>> submitBatchReview(BatchReviewMultipartDTO dto) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        List<Long> taskIds = reviewService.submitBatchReviewTask(dto.getTitle(), dto.getLanguage(), dto.getAiModel(), dto.getAsync(), dto.getFiles(), Long.parseLong(userId));
        return Result.success("批量任务提交成功", taskIds);
    }

    /**
     * 同步执行代码审查
     */
    @Operation(summary = "同步代码审查", description = "立即执行代码审查并返回结果，耗时较长")
    @PostMapping("/sync")
    public Result<ReviewTask> syncReview(
            @Parameter(description = "代码审查请求", required = true) @Valid @RequestBody CodeReviewRequestDTO dto) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        ReviewTask task = reviewService.executeSyncReview(dto, Long.parseLong(userId));
        return Result.success("审查完成", task);
    }

    /**
     * 获取审查任务详情
     */
    @Operation(summary = "获取任务详情", description = "根据任务ID获取代码审查任务的详细信息")
    @GetMapping("/task/{taskId}")
    public Result<ReviewTask> getTaskDetail(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        ReviewTask task = reviewService.getTaskDetail(taskId, Long.parseLong(userId));
        return Result.success(task);
    }

    /**
     * 获取用户的审查任务列表
     */
    @Operation(summary = "获取审查任务列表", description = "分页查询当前用户的所有代码审查任务")
    @GetMapping("/tasks")
    public Result<PageResponseDTO<ReviewTask>> getUserTasks(ReviewTaskQueryDTO queryDTO) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        PageResponseDTO<ReviewTask> tasks = reviewService.getUserTasks(Long.parseLong(userId), queryDTO);
        return Result.success(tasks);
    }

    /**
     * 删除审查任务
     */
    @Operation(summary = "删除审查任务", description = "删除指定的代码审查任务")
    @DeleteMapping("/task/{taskId}")
    public Result<String> deleteTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        reviewService.deleteTask(taskId, Long.parseLong(userId));
        return Result.success("删除成功", "删除成功");
    }

    /**
     * 导出审查报告
     */
    @Operation(summary = "导出审查报告", description = "导出指定任务的审查报告（支持PDF和Excel格式）")
    @PostMapping("/export")
    public void exportReviewReport(
            @Parameter(description = "导出报告请求", required = true) @Valid @RequestBody ExportReportRequestDTO dto,
            HttpServletResponse response) {
        String userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            try {
                response.setStatus(401);
                response.getWriter().write("用户未登录");
                response.getWriter().flush();
            } catch (Exception e) {
                log.error("写入响应失败", e);
            }
            return;
        }
        reviewService.exportReviewReport(dto.getTaskIds(), dto.getFormat(), dto.getIncludeDetails(), Long.parseLong(userId), response);
    }
}
