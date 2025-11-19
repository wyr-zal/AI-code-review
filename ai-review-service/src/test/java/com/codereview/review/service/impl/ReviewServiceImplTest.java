package com.codereview.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codereview.common.exception.BusinessException;
import com.codereview.review.dto.PageResponseDTO;
import com.codereview.review.dto.ReviewTaskQueryDTO;
import com.codereview.review.entity.ReviewTask;
import com.codereview.review.mapper.ReviewTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReviewServiceImpl单元测试
 * @author CodeReview
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ReviewServiceImplTest {

    @Mock
    private ReviewTaskMapper reviewTaskMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ReviewTask mockTask;

    @BeforeEach
    void setUp() {
        mockTask = new ReviewTask();
        mockTask.setId(1L);
        mockTask.setUserId(1L);
        mockTask.setTitle("测试代码审查");
        mockTask.setCodeContent("public class Test { }");
        mockTask.setLanguage("Java");
        mockTask.setAiModel("gpt-3.5-turbo");
        mockTask.setStatus(0);
        mockTask.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testGetTaskDetail_Success() {
        // Given
        when(reviewTaskMapper.selectById(1L)).thenReturn(mockTask);

        // When
        ReviewTask result = reviewService.getTaskDetail(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试代码审查", result.getTitle());
        verify(reviewTaskMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetTaskDetail_NotExists() {
        // Given
        when(reviewTaskMapper.selectById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reviewService.getTaskDetail(1L, 1L);
        });
        assertEquals("审查任务不存在", exception.getMessage());
        verify(reviewTaskMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetTaskDetail_NoPermission() {
        // Given
        when(reviewTaskMapper.selectById(1L)).thenReturn(mockTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reviewService.getTaskDetail(1L, 2L);  // 不同的userId
        });
        assertEquals("无权访问此任务", exception.getMessage());
        verify(reviewTaskMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetUserTasks_Success() {
        // Given
        ReviewTaskQueryDTO queryDTO = new ReviewTaskQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setSize(10);

        Page<ReviewTask> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(mockTask));
        mockPage.setTotal(1L);

        when(reviewTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        PageResponseDTO<ReviewTask> result = reviewService.getUserTasks(1L, queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        verify(reviewTaskMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetUserTasks_Empty() {
        // Given
        ReviewTaskQueryDTO queryDTO = new ReviewTaskQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setSize(10);

        Page<ReviewTask> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Collections.emptyList());
        mockPage.setTotal(0L);

        when(reviewTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        PageResponseDTO<ReviewTask> result = reviewService.getUserTasks(1L, queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
        assertEquals(0L, result.getTotal());
        verify(reviewTaskMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testDeleteTask_Success() {
        // Given
        when(reviewTaskMapper.selectById(1L)).thenReturn(mockTask);
        when(reviewTaskMapper.deleteById(1L)).thenReturn(1);

        // When
        reviewService.deleteTask(1L, 1L);

        // Then
        verify(reviewTaskMapper, times(1)).selectById(1L);
        verify(reviewTaskMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTask_NotExists() {
        // Given
        when(reviewTaskMapper.selectById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reviewService.deleteTask(1L, 1L);
        });
        assertEquals("审查任务不存在", exception.getMessage());
        verify(reviewTaskMapper, times(1)).selectById(1L);
        verify(reviewTaskMapper, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteTask_NoPermission() {
        // Given
        when(reviewTaskMapper.selectById(1L)).thenReturn(mockTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reviewService.deleteTask(1L, 2L);  // 不同的userId
        });
        assertEquals("无权删除此任务", exception.getMessage());
        verify(reviewTaskMapper, times(1)).selectById(1L);
        verify(reviewTaskMapper, never()).deleteById(anyLong());
    }
}
