package com.codereview.review.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.codereview.review.dto.CodeReviewRequestDTO;
import com.codereview.review.entity.ReviewTask;
import com.codereview.review.mapper.ReviewTaskMapper;
import com.codereview.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 代码审查消息监听器
 * @author CodeReview
 */
@Slf4j
@Component
public class ReviewTaskListener {

    @Resource
    private ReviewService reviewService;

    @Resource
    private ReviewTaskMapper reviewTaskMapper;

    /**
     * 监听代码审查队列
     */
    @RabbitListener(queuesToDeclare = @Queue(name = "code.review.queue", durable = "true"))
    public void handleReviewTask(String message) {
        try {
            log.info("收到代码审查任务: {}", message);

            JSONObject jsonMessage = JSON.parseObject(message);
            Long taskId = jsonMessage.getLong("taskId");

            // 获取任务详情
            ReviewTask task = reviewTaskMapper.selectById(taskId);
            if (task == null) {
                log.error("任务不存在: taskId={}", taskId);
                return;
            }

            // 构建DTO并执行审查
            CodeReviewRequestDTO dto = new CodeReviewRequestDTO();
            dto.setTitle(task.getTitle());
            dto.setCodeContent(task.getCodeContent());
            dto.setLanguage(task.getLanguage());
            dto.setAiModel(task.getAiModel());
            dto.setAsync(false);

            reviewService.executeSyncReview(dto, task.getUserId());

            log.info("代码审查任务处理完成: taskId={}", taskId);

        } catch (Exception e) {
            log.error("处理代码审查任务失败", e);
        }
    }
}
