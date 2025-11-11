package com.codereview.review.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codereview.common.constant.RedisConstants;
import com.codereview.common.enums.ReviewStatusEnum;
import com.codereview.common.exception.BusinessException;
import com.codereview.common.utils.RedisUtils;
import com.codereview.review.dto.CodeReviewRequestDTO;
import com.codereview.review.dto.PageResponseDTO;
import com.codereview.review.entity.ReviewTask;
import com.codereview.review.mapper.ReviewTaskMapper;
import com.codereview.review.service.ReviewService;
import com.codereview.review.strategy.AIClientFactory;
import com.codereview.review.strategy.AIClientStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 代码审查服务实现
 * @author CodeReview
 */
@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewTaskMapper reviewTaskMapper;

    @Resource
    private AIClientFactory aiClientFactory;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisUtils redisUtils;

    private static final String REVIEW_QUEUE = "code.review.queue";

    @Override
    public Long submitReviewTask(CodeReviewRequestDTO dto, Long userId) {
        // 创建审查任务
        ReviewTask task = new ReviewTask();
        task.setUserId(userId);
        task.setTitle(dto.getTitle());
        task.setCodeContent(dto.getCodeContent());
        task.setLanguage(dto.getLanguage());
        task.setAiModel(dto.getAiModel());
        task.setStatus(ReviewStatusEnum.PENDING.getCode());

        reviewTaskMapper.insert(task);
        log.info("创建代码审查任务: taskId={}, userId={}", task.getId(), userId);

        // 如果是异步审查，发送到消息队列
        if (dto.getAsync()) {
            JSONObject message = new JSONObject();
            message.put("taskId", task.getId());
            message.put("userId", userId);
            rabbitTemplate.convertAndSend(REVIEW_QUEUE, message.toJSONString());
            log.info("任务已发送到消息队列: taskId={}", task.getId());
        } else {
            // 同步执行审查
            executeReview(task);
        }

        return task.getId();
    }

    @Override
    public ReviewTask executeSyncReview(CodeReviewRequestDTO dto, Long userId) {
        // 创建审查任务
        ReviewTask task = new ReviewTask();
        task.setUserId(userId);
        task.setTitle(dto.getTitle());
        task.setCodeContent(dto.getCodeContent());
        task.setLanguage(dto.getLanguage());
        task.setAiModel(dto.getAiModel());
        task.setStatus(ReviewStatusEnum.PENDING.getCode());

        reviewTaskMapper.insert(task);

        // 同步执行审查
        executeReview(task);

        return reviewTaskMapper.selectById(task.getId());
    }

    @Override
    public void executeAsyncReview(Long taskId) {
        // 获取已存在的任务
        ReviewTask task = reviewTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: taskId={}", taskId);
            throw new BusinessException("任务不存在");
        }

        // 执行审查
        executeReview(task);
    }

    @Override
    public ReviewTask getTaskDetail(Long taskId) {
        ReviewTask task = reviewTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("审查任务不存在");
        }
        return task;
    }

    @Override
    public PageResponseDTO<ReviewTask> getUserTasks(Long userId, Integer page, Integer size) {
        Page<ReviewTask> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ReviewTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewTask::getUserId, userId);
        wrapper.orderByDesc(ReviewTask::getCreateTime);

        Page<ReviewTask> result = reviewTaskMapper.selectPage(pageParam, wrapper);

        // 构建分页响应对象
        PageResponseDTO<ReviewTask> response = new PageResponseDTO<>();
        response.setRecords(result.getRecords());
        response.setTotal(result.getTotal());
        response.setPage(page);
        response.setSize(size);

        return response;
    }

    @Override
    public void deleteTask(Long taskId, Long userId) {
        ReviewTask task = reviewTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("审查任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该任务");
        }

        reviewTaskMapper.deleteById(taskId);
        log.info("删除审查任务: taskId={}, userId={}", taskId, userId);
    }

    /**
     * 执行代码审查（核心方法）
     */
    private void executeReview(ReviewTask task) {
        String lockKey = RedisConstants.DISTRIBUTED_LOCK_KEY + "review:" + task.getId();
        String lockValue = UUID.randomUUID().toString();

        try {
            // 尝试获取分布式锁
            Boolean locked = redisUtils.tryLock(lockKey, lockValue, 5, TimeUnit.MINUTES);
            if (!locked) {
                log.warn("获取锁失败，任务可能正在执行: taskId={}", task.getId());
                return;
            }

            // 更新任务状态为审查中
            task.setStatus(ReviewStatusEnum.REVIEWING.getCode());
            reviewTaskMapper.updateById(task);

            // 获取AI客户端策略
            AIClientStrategy strategy = aiClientFactory.getStrategy(task.getAiModel());

            // 调用AI进行代码审查
            String reviewResult = strategy.reviewCode(task.getCodeContent(), task.getLanguage());

            // 解析审查结果
            parseAndSaveResult(task, reviewResult);

            // 缓存审查结果
            String cacheKey = RedisConstants.AI_REVIEW_CACHE_KEY + task.getId();
            redisUtils.set(cacheKey, reviewResult, RedisConstants.REVIEW_CACHE_EXPIRE_TIME, TimeUnit.HOURS);

            log.info("代码审查完成: taskId={}", task.getId());

        } catch (Exception e) {
            log.error("代码审查失败: taskId={}", task.getId(), e);
            task.setStatus(ReviewStatusEnum.FAILED.getCode());
            task.setErrorMsg(e.getMessage());
            reviewTaskMapper.updateById(task);
        } finally {
            // 释放锁
            redisUtils.unlock(lockKey, lockValue);
        }
    }

    /**
     * 解析并保存审查结果
     */
    private void parseAndSaveResult(ReviewTask task, String reviewResult) {
        log.info("开始解析审查结果 - taskId: {}", task.getId());
        log.debug("原始审查结果: {}", reviewResult);

        try {
            // 提取 JSON 内容
            String jsonContent = extractJsonContent(reviewResult);
            log.debug("提取的 JSON 内容: {}", jsonContent);

            // 尝试解析JSON格式的结果
            JSONObject result = JSON.parseObject(jsonContent);

            task.setReviewResult(jsonContent);
            task.setQualityScore(result.getInteger("qualityScore"));
            task.setSecurityScore(result.getInteger("securityScore"));
            task.setPerformanceScore(result.getInteger("performanceScore"));

            if (result.containsKey("issues")) {
                task.setIssueCount(result.getJSONArray("issues").size());
            }

            task.setStatus(ReviewStatusEnum.COMPLETED.getCode());
            log.info("审查结果解析成功 - taskId: {}, qualityScore: {}, securityScore: {}, performanceScore: {}",
                    task.getId(), task.getQualityScore(), task.getSecurityScore(), task.getPerformanceScore());
        } catch (Exception e) {
            log.error("解析审查结果失败 - taskId: {}, error: {}", task.getId(), e.getMessage(), e);
            // 如果解析失败，直接保存原始结果
            task.setReviewResult(reviewResult);
            task.setStatus(ReviewStatusEnum.COMPLETED.getCode());
        }

        reviewTaskMapper.updateById(task);
    }

    /**
     * 从 AI 返回的内容中提取 JSON
     * 支持多种格式：
     * 1. 纯 JSON: {...}
     * 2. Markdown 代码块: ```json\n{...}\n```
     * 3. 文本 + JSON: 一些说明\n{...}
     */
    private String extractJsonContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "{}";
        }

        content = content.trim();

        // 情况1: 检查是否有 markdown 代码块
        if (content.contains("```json")) {
            int startIndex = content.indexOf("```json") + 7;
            int endIndex = content.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                String extracted = content.substring(startIndex, endIndex).trim();
                log.debug("从 markdown 代码块中提取 JSON");
                return extracted;
            }
        }

        // 情况2: 检查是否有普通代码块
        if (content.contains("```")) {
            int startIndex = content.indexOf("```") + 3;
            // 跳过可能的语言标识
            while (startIndex < content.length() && content.charAt(startIndex) != '\n' && content.charAt(startIndex) != '{') {
                startIndex++;
            }
            int endIndex = content.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                String extracted = content.substring(startIndex, endIndex).trim();
                log.debug("从普通代码块中提取 JSON");
                return extracted;
            }
        }

        // 情况3: 查找第一个 { 到最后一个 }
        int firstBrace = content.indexOf('{');
        int lastBrace = content.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            String extracted = content.substring(firstBrace, lastBrace + 1).trim();
            log.debug("从文本中提取 JSON（查找大括号）");
            return extracted;
        }

        // 情况4: 直接返回原内容（可能已经是纯 JSON）
        log.debug("直接使用原始内容作为 JSON");
        return content;
    }
}
