package com.codereview.user.controller;

import com.codereview.common.result.Result;
import com.codereview.user.dto.UserLoginDTO;
import com.codereview.user.dto.UserRegisterDTO;
import com.codereview.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController单元测试
 * @author CodeReview
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRegisterDTO registerDTO;
    private UserLoginDTO loginDTO;
    private Map<String, Object> loginResult;

    @BeforeEach
    void setUp() {
        registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("123456");
        registerDTO.setEmail("test@example.com");
        registerDTO.setNickname("测试用户");

        loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        loginResult = new HashMap<>();
        loginResult.put("token", "mock-jwt-token");
        loginResult.put("userId", 1L);
        loginResult.put("username", "testuser");
        loginResult.put("nickname", "测试用户");
        loginResult.put("email", "test@example.com");
    }

    @Test
    void testRegister_Success() throws Exception {
        // Given
        doNothing().when(userService).register(any(UserRegisterDTO.class));

        // When & Then
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"));

        verify(userService, times(1)).register(any(UserRegisterDTO.class));
    }

    @Test
    void testRegister_InvalidInput() throws Exception {
        // Given
        UserRegisterDTO invalidDTO = new UserRegisterDTO();
        invalidDTO.setUsername("ab"); // 用户名太短
        invalidDTO.setPassword("123"); // 密码太短
        invalidDTO.setEmail("invalid-email"); // 邮箱格式错误

        // When & Then
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(UserRegisterDTO.class));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        when(userService.login(any(UserLoginDTO.class))).thenReturn(loginResult);

        // When & Then
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService, times(1)).login(any(UserLoginDTO.class));
    }

    @Test
    void testLogin_InvalidInput() throws Exception {
        // Given
        UserLoginDTO invalidDTO = new UserLoginDTO();
        invalidDTO.setUsername(""); // 用户名为空
        invalidDTO.setPassword(""); // 密码为空

        // When & Then
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(UserLoginDTO.class));
    }

    @Test
    void testLogout_Success() throws Exception {
        // Given
        String userId = "1";
        doNothing().when(userService).logout(userId);

        // When & Then
        mockMvc.perform(post("/user/logout")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));

        verify(userService, times(1)).logout(userId);
    }

    @Test
    void testLogout_NoUserId() throws Exception {
        // When & Then
        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        verify(userService, never()).logout(anyString());
    }

    @Test
    void testGetUserInfo_Success() throws Exception {
        // Given
        String userId = "1";
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", 1L);
        userInfo.put("username", "testuser");
        userInfo.put("nickname", "测试用户");
        userInfo.put("email", "test@example.com");

        when(userService.getUserInfo(1L)).thenReturn(userInfo);

        // When & Then
        mockMvc.perform(get("/user/info")
                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService, times(1)).getUserInfo(1L);
    }

    @Test
    void testGetUserInfo_NoUserId() throws Exception {
        // When & Then
        mockMvc.perform(get("/user/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        verify(userService, never()).getUserInfo(anyLong());
    }
}
