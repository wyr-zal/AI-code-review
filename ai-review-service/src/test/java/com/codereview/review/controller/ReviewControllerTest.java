package com.codereview.review.controller;

import com.codereview.review.dto.CodeReviewRequestDTO;
import com.codereview.review.dto.PageResponseDTO;
import com.codereview.review.dto.ReviewTaskQueryDTO;
import com.codereview.review.entity.ReviewTask;
import com.codereview.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReviewController单元测试
 * @author CodeReview
 */
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    private CodeReviewRequestDTO reviewRequestDTO;
    private ReviewTask mockTask;

    @BeforeEach
    void setUp() {
        reviewRequestDTO = new CodeReviewRequestDTO();
        reviewRequestDTO.setTitle("测试代码审查");
        reviewRequestDTO.setCodeContent("public class Test { }");
        reviewRequestDTO.setLanguage("Java");
        reviewRequestDTO.setAiModel("Qwen3-Coder");
        reviewRequestDTO.setAsync(true);

        mockTask = new ReviewTask();
        mockTask.setId(1L);
        mockTask.setUserId(1L);
        mockTask.setTitle("测试代码审查");
        mockTask.setCodeContent("public class Test { }");
        mockTask.setLanguage("Java");
        mockTask.setAiModel("Qwen3-Coder");
        mockTask.setStatus(2); // 已完成
        mockTask.setQualityScore(85);
        mockTask.setSecurityScore(90);
        mockTask.setPerformanceScore(80);
        mockTask.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testSubmitReview_Success() throws Exception {
        // Given
        when(reviewService.submitReviewTask(any(CodeReviewRequestDTO.class), anyLong())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/review/submit")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("任务提交成功"))
                .andExpect(jsonPath("$.data").value(1));

        verify(reviewService, times(1)).submitReviewTask(any(CodeReviewRequestDTO.class), eq(1L));
    }

    @Test
    void testSubmitReview_NoUserId() throws Exception {
        // When & Then
        mockMvc.perform(post("/review/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        verify(reviewService, never()).submitReviewTask(any(), anyLong());
    }

    @Test
    void testSubmitReview_InvalidInput() throws Exception {
        // Given
        CodeReviewRequestDTO invalidDTO = new CodeReviewRequestDTO();
        invalidDTO.setTitle(""); // 标题为空
        invalidDTO.setCodeContent(""); // 代码为空

        // When & Then
        mockMvc.perform(post("/review/submit")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).submitReviewTask(any(), anyLong());
    }

    @Test
    void testSyncReview_Success() throws Exception {
        // Given
        when(reviewService.executeSyncReview(any(CodeReviewRequestDTO.class), anyLong())).thenReturn(mockTask);

        // When & Then
        mockMvc.perform(post("/review/sync")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试代码审查"))
                .andExpect(jsonPath("$.data.qualityScore").value(85));

        verify(reviewService, times(1)).executeSyncReview(any(CodeReviewRequestDTO.class), eq(1L));
    }

    @Test
    void testGetTaskDetail_Success() throws Exception {
        // Given
        when(reviewService.getTaskDetail(1L, 1L)).thenReturn(mockTask);

        // When & Then
        mockMvc.perform(get("/review/task/1")
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试代码审查"));

        verify(reviewService, times(1)).getTaskDetail(1L, 1L);
    }

    @Test
    void testGetTaskDetail_NoUserId() throws Exception {
        // When & Then
        mockMvc.perform(get("/review/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        verify(reviewService, never()).getTaskDetail(anyLong(), anyLong());
    }

    @Test
    void testGetUserTasks_Success() throws Exception {
        // Given
        PageResponseDTO<ReviewTask> pageResponse = new PageResponseDTO<>();
        pageResponse.setRecords(Arrays.asList(mockTask));
        pageResponse.setTotal(1L);
        pageResponse.setPage(1);
        pageResponse.setSize(10);

        when(reviewService.getUserTasks(anyLong(), any(ReviewTaskQueryDTO.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/review/tasks")
                .header("X-User-Id", "1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").value(1));

        verify(reviewService, times(1)).getUserTasks(eq(1L), any(ReviewTaskQueryDTO.class));
    }

    @Test
    void testGetUserTasks_WithFilters() throws Exception {
        // Given
        PageResponseDTO<ReviewTask> pageResponse = new PageResponseDTO<>();
        pageResponse.setRecords(Collections.emptyList());
        pageResponse.setTotal(0L);
        pageResponse.setPage(1);
        pageResponse.setSize(10);

        when(reviewService.getUserTasks(anyLong(), any(ReviewTaskQueryDTO.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/review/tasks")
                .header("X-User-Id", "1")
                .param("page", "1")
                .param("size", "10")
                .param("status", "2")
                .param("language", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0));

        verify(reviewService, times(1)).getUserTasks(eq(1L), any(ReviewTaskQueryDTO.class));
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        // Given
        doNothing().when(reviewService).deleteTask(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/review/task/1")
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(reviewService, times(1)).deleteTask(1L, 1L);
    }

    @Test
    void testDeleteTask_NoUserId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/review/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        verify(reviewService, never()).deleteTask(anyLong(), anyLong());
    }
}
