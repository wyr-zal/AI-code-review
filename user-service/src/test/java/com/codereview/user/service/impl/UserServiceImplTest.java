package com.codereview.user.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codereview.common.exception.BusinessException;
import com.codereview.common.utils.JwtUtils;
import com.codereview.common.utils.RedisUtils;
import com.codereview.user.dto.UserLoginDTO;
import com.codereview.user.dto.UserRegisterDTO;
import com.codereview.user.entity.User;
import com.codereview.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl单元测试
 * @author CodeReview
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterDTO registerDTO;
    private UserLoginDTO loginDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("123456");
        registerDTO.setEmail("test@example.com");
        registerDTO.setNickname("测试用户");

        loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword(DigestUtil.md5Hex("123456"));
        mockUser.setEmail("test@example.com");
        mockUser.setNickname("测试用户");
        mockUser.setRole(0);
        mockUser.setStatus(1);
    }

    @Test
    void testRegister_Success() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        userService.register(registerDTO);

        // Then
        verify(userMapper, times(2)).selectOne(any(LambdaQueryWrapper.class));
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testRegister_UsernameExists() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerDTO);
        });
        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testRegister_EmailExists() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)  // 第一次查询用户名不存在
                .thenReturn(mockUser);  // 第二次查询邮箱已存在

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerDTO);
        });
        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userMapper, times(2)).selectOne(any(LambdaQueryWrapper.class));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        doNothing().when(redisUtils).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // When
        Map<String, Object> result = userService.login(loginDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("token"));
        assertTrue(result.containsKey("userId"));
        assertTrue(result.containsKey("username"));
        assertTrue(result.containsKey("nickname"));
        assertTrue(result.containsKey("email"));
        assertEquals(1L, result.get("userId"));
        assertEquals("testuser", result.get("username"));
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(redisUtils, times(1)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testLogin_UserNotExists() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(redisUtils, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testLogin_WrongPassword() {
        // Given
        loginDTO.setPassword("wrongpassword");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
        assertEquals("密码错误", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(redisUtils, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testLogin_UserDisabled() {
        // Given
        mockUser.setStatus(0);  // 禁用状态
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
        assertEquals("账号已被禁用", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(redisUtils, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testLogout_Success() {
        // Given
        String userId = "1";
        when(redisUtils.delete(anyString())).thenReturn(true);

        // When
        userService.logout(userId);

        // Then
        verify(redisUtils, times(1)).delete(anyString());
    }

    @Test
    void testGetUserInfo_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        // When
        Map<String, Object> result = userService.getUserInfo(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("userId"));
        assertEquals("testuser", result.get("username"));
        assertEquals("测试用户", result.get("nickname"));
        assertEquals("test@example.com", result.get("email"));
        assertEquals(0, result.get("role"));
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetUserInfo_UserNotExists() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserInfo(1L);
        });
        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper, times(1)).selectById(1L);
    }
}
